package com.mechcard.ui.person

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CreateCollectionRequest
import com.amazonaws.services.rekognition.model.DeleteFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.services.rekognition.model.IndexFacesRequest
import com.amazonaws.services.rekognition.model.QualityFilter
import com.amazonaws.services.rekognition.model.S3Object
import com.google.gson.Gson
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.livinglifetechway.k4kotlin.core.androidx.hideKeyboard
import com.livinglifetechway.k4kotlin.core.androidx.shortToast
import com.livinglifetechway.k4kotlin.core.androidx.toast
import com.livinglifetechway.k4kotlin.core.androidx.toastNow
import com.livinglifetechway.k4kotlin.core.orFalse
import com.mechcard.MainActivity
import com.mechcard.R
import com.mechcard.apis.ApiViewModel
import com.mechcard.databinding.FragmentAddPersonBinding
import com.mechcard.databinding.LayoutCaptureSuccessBinding
import com.mechcard.models.ImageS3Request
import com.mechcard.models.Mechanic
import com.mechcard.models.MechanicFaceRequest
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.utils.EncryptionUtils
import com.mechcard.utils.face.CameraXViewModel
import com.mechcard.utils.face.CaptureListener
import com.mechcard.utils.face.FaceDetectorProcessor
import com.mechcard.utils.face.PreferenceUtils
import com.mechcard.utils.face.VisionImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.Exception
import kotlin.math.log


class AddPersonFragment : Fragment() {

    companion object {
        fun newInstance() = AddPersonFragment()
        private const val TAG = "AddPersonFragment"
        private const val FACE_DETECTION = "Face Detection"

    }

    private lateinit var viewModel: AddPersonViewModel
    private lateinit var apiViewModel: ApiViewModel
    private lateinit var mBinding: FragmentAddPersonBinding
//    private var collectionId = "mechcard-faces"

    private val suggestions = ArrayList<Mechanic>()
    private lateinit var adapter: ArrayAdapter<Mechanic>
    private var selectedMechanic: Mechanic? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = FACE_DETECTION
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_person, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddPersonViewModel::class.java)
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[CameraXViewModel::class.java]
            .processCameraProvider
            .observe(
                requireActivity()
            )
            { provider ->
                cameraProvider = provider
                bindAllCameraUseCases()
            }

        mBinding.textBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.textCaptureFace.setOnClickListener {
//            showPersonAddedDialog()
            Log.e(TAG, "onViewCreated: ${(suggestions.size)}")
            Log.e(
                TAG,
                "onViewCreated: ${Gson().toJson(suggestions.filter { it -> it.faceID.isEmpty() })}"
            )
            Log.e(
                TAG,
                "onViewCreated: ${Gson().toJson(suggestions.filter { it -> it.faceID.isNotEmpty() })}"
            )
        }

        mBinding.actMechanicName.setOnFocusChangeListener { view, b ->
            mBinding.actMechanicName.showDropDown()
        }

        mBinding.actMechanicName.setOnClickListener { view ->
            mBinding.actMechanicName.showDropDown()
        }

        mBinding.actMechanicName.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                selectedMechanic = suggestions.getOrNull(position)
                mBinding.tvMechanicCode.setText(selectedMechanic?.code)
            }

        (requireActivity() as MainActivity).setInactivityListener(object : InactivityListener {
            override fun onInactivityCallback() {
                if (alertDialog?.isShowing.orFalse()) {
                    alertDialog?.dismiss()
                }
                alertDialog?.dismiss()
                (requireActivity() as MainActivity).resetDisconnectTimer()
//                MechCardPref.clear()
                val startDestination = findNavController().graph.startDestinationId
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(startDestination, true)
                    .build()
                findNavController().navigate(startDestination, null, navOptions)
            }
        })

        getMechanics()
    }

    private fun getMechanics() {
        val dialog = MechProgressDialog(requireContext())
            .setTitle("Loading...")
            .create()
        dialog.show()

        apiViewModel.getMechanics(
            9999,
            0,
            "",
            "ASC",
            "",
            "Bearer ${MechCardPref.accessToken}"
        ) { mechanics ->
            suggestions.clear()
            suggestions.addAll(mechanics)
            setPersonAdapter()
            dialog.dismiss()
        }
    }

    private fun setPersonAdapter() {
        adapter = ArrayAdapter<Mechanic>(
            requireContext(),
            R.layout.layout_drodown_item,
            suggestions
        )

        mBinding.actMechanicName.setAdapter(adapter)
    }

    private fun showPersonAddedDialog(
        rekognitionClient: AmazonRekognitionClient,
        faceId: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val bind = LayoutCaptureSuccessBinding.inflate(layoutInflater)
        builder.setView(bind.root)
        alertDialog = builder.create()
        alertDialog?.setCancelable(false)
        alertDialog?.show()

        bind.textViewName.text = "${selectedMechanic?.name} (${selectedMechanic?.code})"

        bind.buttonRecapture.setOnClickListener {
            lifecycleScope.launch {
                deleteFacesCollection(rekognitionClient, faceId)
                bindAllCameraUseCases()
            }
            alertDialog?.dismiss()
        }
        bind.buttonAddFace.setOnClickListener {
            val pin = bind.squareField.text.toString()
            if (pin.length == 4) {
                val request = MechanicFaceRequest(
                    mechanicID = selectedMechanic?.code,
                    faceID = faceId,
                    pin = bind.squareField.text.toString()
                )

                Log.e("*******  Add Face  ********", "")
                Log.e("Bearer token", "Bearer ${MechCardPref.accessToken}")
                Log.e("Access token", "Bearer ${MechCardPref.accessToken}")
                Log.e("Refresh token", "Bearer ${MechCardPref.refreshToken}")
                Log.e("Mechanic ID", selectedMechanic?.code.toString())
                Log.e("Face ID", faceId.toString())
                Log.e("PIN", pin.toString())

                val dialog = MechProgressDialog(requireContext())
                    .setTitle("Validating Face...")
                    .create()
                dialog.show()





                apiViewModel.addMechanicFace(
                    request,
                    "Bearer ${MechCardPref.accessToken}"
                ) { mechanics, action ->
                    dialog.dismiss()
                    if (action?.status == "0") {
                        alertDialog?.dismiss()
                        selectedMechanic?.faceID?.let {
                            lifecycleScope.launch {
                                deleteFacesCollection(rekognitionClient, it)
                            }
                        }
                        signInAPICall(faceId)
//                        findNavController().navigateUp()
                    } else {
                        hideKeyboard()
                        bind.squareField.fieldColor = resources.getColor(R.color.error, null)
                        bind.squareField.fieldBgColor =
                            resources.getColor(R.color.errorContainer, null)
//                        bind.squareField.error = action?.message.orEmpty()
//                        bind.squareField.highlightColor = resources.getColor(R.color.error, null)
                        toastNow(action?.message.orEmpty())
                    }
                }

            } else {
                toastNow("Please enter valid security pin")
            }
        }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }

    override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(requireContext())) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = getTargetResolution()

//            PreferenceUtils.getCameraXTargetResolution(requireContext(), lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(mBinding.previewView.surfaceProvider)
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            previewUseCase
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
//        val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(requireContext())
        val faceDetectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        imageProcessor = FaceDetectorProcessor(requireContext(), faceDetectorOptions)

        imageProcessor?.setCaptureListener(object : CaptureListener {
            override fun onFaceDetected(probebility: Double) {
//                toast("Face Detected with : ${probebility}")
                Log.e(TAG, "onFaceDetected: ${probebility}")
                if (selectedMechanic != null) {
                    imageProcessor?.stop()

                    val bitmap = getViewBitmap(mBinding.graphicOverlay)
                    bitmap?.let { b ->
                        val base64Image = bitmapToBase64(bitmap)
                        uploadToS3(base64Image, "png")
                    }

                }
            }
        })

        val builder = ImageAnalysis.Builder()
        val targetResolution = getTargetResolution()
//            PreferenceUtils.getCameraXTargetResolution(requireContext(), lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(requireContext()),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        mBinding.graphicOverlay.setImageSourceInfo(
                            imageProxy.width,
                            imageProxy.height,
                            isImageFlipped
                        )
                    } else {
                        mBinding.graphicOverlay.setImageSourceInfo(
                            imageProxy.height,
                            imageProxy.width,
                            isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
//                    imageProxy?.image?.let {
//                        val image = InputImage.fromMediaImage(it, rotationDegrees)
//                        detectFaces(image)
//                    }

                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, mBinding.graphicOverlay)


                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            analysisUseCase
        )
    }


    private fun addFaceToCollection(objectKey: String, bucketName: String) {
        val rekognitionClient: AmazonRekognitionClient = AmazonRekognitionClient(
            BasicAWSCredentials(
                EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsAccessKey,
                    "xkKbey58VgC4jYTMPfaXGrr"
                ), EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsSecret,
                    "xkKbey58VgC4jYTMPfaXGrr"
                )
            )
        )
        rekognitionClient.setRegion(Region.getRegion(Regions.AP_SOUTH_1))

        lifecycleScope.launch {
            addToCollection(
                objectKey,
                bucketName,
                rekognitionClient
            )
        }
    }

    private suspend fun addToCollection(
        fileName: String,
        bucketName: String,
        rekognitionClient: AmazonRekognitionClient
    ) {
        val image = Image()
            .withS3Object(
                S3Object()
                    .withBucket(bucketName)
                    .withName(fileName)
            )


        val indexFacesRequest = IndexFacesRequest()
            .withImage(image)
            .withQualityFilter(QualityFilter.AUTO)
            .withMaxFaces(1)
            .withCollectionId(MechCardPref.collectionId)
            .withExternalImageId(fileName)
            .withDetectionAttributes("DEFAULT")

        withContext(Dispatchers.IO) {
            val indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest)
            val faceRecords = indexFacesResult.faceRecords
            if (faceRecords.isNotEmpty()) {
                val faceRecord = faceRecords?.get(0)
                faceRecord?.let {
                    Log.e(TAG, "addToCollection: ${it.face?.faceId.orEmpty()}")
                    requireActivity().runOnUiThread {
                        showPersonAddedDialog(rekognitionClient, it.face?.faceId.orEmpty())
                    }
                }
            }
            for (faceRecord in faceRecords) {
                println("  Face ID: " + faceRecord.face.faceId)
                println("  Location:" + faceRecord.faceDetail.boundingBox.toString())
            }

            val unindexedFaces = indexFacesResult.unindexedFaces
            println("Faces not indexed:")
            for (unindexedFace in unindexedFaces) {
                println("  Location:" + unindexedFace.faceDetail.boundingBox.toString())
                println("  Reasons:")
                for (reason in unindexedFace.reasons) {
                    println("   $reason")
                }
            }
        }

    }

//    suspend fun createMyCollection(client: AmazonRekognitionClient) {
//        val request = CreateCollectionRequest()
//        request.collectionId = collectionId
//        withContext(Dispatchers.IO) {
//            client.createCollection(request)
//        }
//    }

    private fun uploadToS3(base64: String, extension: String) {
        val request = ImageS3Request(
            mechanicID = selectedMechanic?.code, base64Image = base64, extension = extension
        )

        val dialog = MechProgressDialog(requireContext()).setTitle("Uploading ...").create()
        dialog.show()
        apiViewModel.uploadToS3(
            request, "Bearer ${MechCardPref.accessToken}"
        ) { response ->
            dialog.dismiss()
            if (!response?.keyname.isNullOrEmpty()) {
//                shortToast("File Uploaded")
                addFaceToCollection(response?.keyname.orEmpty(), response?.bucketname.orEmpty())
            } else {
                toastNow("Something went wrong, Try again!")
                lifecycleScope.launch {
                    bindAllCameraUseCases()
                }
            }
        }
    }

    fun getViewBitmap(view: View): Bitmap? {
        //Get the dimensions of the view so we can re-layout the view at its current size
        //and create a bitmap of the same size
        val width = view.width
        val height = view.height
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        //Cause the view to re-layout
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        //Create a bitmap backed Canvas to draw the view into
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)

        //Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
        view.draw(c)
        return b
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    //Delete Face id from collection
    suspend fun deleteFacesCollection(
        rekognitionClient: AmazonRekognitionClient,
        faceId: String?
    ) {
        try {

            val faceIds = arrayListOf<String>(faceId.orEmpty())
            withContext(Dispatchers.IO) {
                val deleteFacesRequest: DeleteFacesRequest = DeleteFacesRequest()
                    .withCollectionId(MechCardPref.collectionId)
                    .withFaceIds(faceIds)

                val deleteFacesResult = rekognitionClient.deleteFaces(deleteFacesRequest)

                val faceRecords = deleteFacesResult.deletedFaces
                faceRecords.forEach {
                    Log.e(TAG, "deleteFacesCollection: ${it}")
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTargetResolution(): Size {
        return when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> Size(1200, 1600)
            Configuration.ORIENTATION_LANDSCAPE -> Size(1600, 1200)
            else -> Size(1600, 1200)
        }
    }

    fun signInAPICall(faceId: String) {
//        val code = "M01"
//        val mechanicAuthData = faceId
//        val name = "Jitosh"


        Log.e("******  Sign In ******", "")
        Log.e("Authorization", "Bearer $" + MechCardPref.accessToken)
        Log.e("Access Token", "" + MechCardPref.accessToken)
        Log.e("RefreshToken", "" + MechCardPref.refreshToken)
        Log.e("FaceID", "" + faceId)

        val dialog = MechProgressDialog(requireContext()).setTitle("Signing In...").create()
        dialog.show()
        apiViewModel.signIn(
            MechCardPref.accessToken, MechCardPref.refreshToken, faceId
        ) { signinResp ->
            dialog.dismiss()
            if (signinResp == null) {
                toastNow("Face not found")
                bindAllCameraUseCases()
            } else {
                MechCardPref.isUserLogIn = true
                if (signinResp.role.equals("S")) {
                    findNavController().navigate(R.id.action_addPersonFragment_to_dashboardSupervisorFragment)
                } else {
                    findNavController().navigate(R.id.action_addPersonFragment_to_jobsFragment)
                }
            }
        }
    }


}
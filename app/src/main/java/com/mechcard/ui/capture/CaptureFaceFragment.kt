package com.mechcard.ui.capture

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CreateCollectionRequest
import com.amazonaws.services.rekognition.model.S3Object
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.livinglifetechway.k4kotlin.core.androidx.toastNow
import com.mechcard.MainActivity
import com.mechcard.R
import com.mechcard.apis.ApiViewModel
import com.mechcard.apis.Constants
import com.mechcard.databinding.FragmentCaptureFaceBinding
import com.mechcard.databinding.LayoutInactivityBinding
import com.mechcard.databinding.LayoutWelcomeBinding
import com.mechcard.models.ImageS3Request
import com.mechcard.models.SignInResponse
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.utils.EncryptionUtils
import com.mechcard.utils.face.CameraXViewModel
import com.mechcard.utils.face.CaptureListener
import com.mechcard.utils.face.FaceDetectorProcessor
import com.mechcard.utils.face.GraphicOverlay
import com.mechcard.utils.face.PreferenceUtils
import com.mechcard.utils.face.VisionImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CaptureFaceFragment : Fragment() {

    companion object {
        fun newInstance() = CaptureFaceFragment()

        private const val TAG = "CameraXLivePreview"
        private const val FACE_DETECTION = "Face Detection"

    }

    private lateinit var viewModel: CaptureFaceViewModel
    private lateinit var apiViewModel: ApiViewModel
    private lateinit var mBinding: FragmentCaptureFaceBinding

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var captureListener: CaptureListener? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = FACE_DETECTION
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? = null
    private var detectedImageProxy: ImageProxy? = null
    private var navigateToAddFaceScreen:Boolean=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_capture_face, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CaptureFaceViewModel::class.java)
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        generateAccessToken()
        Log.e("******  Get Token ******","")
        Log.e("clientID",Constants.DEV_CLIENT_ID)
        Log.e("clientSecret",Constants.DEV_SECRET)
//        getAWSConfigData()
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[CameraXViewModel::class.java].processCameraProvider.observe(
            requireActivity()
        ) { provider ->
            cameraProvider = provider
            bindAllCameraUseCases()
        }
        mBinding.textCaptureFace.setOnClickListener {


//            findNavController().navigate(R.id.action_captureFaceFragment_to_jobsFragment)
        }

        mBinding.buttonAddFace.setOnClickListener {
            navigateToAddFaceScreen=true
            imageProcessor?.run { this.stop() }
            findNavController().navigate(R.id.action_captureFaceFragment_to_addPersonFragment)
        }

        (requireActivity() as MainActivity).setInactivityListener(object : InactivityListener {
            override fun onInactivityCallback() {
//                toastNow("show Dialog")
                MechCardPref.isUserLogIn = false
                MechCardPref.signedInMechanic = null
                MechCardPref.accessToken = null
                MechCardPref.refreshToken = null


                Log.e("******  Get Token ******","")
                Log.e("clientID",Constants.DEV_CLIENT_ID)
                Log.e("clientSecret",Constants.DEV_SECRET)


                apiViewModel.getToken(Constants.DEV_CLIENT_ID, Constants.DEV_SECRET) {

                }
                imageProcessor?.stop()
                showAppInactivityDialog()
            }
        })

//        signInAPICall("72826351-9cd9-4355-a07d-879498005326")
    }

    private fun getAWSConfigData() {
        val dialog = MechProgressDialog(requireActivity()).setTitle("Loading...").create()
        dialog.show()
        apiViewModel.getAWSConfigData("Bearer ${MechCardPref.accessToken}") { configData ->
            dialog.dismiss()
            configData?.let {
                MechCardPref.configData = it
                lifecycleScope.launch {
                    createMyCollection()
                }
            }
        }
    }

    private fun showFaceVerifiedDialog(signinResp: SignInResponse) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val bind = LayoutWelcomeBinding.inflate(layoutInflater)
        builder.setView(bind.root)
        bind.tvName.text = "Name: ${MechCardPref.signedInMechanic?.mechanicName}"
        bind.tvMobNo.text = "Mobile Number: -"
        bind.tvId.text = "ID: ${MechCardPref.signedInMechanic?.mechanicid}"
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()


        if(signinResp.role.equals("S")){
            Looper.myLooper()?.let {
                Handler(it).postDelayed({
                    alertDialog.dismiss()
                    findNavController().navigate(R.id.action_captureFaceFragment_to_dashboardSupervisorFragment)
                }, 2000)
            }
        }else{
            if (MechCardPref.signedInMechanic?.taskData != null && MechCardPref.signedInMechanic?.taskData?.jobID != null) {
                Looper.myLooper()?.let {
                    Handler(it).postDelayed({
                        alertDialog.dismiss()
                        findNavController().navigate(R.id.action_captureFaceFragment_to_runningServicesFragment)
                    }, 2000)
                }
            } else {
                Looper.myLooper()?.let {
                    Handler(it).postDelayed({
                        alertDialog.dismiss()
                        findNavController().navigate(R.id.action_captureFaceFragment_to_jobsFragment)
                    }, 2000)
                }
            }
        }



//        bind.viewMain.setOnClickListener {
//            alertDialog.dismiss()
//            findNavController().navigate(R.id.action_captureFaceFragment_to_jobsFragment)
//        }
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
        startTimer()
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
        val targetResolution =
            PreferenceUtils.getCameraXTargetResolution(requireContext(), lensFacing)
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
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
        imageProcessor = FaceDetectorProcessor(requireContext(), faceDetectorOptions)

        imageProcessor?.setCaptureListener(object : CaptureListener {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onFaceDetected(probebility: Double) {
                val image: Image? = detectedImageProxy?.image
                imageProcessor?.stop()

                image?.let {
                    val bitmap = getViewBitmap(mBinding.graphicOverlay)
                    bitmap?.let { b ->
                        val base64Image = bitmapToBase64(bitmap)
                        uploadToS3(base64Image, "png")
                    }
                }
            }
        })

        val builder = ImageAnalysis.Builder()
        val targetResolution =
            PreferenceUtils.getCameraXTargetResolution(requireContext(), lensFacing)
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
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        mBinding.graphicOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, mBinding.graphicOverlay)
                    detectedImageProxy = imageProxy
                    Log.e(TAG, "bindAnalysisUseCase: **************************")
                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            analysisUseCase
        )
    }

    private fun recognizeFace(objectKey: String, bucketName: String) {
        val rekognitionClient: AmazonRekognitionClient = AmazonRekognitionClient(
            BasicAWSCredentials(
                EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsAccessKey, "xkKbey58VgC4jYTMPfaXGrr"
                ), EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsSecret, "xkKbey58VgC4jYTMPfaXGrr"
                )
            )
        )

        rekognitionClient.setRegion(Region.getRegion(Regions.AP_SOUTH_1))

        lifecycleScope.launch {
            compareImageFromBucket(objectKey, rekognitionClient, bucketName)
        }

    }

    private suspend fun compareImageFromBucket(
        fileName: String, rekognitionClient: AmazonRekognitionClient, bucketName: String
    ) {

//        val objectMapper = ObjectMapper()

        val image = com.amazonaws.services.rekognition.model.Image().withS3Object(
            S3Object().withBucket(bucketName).withName(fileName)
        )

        // Search collection for faces similar to the largest face in the image.

        // Search collection for faces similar to the largest face in the image.
        val searchFacesByImageRequest =
            SearchFacesByImageRequest().withCollectionId(MechCardPref.collectionId).withImage(image)
                .withFaceMatchThreshold(90f).withMaxFaces(1)

        System.out.println("Faces matching largest face in image from $fileName ")

        withContext(Dispatchers.IO) {
            try {
                val searchFacesByImageResult =
                    rekognitionClient.searchFacesByImage(searchFacesByImageRequest)

                val faceImageMatches = searchFacesByImageResult.faceMatches
                if (faceImageMatches.isEmpty()) {

                    requireActivity().runOnUiThread {
                        toastNow("No Face found")
                        bindAllCameraUseCases()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        if (faceImageMatches.isNotEmpty()) {
                            faceImageMatches?.get(0)?.face?.faceId?.let {
                                apiViewModel.deleteFromS3("Bearer ${MechCardPref.accessToken}")
                                signInAPICall(it)
                            }
                        }
                        faceImageMatches.forEach {
                            Log.e(TAG, "compareImageFromBucket: ${it.face?.faceId}")
                        }
//                    toastNow("Face found")
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    toastNow("Face not found")
                    bindAllCameraUseCases()
                }
                e.printStackTrace()
            }
        }

    }


    private fun startTimer() {
        lifecycleScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                val time = System.currentTimeMillis()
                setCountdownTime(time)
                startTimer()
            }
        }
    }

    private fun setCountdownTime(time: Long) {

        // Create a SimpleDateFormat instance
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Convert milliseconds to a Date object
        val date = Date(time)

        // Format the date and time
        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)

        mBinding.textDate.text = formattedDate
        mBinding.textTime.text = formattedTime
    }

    private fun uploadToS3(base64: String, extension: String) {
        val request = ImageS3Request(
            mechanicID = "LGN", base64Image = base64, extension = extension
        )

        val dialog = MechProgressDialog(requireContext()).setTitle("Verifying ...").create()
        dialog.show()
        apiViewModel.uploadToS3(
            request, "Bearer ${MechCardPref.accessToken}"
        ) { response ->
            dialog.dismiss()
            if (!response?.keyname.isNullOrEmpty()) {
                recognizeFace(response?.keyname.orEmpty(), response?.bucketname.orEmpty())
            } else {
                toastNow("Something went wrong, Try again!")
                bindAllCameraUseCases()
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
        bitmap.compress(CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun signInAPICall(faceId: String) {
//        val code = "M01"
//        val mechanicAuthData = faceId
//        val name = "Jitosh"

        val dialog = MechProgressDialog(requireContext()).setTitle("Signing In...").create()
        dialog.show()

        Log.e("******  Sign In ******","")
        Log.e("Authorization","Bearer $"+MechCardPref.accessToken)
        Log.e("Access Token",""+MechCardPref.accessToken)
        Log.e("RefreshToken",""+MechCardPref.refreshToken)
        Log.e("FaceID",""+faceId)


        apiViewModel.signIn(
            MechCardPref.accessToken, MechCardPref.refreshToken, faceId
        ) { signinResp ->
            dialog.dismiss()
            if (signinResp == null) {
                toastNow("Face not found")
                bindAllCameraUseCases()
            } else {

                if(navigateToAddFaceScreen!=true){
                    showFaceVerifiedDialog(signinResp)
                    MechCardPref.isUserLogIn = true
                }
            }
        }
    }

    suspend fun createMyCollection() {
        MechCardPref.collectionId = "FinERP-" + MechCardPref.configData?.projectID

        val rekognitionClient: AmazonRekognitionClient = AmazonRekognitionClient(
            BasicAWSCredentials(
                EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsAccessKey, "xkKbey58VgC4jYTMPfaXGrr"
                ), EncryptionUtils().decrypt(
                    MechCardPref.configData?.awsSecret, "xkKbey58VgC4jYTMPfaXGrr"
                )
            )
        )

        rekognitionClient.setRegion(Region.getRegion(Regions.AP_SOUTH_1))

        val request = CreateCollectionRequest()
        request.collectionId = MechCardPref.collectionId
        withContext(Dispatchers.IO) {
            try {
                rekognitionClient.createCollection(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showAppInactivityDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val bind = LayoutInactivityBinding.inflate(layoutInflater)
        builder.setView(bind.root)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

        bind.viewMain.setOnClickListener {
            alertDialog.dismiss()
            bindAllCameraUseCases()
            (requireActivity() as MainActivity).resetDisconnectTimer()
        }
    }

    private fun generateAccessToken() {
        apiViewModel.getToken(Constants.DEV_CLIENT_ID, Constants.DEV_SECRET) {
            getAWSConfigData()
        }

    }
}
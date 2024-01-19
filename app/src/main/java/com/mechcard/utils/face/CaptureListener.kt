package com.mechcard.utils.face


import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException
import java.nio.ByteBuffer


/** An interface to process the images with different vision detectors and custom image models.  */
interface CaptureListener {
    fun onFaceDetected(probebility: Double)
}

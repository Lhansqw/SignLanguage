package com.tuapp.signlanguage.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker

/** Resultado: manos (21 puntos normalizados 0..1) + tamaño de la imagen analizada. */
data class HandResult(
    val hands: List<List<PointF>> = emptyList(),
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val prediction: SignPrediction = SignPrediction("Esperando...", 0f)
)

class HandLandmarkerHelper(
    context: Context,
    private val onResult: (HandResult) -> Unit
) {
    private var landmarker: HandLandmarker? = null
    @Volatile private var lastW = 1
    @Volatile private var lastH = 1

    init {
        try {
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
                .setNumHands(2)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, _ ->
                    val hands = result.landmarks().map { hand -> hand.map { PointF(it.x(), it.y()) } }
                    val prediction = if (hands.isNotEmpty()) {
                        SignClassifier.classify(hands[0])
                    } else {
                        SignPrediction("No hay mano", 0f)
                    }
                    onResult(HandResult(hands, lastW, lastH, prediction))
                }
                .build()
            landmarker = HandLandmarker.createFromOptions(context, options)
        } catch (e: Throwable) {
            e.printStackTrace()
            onResult(HandResult(prediction = SignPrediction("Error al cargar IA: ${e.localizedMessage ?: "librerías no encontradas"}", 0f)))
        }
    }

    fun detect(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val lm = landmarker
        if (lm == null) {
            imageProxy.close()
            return
        }
        try {
            val bitmap = imageProxy.toBitmap()
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (isFrontCamera) postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            lastW = rotated.width
            lastH = rotated.height

            lm.detectAsync(BitmapImageBuilder(rotated).build(), SystemClock.uptimeMillis())

            if (rotated != bitmap) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        try {
            landmarker?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

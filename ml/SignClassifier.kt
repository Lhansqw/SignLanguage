package com.tuapp.signlanguage.ml

import android.graphics.PointF
import kotlin.math.hypot

data class SignPrediction(
    val label: String,
    val confidence: Float,
    val description: String = ""
)

object SignClassifier {

    private const val WRIST = 0
    private const val THUMB_CMC = 1
    private const val THUMB_MCP = 2
    private const val THUMB_IP = 3
    private const val THUMB_TIP = 4

    private const val INDEX_MCP = 5
    private const val INDEX_PIP = 6
    private const val INDEX_DIP = 7
    private const val INDEX_TIP = 8

    private const val MIDDLE_MCP = 9
    private const val MIDDLE_PIP = 10
    private const val MIDDLE_DIP = 11
    private const val MIDDLE_TIP = 12

    private const val RING_MCP = 13
    private const val RING_PIP = 14
    private const val RING_DIP = 15
    private const val RING_TIP = 16

    private const val PINKY_MCP = 17
    private const val PINKY_PIP = 18
    private const val PINKY_DIP = 19
    private const val PINKY_TIP = 20

    fun classify(landmarks: List<PointF>): SignPrediction {
        if (landmarks.size < 21) {
            return SignPrediction("Desconocido", 0f, "Puntos insuficientes")
        }

        val wrist = landmarks[WRIST]

        val indexExtended = isFingerExtended(landmarks, INDEX_MCP, INDEX_PIP, INDEX_TIP, wrist)
        val middleExtended = isFingerExtended(landmarks, MIDDLE_MCP, MIDDLE_PIP, MIDDLE_TIP, wrist)
        val ringExtended = isFingerExtended(landmarks, RING_MCP, RING_PIP, RING_TIP, wrist)
        val pinkyExtended = isFingerExtended(landmarks, PINKY_MCP, PINKY_PIP, PINKY_TIP, wrist)
        val thumbExtended = isThumbExtended(landmarks)

        val distThumbIndex = dist(landmarks[THUMB_TIP], landmarks[INDEX_TIP])
        val distIndexMiddle = dist(landmarks[INDEX_TIP], landmarks[MIDDLE_TIP])

        val handScale = dist(wrist, landmarks[MIDDLE_MCP]).coerceAtLeast(0.001f)
        val normThumbIndex = distThumbIndex / handScale

        return when {
            indexExtended && middleExtended && ringExtended && pinkyExtended && thumbExtended -> {
                SignPrediction("Hola / 5", 0.95f, "Palma abierta")
            }

            thumbExtended && indexExtended && !middleExtended && !ringExtended && pinkyExtended -> {
                SignPrediction("Te Quiero 🤟", 0.92f, "Seña ILY")
            }

            thumbExtended && !indexExtended && !middleExtended && !ringExtended && pinkyExtended -> {
                SignPrediction("Y / Llamada 🤙", 0.90f, "Letra Y / Teléfono")
            }

            thumbExtended && indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                SignPrediction("L", 0.93f, "Letra L")
            }

            !thumbExtended && indexExtended && middleExtended && !ringExtended && !pinkyExtended && normThumbIndex > 0.3f -> {
                if (distIndexMiddle / handScale < 0.25f) {
                    SignPrediction("U", 0.90f, "Letra U (dedos juntos)")
                } else {
                    SignPrediction("V", 0.92f, "Letra V (paz / victoria)")
                }
            }

            !thumbExtended && indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                SignPrediction("D / 1", 0.90f, "Letra D / Apuntando")
            }

            !thumbExtended && !indexExtended && !middleExtended && !ringExtended && pinkyExtended -> {
                SignPrediction("I", 0.91f, "Letra I")
            }

            !thumbExtended && indexExtended && middleExtended && ringExtended && pinkyExtended -> {
                SignPrediction("B", 0.89f, "Letra B")
            }

            normThumbIndex < 0.25f && middleExtended && ringExtended && pinkyExtended -> {
                SignPrediction("OK 👌", 0.94f, "Gesto OK / Letra F")
            }

            thumbExtended && !indexExtended && !middleExtended && !ringExtended && !pinkyExtended &&
                    landmarks[THUMB_TIP].y < landmarks[THUMB_MCP].y -> {
                SignPrediction("Bien 👍", 0.92f, "Pulgar arriba")
            }

            isFormingCShape(landmarks, handScale) -> {
                SignPrediction("C", 0.88f, "Letra C")
            }

            normThumbIndex < 0.22f && !indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                SignPrediction("O", 0.89f, "Letra O")
            }

            !indexExtended && !middleExtended && !ringExtended && !pinkyExtended -> {
                if (thumbExtended || landmarks[THUMB_TIP].x > landmarks[INDEX_MCP].x) {
                    SignPrediction("A", 0.90f, "Letra A / Puño con pulgar lateral")
                } else {
                    SignPrediction("E", 0.86f, "Letra E / Puño cerrado")
                }
            }

            else -> {
                SignPrediction("Detectando...", 0.50f, "Analizando seña")
            }
        }
    }

    private fun isFingerExtended(
        landmarks: List<PointF>,
        mcpIdx: Int,
        pipIdx: Int,
        tipIdx: Int,
        wrist: PointF
    ): Boolean {
        val mcp = landmarks[mcpIdx]
        val pip = landmarks[pipIdx]
        val tip = landmarks[tipIdx]

        val distTipWrist = dist(tip, wrist)
        val distPipWrist = dist(pip, wrist)
        val distMcpWrist = dist(mcp, wrist)

        return distTipWrist > distPipWrist && distPipWrist > distMcpWrist
    }

    private fun isThumbExtended(landmarks: List<PointF>): Boolean {
        val wrist = landmarks[WRIST]
        val thumbTip = landmarks[THUMB_TIP]
        val thumbMcp = landmarks[THUMB_MCP]
        val indexMcp = landmarks[INDEX_MCP]

        val distTipWrist = dist(thumbTip, wrist)
        val distMcpWrist = dist(thumbMcp, wrist)

        return distTipWrist > distMcpWrist * 1.2f || dist(thumbTip, indexMcp) > dist(thumbMcp, indexMcp) * 1.3f
    }

    private fun isFormingCShape(landmarks: List<PointF>, handScale: Float): Boolean {
        val thumbTip = landmarks[THUMB_TIP]
        val indexTip = landmarks[INDEX_TIP]
        val d = dist(thumbTip, indexTip) / handScale
        return d in 0.28f..0.65f && !isFingerExtended(landmarks, INDEX_MCP, INDEX_PIP, INDEX_TIP, landmarks[WRIST])
    }

    private fun dist(p1: PointF, p2: PointF): Float {
        return hypot(p1.x - p2.x, p1.y - p2.y)
    }
}

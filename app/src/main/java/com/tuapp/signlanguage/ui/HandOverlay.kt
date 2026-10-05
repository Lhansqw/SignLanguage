package com.tuapp.signlanguage.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.tuapp.signlanguage.ml.HandResult

private val CONNECTIONS = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    5 to 9, 9 to 10, 10 to 11, 11 to 12,
    9 to 13, 13 to 14, 14 to 15, 15 to 16,
    13 to 17, 17 to 18, 18 to 19, 19 to 20,
    0 to 17
)

@Composable
fun HandOverlay(result: HandResult, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val imgW = if (result.imageWidth <= 0) 1 else result.imageWidth
        val imgH = if (result.imageHeight <= 0) 1 else result.imageHeight

        val scale = minOf(size.width / imgW, size.height / imgH)
        val offX = (size.width - imgW * scale) / 2f
        val offY = (size.height - imgH * scale) / 2f

        fun map(x: Float, y: Float) =
            Offset(offX + x * imgW * scale, offY + y * imgH * scale)

        val lineColor = if (result.prediction.confidence > 0.8f) Color.Cyan else Color.Green
        val pointColor = if (result.prediction.confidence > 0.8f) Color.Yellow else Color.Red

        result.hands.forEach { hand ->
            if (hand.size < 21) return@forEach
            CONNECTIONS.forEach { (a, b) ->
                drawLine(lineColor, map(hand[a].x, hand[a].y), map(hand[b].x, hand[b].y), 5f)
            }
            hand.forEach { drawCircle(pointColor, 9f, map(it.x, it.y)) }
        }
    }
}

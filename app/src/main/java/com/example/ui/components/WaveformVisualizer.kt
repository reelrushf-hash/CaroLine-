package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanCore
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .height(28.dp)
    ) {
        val barCount = 36
        val barWidth = 3.5.dp.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
        val centerY = size.height / 2f
        val maxAmplitude = size.height * 0.45f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            // Sine wave calculation
            val wave = sin((progress * 4 * Math.PI + phase).toDouble()).toFloat()

            val baseHeight = 5.dp.toPx()
            val animatedHeight = if (isListening || isSpeaking) {
                val energy = (audioLevel * 1.8f).coerceIn(0.1f, 1f)
                baseHeight + (kotlin.math.abs(wave) * maxAmplitude * energy)
            } else {
                baseHeight + (kotlin.math.abs(wave) * 3.dp.toPx())
            }

            val x = i * (barWidth + spacing)
            val y = centerY - (animatedHeight / 2f)

            val alpha = if (isListening || isSpeaking) {
                (0.6f + (kotlin.math.abs(wave) * 0.4f)).coerceIn(0.4f, 1f)
            } else {
                0.45f
            }

            drawRoundRect(
                color = CyanCore.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = Size(barWidth, animatedHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

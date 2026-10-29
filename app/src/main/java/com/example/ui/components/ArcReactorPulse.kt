package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanCore
import com.example.ui.theme.CyanGlow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorPulse(
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorPulse")

    // Breathing pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Continuous rotation for HUD tick marks
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Dynamic scale influenced by audio energy
    val dynamicAudioBoost = if (isListening || isSpeaking) audioLevel * 0.25f else 0f
    val currentScale = (pulseScale + dynamicAudioBoost).coerceIn(0.85f, 1.4f)

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.92f * currentScale

            // Outer subtle glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CyanGlow.copy(alpha = if (isListening) 0.22f else 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.15f
                ),
                radius = maxRadius * 1.15f,
                center = center
            )

            // Outer ring 1
            drawCircle(
                color = CyanCore.copy(alpha = if (isListening) 0.55f else 0.30f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Ring 2 (Middle concentric ring)
            drawCircle(
                color = CyanBright.copy(alpha = if (isListening) 0.75f else 0.45f),
                radius = maxRadius * 0.72f,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Ring 3 (Inner concentrated ring)
            drawCircle(
                color = CyanCore.copy(alpha = if (isListening) 0.90f else 0.65f),
                radius = maxRadius * 0.44f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Futuristic HUD tick marks along Ring 2
            val tickRadius = maxRadius * 0.72f
            val numTicks = 24
            for (i in 0 until numTicks) {
                val angleDeg = (i * (360f / numTicks) + rotationAngle).toDouble()
                val angleRad = Math.toRadians(angleDeg)
                val isMajorTick = i % 6 == 0
                val tickLen = if (isMajorTick) 8.dp.toPx() else 4.dp.toPx()

                val startX = (center.x + (tickRadius - tickLen / 2) * cos(angleRad)).toFloat()
                val startY = (center.y + (tickRadius - tickLen / 2) * sin(angleRad)).toFloat()
                val endX = (center.x + (tickRadius + tickLen / 2) * cos(angleRad)).toFloat()
                val endY = (center.y + (tickRadius + tickLen / 2) * sin(angleRad)).toFloat()

                drawLine(
                    color = if (isMajorTick) CyanCore else CyanCore.copy(alpha = 0.35f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajorTick) 2.dp.toPx() else 1.2.dp.toPx()
                )
            }

            // Glowing center orb (Arc Reactor spark)
            val coreRadius = (10.dp.toPx() * (1f + dynamicAudioBoost * 1.5f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        CyanCore,
                        CyanCore.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 2.5f
                ),
                radius = coreRadius * 2.5f,
                center = center
            )

            drawCircle(
                color = CyanCore,
                radius = coreRadius,
                center = center
            )

            // Pure white hot center
            drawCircle(
                color = Color.White,
                radius = coreRadius * 0.45f,
                center = center
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    amplitude: Float = 0.5f,
    barCount: Int = 28,
    activeColor: Color = Color(0xFF00E676),
    isActive: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "audio_bars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val totalWidth = size.width
        val barWidth = 4.dp.toPx()
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing) + barWidth / 2f
            val dynamicHeight = if (isActive) {
                val wave = (sin((i * 0.45 + phase).toDouble()).toFloat() + 1f) / 2f
                val h = (wave * 0.6f + amplitude * 0.4f) * size.height * 0.85f
                h.coerceAtLeast(4.dp.toPx())
            } else {
                6.dp.toPx()
            }

            drawLine(
                color = if (isActive) activeColor else Color.Gray.copy(alpha = 0.5f),
                start = Offset(x, centerY - dynamicHeight / 2f),
                end = Offset(x, centerY + dynamicHeight / 2f),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

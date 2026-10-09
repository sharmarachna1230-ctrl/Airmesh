package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeerDevice
import com.example.ui.theme.WhatsAppPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarAnimation(
    modifier: Modifier = Modifier,
    peers: List<PeerDevice> = emptyList(),
    isScanning: Boolean = true,
    onPeerClick: (PeerDevice) -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    // Rotation angle for the scanner line
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse expanding ripple
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = minOf(size.width, size.height) / 2 * 0.92f

            // Concentric range circles (10m, 20m, 30m)
            val rings = 3
            for (i in 1..rings) {
                val r = maxRadius * (i.toFloat() / rings)
                drawCircle(
                    color = Color(0xFF00A884).copy(alpha = 0.2f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Expanding ripple wave
            if (isScanning) {
                drawCircle(
                    color = Color(0xFF25D366).copy(alpha = (1f - pulseProgress) * 0.35f),
                    radius = maxRadius * pulseProgress,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Crosshair lines
            drawLine(
                color = Color(0xFF00A884).copy(alpha = 0.15f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF00A884).copy(alpha = 0.15f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Rotating sweep gradient beam
            if (isScanning) {
                rotate(rotation, pivot = center) {
                    val sweepBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E676).copy(alpha = 0.4f),
                            Color(0xFF00A884).copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxRadius
                    )
                    drawCircle(
                        brush = sweepBrush,
                        radius = maxRadius,
                        center = center
                    )
                    drawLine(
                        color = Color(0xFF00E676).copy(alpha = 0.8f),
                        start = center,
                        end = Offset(center.x, center.y - maxRadius),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }

            // Center beacon dot (You)
            drawCircle(
                color = Color(0xFF00E676),
                radius = 6.dp.toPx(),
                center = center
            )
        }

        // Render interactive peer node blips
        peers.forEachIndexed { index, peer ->
            // Distribute angles deterministically around the circle
            val angle = (index * 72.0 + 40.0) * (Math.PI / 180.0)
            val normalizedDist = (peer.distanceMeters / 15.0).coerceIn(0.25, 0.88)
            val radiusPx = 130.0 * normalizedDist

            val xOffset = (cos(angle) * radiusPx).toInt()
            val yOffset = (sin(angle) * radiusPx).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffset * 2, yOffset * 2) }
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (peer.isConnected) WhatsAppPrimary else Color(0xFF2A3942))
                    .clickable { onPeerClick(peer) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = peer.displayName.take(2).uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

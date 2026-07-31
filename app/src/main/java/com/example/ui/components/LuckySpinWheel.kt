package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PurpleDarkBackground
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuckySpinWheel(
    targetWinningNumber: Int?,
    isSpinning: Boolean,
    onSpinFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(isSpinning, targetWinningNumber) {
        if (isSpinning && targetWinningNumber != null && targetWinningNumber in 1..25) {
            val sectorAngle = 360f / 25f
            // Target angle so top indicator lands on sector corresponding to targetWinningNumber
            val targetSectorCenter = (targetWinningNumber - 1) * sectorAngle + (sectorAngle / 2f)
            val desiredFinalAngle = (360f - targetSectorCenter) % 360f

            val totalRounds = 5 * 360f // 5 full rotations for dramatic physics spin
            val finalTargetAngle = totalRounds + desiredFinalAngle

            rotationAngle.animateTo(
                targetValue = finalTargetAngle,
                animationSpec = tween(
                    durationMillis = 4000,
                    easing = FastOutSlowInEasing
                )
            )
            onSpinFinished()
        }
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Glow Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.2f),
                radius = size.minDimension / 2f + 12f
            )
        }

        // Spinning Wheel sectors
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            val sectorAngle = 360f / 25f

            val sectorColors = listOf(
                Color(0xFF8E24AA), Color(0xFFD81B60), Color(0xFF1E88E5), Color(0xFF00ACC1),
                Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFFE53935),
                Color(0xFF6D4C41), Color(0xFF5E35B1), Color(0xFF00897B), Color(0xFF3949AB),
                Color(0xFFC0CA33), Color(0xFFFFB300), Color(0xFFF4511E), Color(0xFF757575),
                Color(0xFF8E24AA), Color(0xFFD81B60), Color(0xFF1E88E5), Color(0xFF00ACC1),
                Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFFE53935),
                Color(0xFF5E35B1)
            )

            rotate(rotationAngle.value, center) {
                for (i in 0 until 25) {
                    val startAngle = i * sectorAngle
                    val color = sectorColors[i % sectorColors.size]

                    // Sector Arc
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Sector Border
                    drawArc(
                        color = Color(0x66FFFFFF),
                        startAngle = startAngle,
                        sweepAngle = sectorAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 1.5f)
                    )

                    // Sector Number Text
                    val textAngleRad = Math.toRadians((startAngle + sectorAngle / 2f).toDouble())
                    val textRadius = radius * 0.72f
                    val textX = (center.x + textRadius * cos(textAngleRad)).toFloat()
                    val textY = (center.y + textRadius * sin(textAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.drawText(
                        "${i + 1}",
                        textX,
                        textY + 8f,
                        android.graphics.Paint().apply {
                            setColor(android.graphics.Color.WHITE)
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                        }
                    )
                }

                // Outer Gold Ring
                drawCircle(
                    color = GoldPrimary,
                    radius = radius,
                    style = Stroke(width = 8f)
                )

                // Perimeter LED Bulbs
                for (i in 0 until 25) {
                    val bulbAngleRad = Math.toRadians((i * sectorAngle).toDouble())
                    val bulbRadius = radius - 4f
                    val bulbX = (center.x + bulbRadius * cos(bulbAngleRad)).toFloat()
                    val bulbY = (center.y + bulbRadius * sin(bulbAngleRad)).toFloat()

                    drawCircle(
                        color = if (i % 2 == 0) GoldAccent else Color.White,
                        radius = 4f,
                        center = Offset(bulbX, bulbY)
                    )
                }
            }
        }

        // Wheel Center Golden Hub
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GoldPrimary)
                .border(3.dp, PurpleDarkBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SPIN",
                fontWeight = FontWeight.Black,
                color = PurpleDarkBackground,
                fontSize = 14.sp
            )
        }

        // Top Pointer Indicator (Golden Triangle)
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(28.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, color = GoldPrimary)
            drawPath(path, color = Color.White, style = Stroke(width = 2f))
        }
    }
}

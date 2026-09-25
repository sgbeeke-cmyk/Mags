package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerBand
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraVioletSecondary

@Composable
fun EqualizerCurveView(
    bands: List<EqualizerBand>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("equalizer_curve_container")
            .fillMaxWidth()
            .height(180.dp)
            .border(1.dp, AuraDarkSurfaceVariant, RoundedCornerShape(16.dp))
            .background(AuraDarkSurface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            // Reference grid lines: +12dB, 0dB, -12dB
            val topGridY = height * 0.15f
            val bottomGridY = height * 0.85f

            // 0 dB center reference line
            drawLine(
                color = Color(0xFF263042),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.dp.toPx()
            )

            // +12dB line (dashed/faint)
            drawLine(
                color = Color(0xFF1B2332),
                start = Offset(0f, topGridY),
                end = Offset(width, topGridY),
                strokeWidth = 0.5.dp.toPx()
            )

            // -12dB line
            drawLine(
                color = Color(0xFF1B2332),
                start = Offset(0f, bottomGridY),
                end = Offset(width, bottomGridY),
                strokeWidth = 0.5.dp.toPx()
            )

            if (bands.isEmpty()) return@Canvas

            // Compute points (x, y) for each band
            // gainDb range is typically -12dB to +12dB
            val points = bands.mapIndexed { index, band ->
                val x = if (bands.size > 1) {
                    (index.toFloat() / (bands.size - 1)) * (width - 40f) + 20f
                } else {
                    width / 2f
                }
                // map gainDb [-12..12] to [bottomGridY..topGridY]
                val normalizedGain = (band.gainDb.coerceIn(-12f, 12f) + 12f) / 24f // 0..1
                val y = bottomGridY - (normalizedGain * (bottomGridY - topGridY))
                Offset(x, y)
            }

            // Build smooth Bezier path
            val curvePath = Path()
            val fillPath = Path()

            curvePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y

                curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw glowing gradient fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AuraCyanPrimary.copy(alpha = 0.25f),
                        AuraVioletSecondary.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )

            // Draw smooth response line
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(AuraCyanPrimary, AuraVioletSecondary)
                ),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw band point handles
            points.forEachIndexed { idx, point ->
                // Outer glow dot
                drawCircle(
                    color = AuraCyanPrimary.copy(alpha = 0.35f),
                    radius = 7.dp.toPx(),
                    center = point
                )
                // Center solid dot
                drawCircle(
                    color = if (bands[idx].gainDb > 0) AuraCyanPrimary else if (bands[idx].gainDb < 0) AuraVioletSecondary else Color.White,
                    radius = 3.5.dp.toPx(),
                    center = point
                )
            }
        }

        // dB labels on side
        Text(
            text = "+12 dB",
            fontSize = 9.sp,
            color = Color(0xFF6B7A90),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Text(
            text = "0 dB",
            fontSize = 9.sp,
            color = Color(0xFF4C586B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "-12 dB",
            fontSize = 9.sp,
            color = Color(0xFF6B7A90),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

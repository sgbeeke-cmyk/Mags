package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary
import com.example.visualizer.VisualizerFrame
import com.example.visualizer.VisualizerStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact visualizer used in list items and MiniPlayer.
 * Reacts to live VisualizerFrame magnitudes if provided, or smooth fallback animation.
 */
@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 3.dp,
    visualizerFrame: VisualizerFrame? = null
) {
    if (visualizerFrame != null && visualizerFrame.magnitudes.isNotEmpty()) {
        val frameMags = visualizerFrame.magnitudes
        Row(
            modifier = modifier.testTag("audio_visualizer_compact"),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val step = (frameMags.size / barCount).coerceAtLeast(1)
            for (i in 0 until barCount) {
                val mag = if (isPlaying) {
                    val raw = frameMags.getOrElse(i * step) { 0.2f }
                    raw.coerceIn(0.12f, 1.0f)
                } else {
                    0.1f
                }
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(maxHeight * mag)
                        .background(
                            brush = Brush.verticalGradient(listOf(AuraCyanPrimary, AuraVioletSecondary)),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    } else {
        // Fallback smooth transition
        val transition = rememberInfiniteTransition(label = "visualizer")
        val h1 by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 0.95f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h1"
        )
        val h2 by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = if (isPlaying) 1.0f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 560, delayMillis = 100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h2"
        )
        val h3 by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = if (isPlaying) 0.85f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, delayMillis = 50, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h3"
        )
        val h4 by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = if (isPlaying) 0.75f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 480, delayMillis = 150, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h4"
        )

        val heights = listOf(h1, h2, h3, h4)

        Row(
            modifier = modifier.testTag("audio_visualizer_compact"),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            heights.take(barCount).forEach { factor ->
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(maxHeight * factor.coerceIn(0.12f, 1.0f))
                        .background(
                            brush = Brush.verticalGradient(listOf(AuraCyanPrimary, AuraVioletSecondary)),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * Full-scale, high-fidelity Audio Visualizer Canvas for PlayerBottomSheet and Equalizer screen.
 * Supports multiple rendering styles: Spectrum Bars, Waveform Oscilloscope, Radial/Circular, and Peak Spectrum.
 */
@Composable
fun RealtimeVisualizerCanvas(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    style: VisualizerStyle = VisualizerStyle.BARS,
    primaryColor: Color = AuraCyanPrimary,
    secondaryColor: Color = AuraVioletSecondary,
    showGlow: Boolean = true
) {
    Canvas(
        modifier = modifier
            .testTag("realtime_visualizer_canvas")
            .fillMaxSize()
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (style) {
            VisualizerStyle.BARS -> drawSpectrumBars(frame, isPlaying, w, h, primaryColor, secondaryColor)
            VisualizerStyle.WAVEFORM -> drawOscilloscopeWaveform(frame, isPlaying, w, h, primaryColor, secondaryColor)
            VisualizerStyle.CIRCULAR -> drawRadialVisualizer(frame, isPlaying, w, h, primaryColor, secondaryColor)
            VisualizerStyle.SPECTRUM_PEAKS -> drawPeakSpectrum(frame, isPlaying, w, h, primaryColor, secondaryColor)
        }
    }
}

private fun DrawScope.drawSpectrumBars(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val mags = frame.magnitudes
    val barCount = if (mags.isNotEmpty()) mags.size.coerceAtMost(32) else 32
    val totalSpacing = (barCount - 1) * 4.dp.toPx()
    val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
    val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

    for (i in 0 until barCount) {
        val rawMag = if (isPlaying && mags.isNotEmpty()) mags.getOrElse(i) { 0.05f } else 0.02f
        val clampedMag = rawMag.coerceIn(0.04f, 1.0f)
        val barHeight = (height * clampedMag).coerceAtLeast(4.dp.toPx())
        val x = i * (barWidth + 4.dp.toPx())
        val y = height - barHeight

        val colorFraction = i.toFloat() / barCount
        val barBrush = Brush.verticalGradient(
            colors = listOf(
                secondaryColor.copy(alpha = 0.95f),
                primaryColor.copy(alpha = 0.85f)
            ),
            startY = y,
            endY = height
        )

        drawRoundRect(
            brush = barBrush,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = cornerRadius
        )

        // Draw glowing cap on top of each active bar
        if (clampedMag > 0.15f) {
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = (barWidth / 2.5f).coerceAtMost(3.dp.toPx()),
                center = Offset(x + barWidth / 2f, y + 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawOscilloscopeWaveform(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val wave = frame.waveform
    val centerY = height / 2f

    val path = Path()
    val fillPath = Path()
    fillPath.moveTo(0f, centerY)

    val points = if (wave.isNotEmpty()) wave.size else 64
    val step = width / (points - 1).coerceAtLeast(1)

    for (i in 0 until points) {
        val rawVal = if (isPlaying && wave.isNotEmpty()) wave.getOrElse(i) { 0f } else 0f
        val x = i * step
        val y = centerY - (rawVal * (height * 0.44f))

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            val prevX = (i - 1) * step
            val prevVal = if (isPlaying && wave.isNotEmpty()) wave.getOrElse(i - 1) { 0f } else 0f
            val prevY = centerY - (prevVal * (height * 0.44f))
            val cX = (prevX + x) / 2f
            path.quadraticTo(prevX, prevY, cX, (prevY + y) / 2f)
        }
        fillPath.lineTo(x, y)
    }
    fillPath.lineTo(width, centerY)
    fillPath.close()

    // Semi-transparent gradient fill
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.25f),
                Color.Transparent
            ),
            startY = 0f,
            endY = height
        )
    )

    // Glowing main stroke
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, primaryColor)
        ),
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Center zero-crossing guideline
    drawLine(
        color = AuraDarkBorder.copy(alpha = 0.5f),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawRadialVisualizer(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val centerX = width / 2f
    val centerY = height / 2f
    val baseRadius = (minOf(width, height) * 0.22f).coerceAtLeast(20.dp.toPx())
    val maxBarLength = (minOf(width, height) * 0.24f).coerceAtLeast(20.dp.toPx())

    val mags = frame.magnitudes
    val count = if (mags.isNotEmpty()) mags.size.coerceAtMost(32) else 32
    val angleStep = (2 * PI / count).toFloat()

    // Base pulsing center circle
    val rms = if (isPlaying) frame.rmsLevel.coerceIn(0f, 1f) else 0f
    val centerPulseRadius = baseRadius * (1f + rms * 0.18f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = centerPulseRadius * 1.5f
        ),
        radius = centerPulseRadius * 1.5f,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = AuraDarkSurfaceVariant,
        radius = centerPulseRadius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2.dp.toPx())
    )

    for (i in 0 until count) {
        val angle = i * angleStep - (PI / 2).toFloat()
        val mag = if (isPlaying && mags.isNotEmpty()) mags.getOrElse(i) { 0.05f } else 0.04f
        val barLen = (maxBarLength * mag.coerceIn(0.08f, 1f))

        val startX = centerX + cos(angle) * centerPulseRadius
        val startY = centerY + sin(angle) * centerPulseRadius
        val endX = centerX + cos(angle) * (centerPulseRadius + barLen)
        val endY = centerY + sin(angle) * (centerPulseRadius + barLen)

        val frac = i.toFloat() / count
        val strokeColor = if (frac < 0.5f) {
            primaryColor
        } else {
            secondaryColor
        }

        drawLine(
            color = strokeColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Radial tip dot
        if (mag > 0.2f) {
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.dp.toPx(),
                center = Offset(endX, endY)
            )
        }
    }
}

private fun DrawScope.drawPeakSpectrum(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val mags = frame.magnitudes
    val barCount = if (mags.isNotEmpty()) mags.size.coerceAtMost(32) else 32
    val totalSpacing = (barCount - 1) * 3.dp.toPx()
    val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

    for (i in 0 until barCount) {
        val rawMag = if (isPlaying && mags.isNotEmpty()) mags.getOrElse(i) { 0.05f } else 0.03f
        val clampedMag = rawMag.coerceIn(0.04f, 1.0f)
        val barHeight = (height * clampedMag).coerceAtLeast(3.dp.toPx())
        val x = i * (barWidth + 3.dp.toPx())
        val y = height - barHeight

        // Segmented LED look
        val segmentCount = 12
        val segmentHeight = (height - (segmentCount * 2.dp.toPx())) / segmentCount
        val activeSegments = (clampedMag * segmentCount).toInt().coerceIn(1, segmentCount)

        for (s in 0 until activeSegments) {
            val segY = height - ((s + 1) * (segmentHeight + 2.dp.toPx()))
            val segFrac = s.toFloat() / segmentCount
            val segColor = when {
                segFrac > 0.82f -> Color(0xFFFF5252) // Peak Red
                segFrac > 0.65f -> Color(0xFFFFD600) // High Yellow
                else -> primaryColor // Cyan
            }
            drawRoundRect(
                color = segColor,
                topLeft = Offset(x, segY),
                size = Size(barWidth, segmentHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

/**
 * Interactive Visualizer Panel with real-time hardware status, style switcher chips,
 * and optional permission grant prompt for Media3 AudioSessionId synchronization.
 */
@Composable
fun RealtimeVisualizerControlPanel(
    frame: VisualizerFrame,
    isPlaying: Boolean,
    hasRecordPermission: Boolean,
    onRequestPermission: () -> Unit,
    selectedStyle: VisualizerStyle,
    onSelectStyle: (VisualizerStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("realtime_visualizer_panel")
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuraDarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Status indicator & hardware lock badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) AuraCyanPrimary else AuraTextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REAL-TIME SPECTRUM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AuraTextPrimary
                    )
                }

                // Hardware vs Reactive Synthesizer badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (frame.isHardwareSynced) AuraCyanPrimary.copy(alpha = 0.15f) else Color(0x33FFA726))
                        .border(
                            1.dp,
                            if (frame.isHardwareSynced) AuraCyanPrimary.copy(alpha = 0.5f) else Color(0x66FFA726),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (!hasRecordPermission) {
                                onRequestPermission()
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = if (frame.isHardwareSynced) Icons.Default.GraphicEq else Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (frame.isHardwareSynced) AuraCyanPrimary else Color(0xFFFFA726),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (frame.isHardwareSynced) "AudioSession Sync" else if (hasRecordPermission) "DSP Filtered" else "Tap for Hardware Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (frame.isHardwareSynced) AuraCyanPrimary else Color(0xFFFFA726)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Visualizer Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F141E))
                    .border(1.dp, Color(0xFF1E2535), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                RealtimeVisualizerCanvas(
                    frame = frame,
                    isPlaying = isPlaying,
                    style = selectedStyle,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Style Switcher Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VisualizerStyle.values().forEach { style ->
                    val isSelected = style == selectedStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.2f) else AuraDarkCard)
                            .border(
                                1.dp,
                                if (isSelected) AuraCyanPrimary.copy(alpha = 0.6f) else AuraDarkBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectStyle(style) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AuraCyanPrimary else AuraTextSecondary
                        )
                    }
                }
            }
        }
    }
}

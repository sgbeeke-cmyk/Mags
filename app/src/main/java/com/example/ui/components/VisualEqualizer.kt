package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerBand
import com.example.data.model.EqualizerPreset
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

/**
 * Visual Equalizer UI Component for Media3 ExoPlayer audio session.
 * Features:
 * - Real-time continuous frequency response curve with spline interpolation
 * - 5 adjustable frequency bands: 60Hz, 250Hz, 1kHz, 4kHz, 16kHz
 * - Live Media3 ExoPlayer audio session attachment status
 * - Interactive vertical channel strip faders with double-tap to reset to 0dB
 * - One-tap flat reset & audio presets selector
 */
@Composable
fun VisualEqualizer(
    bands: List<EqualizerBand>,
    isEnabled: Boolean,
    audioSessionId: Int,
    onBandGainChange: (Int, Float) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onResetToFlat: () -> Unit,
    currentPresetName: String = "Custom",
    presets: List<EqualizerPreset> = emptyList(),
    onApplyPreset: ((EqualizerPreset) -> Unit)? = null,
    visualizerFrame: VisualizerFrame? = null,
    isPlaying: Boolean = false,
    bassBoost: Int = 0,
    onBassBoostChange: ((Int) -> Unit)? = null,
    virtualizer: Int = 0,
    onVirtualizerChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Canonical 5 frequency definitions to ensure 60Hz, 250Hz, 1kHz, 4kHz, 16kHz
    val canonicalBands = remember(bands) {
        val defaultFreqs = listOf(60, 250, 1000, 4000, 16000)
        defaultFreqs.mapIndexed { index, freq ->
            val existing = bands.getOrNull(index)
            EqualizerBand(
                index = index,
                centerFreqHz = freq,
                gainDb = existing?.gainDb ?: 0f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "eqPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eqPulseAlpha"
    )

    Card(
        modifier = modifier
            .testTag("visual_equalizer_component")
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
        border = BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Media3 Audio Session Status & Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuraDarkSurface)
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    color = if (isEnabled && audioSessionId != 0) {
                                        AuraCyanPrimary.copy(alpha = if (isPlaying) pulseAlpha else 0.8f)
                                    } else {
                                        Color(0xFF6B7280)
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (audioSessionId != 0) {
                                "MEDIA3 SESSION #$audioSessionId • ACTIVE"
                            } else {
                                "MEDIA3 EXOPLAYER SESSION"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled && audioSessionId != 0) AuraCyanPrimary else AuraTextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "5-Band Parametric Equalizer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset to Flat Button
                    Box(
                        modifier = Modifier
                            .testTag("eq_reset_flat_button")
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuraDarkSurface)
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(8.dp))
                            .clickable { onResetToFlat() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset EQ to Flat",
                                tint = AuraTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Flat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Master DSP On/Off Switch
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled,
                        modifier = Modifier.testTag("eq_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AuraCyanPrimary,
                            checkedTrackColor = AuraCyanPrimary.copy(alpha = 0.35f),
                            uncheckedThumbColor = AuraTextMuted,
                            uncheckedTrackColor = AuraDarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Frequency Response Curve Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AuraDarkSurface)
                    .border(1.dp, AuraDarkBorder, RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .testTag("visual_equalizer_curve_canvas")
                        .fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val midY = height / 2f
                    val topGridY = height * 0.15f
                    val bottomGridY = height * 0.85f

                    // Subtle background grid lines (+12dB, 0dB, -12dB)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    // +12dB
                    drawLine(
                        color = Color(0xFF1E2838),
                        start = Offset(0f, topGridY),
                        end = Offset(width, topGridY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // 0dB Center Reference Line
                    drawLine(
                        color = Color(0xFF28364C),
                        start = Offset(0f, midY),
                        end = Offset(width, midY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // -12dB
                    drawLine(
                        color = Color(0xFF1E2838),
                        start = Offset(0f, bottomGridY),
                        end = Offset(width, bottomGridY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // Optional live audio visualizer underlay
                    if (isPlaying && visualizerFrame != null && isEnabled) {
                        val fft = visualizerFrame.magnitudes
                        if (fft.isNotEmpty()) {
                            val maxBars = fft.size.coerceAtMost(32)
                            val barWidth = width / maxBars
                            for (i in 0 until maxBars) {
                                val mag = fft[i].coerceIn(0f, 1f)
                                val barHeight = mag * (height * 0.45f)
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            AuraCyanPrimary.copy(alpha = 0.18f),
                                            Color.Transparent
                                        )
                                    ),
                                    topLeft = Offset(i * barWidth, height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth * 0.7f, barHeight)
                                )
                            }
                        }
                    }

                    // Map 5 canonical bands across the width
                    val bandCount = canonicalBands.size
                    val points = canonicalBands.mapIndexed { index, band ->
                        val x = if (bandCount > 1) {
                            val margin = 30f
                            margin + (index.toFloat() / (bandCount - 1)) * (width - 2 * margin)
                        } else {
                            width / 2f
                        }
                        val gainClamped = if (isEnabled) band.gainDb.coerceIn(-12f, 12f) else 0f
                        val normalized = (gainClamped + 12f) / 24f // 0f (-12dB) to 1f (+12dB)
                        val y = bottomGridY - (normalized * (bottomGridY - topGridY))
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        val curvePath = Path()
                        val fillPath = Path()

                        curvePath.moveTo(0f, if (isEnabled) points.first().y else midY)
                        fillPath.moveTo(0f, height)
                        fillPath.lineTo(0f, if (isEnabled) points.first().y else midY)

                        var prevPoint = Offset(0f, if (isEnabled) points.first().y else midY)
                        for (p in points) {
                            val cpX1 = prevPoint.x + (p.x - prevPoint.x) / 2f
                            val cpY1 = prevPoint.y
                            val cpX2 = prevPoint.x + (p.x - prevPoint.x) / 2f
                            val cpY2 = p.y
                            curvePath.cubicTo(cpX1, cpY1, cpX2, cpY2, p.x, p.y)
                            fillPath.cubicTo(cpX1, cpY1, cpX2, cpY2, p.x, p.y)
                            prevPoint = p
                        }

                        // Extend smoothly to right edge
                        val lastPoint = points.last()
                        val endY = if (isEnabled) lastPoint.y else midY
                        curvePath.cubicTo(
                            lastPoint.x + (width - lastPoint.x) / 2f, lastPoint.y,
                            lastPoint.x + (width - lastPoint.x) / 2f, endY,
                            width, endY
                        )
                        fillPath.cubicTo(
                            lastPoint.x + (width - lastPoint.x) / 2f, lastPoint.y,
                            lastPoint.x + (width - lastPoint.x) / 2f, endY,
                            width, endY
                        )
                        fillPath.lineTo(width, height)
                        fillPath.close()

                        // Gradient fill beneath curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    if (isEnabled) AuraCyanPrimary.copy(alpha = 0.35f) else Color.Gray.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                startY = topGridY,
                                endY = height
                            )
                        )

                        // Main curve line
                        drawPath(
                            path = curvePath,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    if (isEnabled) AuraCyanPrimary else Color.Gray,
                                    if (isEnabled) AuraVioletSecondary else Color.DarkGray
                                )
                            ),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Neon control nodes at each frequency point
                        points.forEachIndexed { i, p ->
                            // Outer glow
                            if (isEnabled) {
                                drawCircle(
                                    color = AuraCyanPrimary.copy(alpha = 0.25f),
                                    radius = 9.dp.toPx(),
                                    center = p
                                )
                            }
                            // Inner node
                            drawCircle(
                                color = if (isEnabled) AuraCyanPrimary else Color.Gray,
                                radius = 4.5.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }

                // dB scale labels on the left edge
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("+12 dB", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AuraTextMuted)
                    Text("0 dB", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AuraCyanPrimary.copy(alpha = 0.7f))
                    Text("-12 dB", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AuraTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5 Adjustable Frequency Fader Strips: 60Hz, 250Hz, 1kHz, 4kHz, 16kHz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("eq_frequency_bands_row"),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val bandRoles = listOf("SUB", "BASS", "MID", "PRESENCE", "AIR")
                val bandLabels = listOf("60Hz", "250Hz", "1kHz", "4kHz", "16kHz")

                canonicalBands.forEachIndexed { index, band ->
                    EqualizerFaderStrip(
                        bandIndex = index,
                        freqLabel = bandLabels.getOrElse(index) { "${band.centerFreqHz}Hz" },
                        roleLabel = bandRoles.getOrElse(index) { "DSP" },
                        gainDb = band.gainDb,
                        isEnabled = isEnabled,
                        onGainChange = { newGain -> onBandGainChange(index, newGain) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Presets Horizontal Row (if provided)
            if (presets.isNotEmpty() && onApplyPreset != null) {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "ACOUSTIC PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AuraTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presetScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(presetScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = currentPresetName == preset.name
                        Box(
                            modifier = Modifier
                                .testTag("eq_preset_${preset.name}")
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.15f) else AuraDarkSurface)
                                .clickable { onApplyPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AuraCyanPrimary else AuraTextSecondary
                            )
                        }
                    }
                }
            }

            // Companion DSP Controls: Bass Boost & Soundstage Virtualizer
            if (onBassBoostChange != null || onVirtualizerChange != null) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onBassBoostChange != null) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                            border = BorderStroke(1.dp, AuraDarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bass Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AuraTextPrimary)
                                    Text("${bassBoost / 10}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AuraCyanPrimary)
                                }
                                Slider(
                                    value = bassBoost.toFloat(),
                                    onValueChange = { onBassBoostChange(it.toInt()) },
                                    valueRange = 0f..1000f,
                                    enabled = isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AuraCyanPrimary,
                                        activeTrackColor = AuraCyanPrimary,
                                        inactiveTrackColor = AuraDarkSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    if (onVirtualizerChange != null) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                            border = BorderStroke(1.dp, AuraDarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Soundstage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AuraTextPrimary)
                                    Text("${virtualizer / 10}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AuraVioletSecondary)
                                }
                                Slider(
                                    value = virtualizer.toFloat(),
                                    onValueChange = { onVirtualizerChange(it.toInt()) },
                                    valueRange = 0f..1000f,
                                    enabled = isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AuraVioletSecondary,
                                        activeTrackColor = AuraVioletSecondary,
                                        inactiveTrackColor = AuraDarkSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Studio-grade Channel Strip Fader for a single frequency band.
 */
@Composable
private fun EqualizerFaderStrip(
    bandIndex: Int,
    freqLabel: String,
    roleLabel: String,
    gainDb: Float,
    isEnabled: Boolean,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("fader_strip_$bandIndex")
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain Readout badge
        Text(
            text = String.format("%+.1f", gainDb),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = when {
                !isEnabled -> AuraTextMuted
                gainDb > 0.05f -> AuraCyanPrimary
                gainDb < -0.05f -> AuraVioletSecondary
                else -> AuraTextSecondary
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // +1dB Nudge Button
        IconButton(
            onClick = { onGainChange((gainDb + 1.0f).coerceAtMost(12f)) },
            enabled = isEnabled,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase $freqLabel by 1dB",
                tint = if (isEnabled) AuraTextMuted else Color.DarkGray,
                modifier = Modifier.size(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Vertical Fader Track (130dp height, 32dp width)
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraDarkSurface)
                .border(1.dp, AuraDarkBorder, RoundedCornerShape(10.dp))
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectTapGestures(
                        onDoubleTap = {
                            // Double-tap resets this specific band to 0.0 dB (flat)
                            onGainChange(0f)
                        },
                        onTap = { offset ->
                            val height = size.height
                            val fraction = 1f - (offset.y / height).coerceIn(0f, 1f)
                            val targetGain = (fraction * 24f - 12f).coerceIn(-12f, 12f)
                            // Round to nearest 0.5dB
                            val rounded = kotlin.math.round(targetGain * 2f) / 2f
                            onGainChange(rounded)
                        }
                    )
                }
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val height = size.height
                        val fraction = 1f - (change.position.y / height).coerceIn(0f, 1f)
                        val targetGain = (fraction * 24f - 12f).coerceIn(-12f, 12f)
                        val rounded = kotlin.math.round(targetGain * 2f) / 2f
                        onGainChange(rounded)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val normalizedGain = ((gainDb.coerceIn(-12f, 12f) + 12f) / 24f).coerceIn(0f, 1f)

            // Center 0dB indicator dash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF324056))
            )

            // Vertical Track Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1B2332))
            )

            // Active Track Fill from center 0dB
            val centerFraction = 0.5f
            if (kotlin.math.abs(normalizedGain - centerFraction) > 0.01f) {
                val fillTop = if (normalizedGain > centerFraction) 1f - normalizedGain else 0.5f
                val fillHeight = kotlin.math.abs(normalizedGain - centerFraction)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((110 * fillHeight).dp)
                        .align(if (normalizedGain > centerFraction) Alignment.TopCenter else Alignment.BottomCenter)
                        .padding(
                            top = if (normalizedGain > centerFraction) (110 * fillTop).dp else 0.dp,
                            bottom = if (normalizedGain <= centerFraction) (110 * normalizedGain).dp else 0.dp
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                if (gainDb >= 0) listOf(AuraCyanPrimary, AuraCyanPrimary.copy(alpha = 0.4f))
                                else listOf(AuraVioletSecondary.copy(alpha = 0.4f), AuraVioletSecondary)
                            )
                        )
                )
            }

            // Floating Fader Thumb Knob
            val thumbYOffset = ((1f - normalizedGain) * 104).dp
            Box(
                modifier = Modifier
                    .padding(top = thumbYOffset)
                    .align(Alignment.TopCenter)
                    .size(width = 28.dp, height = 18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shadow(4.dp, RoundedCornerShape(6.dp), spotColor = if (isEnabled) AuraCyanPrimary else Color.Transparent)
                    .background(
                        if (isEnabled) {
                            Brush.verticalGradient(listOf(Color(0xFF2C394F), Color(0xFF1A2230)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF1E2533), Color(0xFF151A24)))
                        }
                    )
                    .border(
                        1.dp,
                        if (isEnabled) AuraCyanPrimary.copy(alpha = 0.8f) else Color.DarkGray,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glow line on the thumb knob
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.dp)
                        .background(if (isEnabled) AuraCyanPrimary else Color.Gray, RoundedCornerShape(1.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // -1dB Nudge Button
        IconButton(
            onClick = { onGainChange((gainDb - 1.0f).coerceAtLeast(-12f)) },
            enabled = isEnabled,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease $freqLabel by 1dB",
                tint = if (isEnabled) AuraTextMuted else Color.DarkGray,
                modifier = Modifier.size(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Frequency Label Pill (e.g. 60Hz, 250Hz, 1kHz, 4kHz, 16kHz)
        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) AuraTextPrimary else AuraTextMuted
        )

        // Frequency Band Role Label (e.g. SUB, BASS, MID, PRESENCE, AIR)
        Text(
            text = roleLabel,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            color = AuraTextMuted,
            letterSpacing = 0.5.sp
        )
    }
}

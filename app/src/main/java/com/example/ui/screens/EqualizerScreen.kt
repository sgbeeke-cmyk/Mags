package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerBand
import com.example.data.model.EqualizerPreset
import com.example.ui.components.EqualizerCurveView
import com.example.ui.components.RealtimeVisualizerControlPanel
import com.example.visualizer.VisualizerFrame
import com.example.visualizer.VisualizerStyle
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

@Composable
fun EqualizerScreen(
    bands: List<EqualizerBand>,
    bassBoost: Int,
    virtualizer: Int,
    isEnabled: Boolean,
    currentPresetName: String,
    presets: List<EqualizerPreset>,
    isGaplessEnabled: Boolean,
    onToggleFxEnabled: (Boolean) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onApplyPreset: (EqualizerPreset) -> Unit,
    onToggleGapless: (Boolean) -> Unit,
    visualizerFrame: VisualizerFrame = VisualizerFrame(),
    isPlaying: Boolean = false,
    hasRecordPermission: Boolean = false,
    onRequestRecordPermission: () -> Unit = {},
    visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    onSelectVisualizerStyle: (VisualizerStyle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .testTag("equalizer_screen")
            .fillMaxSize()
            .background(AuraDarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Screen Title & Master Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Acoustic Equalizer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
                Text(
                    text = "Parametric DSP & Soundstage Enhancer",
                    fontSize = 13.sp,
                    color = AuraTextSecondary
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleFxEnabled,
                modifier = Modifier.testTag("eq_master_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AuraCyanPrimary,
                    checkedTrackColor = AuraCyanPrimary.copy(alpha = 0.35f),
                    uncheckedThumbColor = AuraTextMuted,
                    uncheckedTrackColor = AuraDarkSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency Curve Visualizer
        EqualizerCurveView(
            bands = bands,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Real-time Audio Visualizer synced with Media3 AudioSessionId
        RealtimeVisualizerControlPanel(
            frame = visualizerFrame,
            isPlaying = isPlaying,
            hasRecordPermission = hasRecordPermission,
            onRequestPermission = onRequestRecordPermission,
            selectedStyle = visualizerStyle,
            onSelectStyle = onSelectVisualizerStyle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Presets Horizontal Row
        Text(
            text = "AUDIO PRESETS",
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
                        .testTag("preset_${preset.name}")
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.15f) else AuraDarkCard)
                        .clickable { onApplyPreset(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AuraCyanPrimary else AuraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Multi-Band Sliders Section
        Text(
            text = "FREQUENCY BANDS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = AuraTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                bands.forEach { band ->
                    val freqLabel = when {
                        band.centerFreqHz >= 1000 -> "${band.centerFreqHz / 1000} kHz"
                        else -> "${band.centerFreqHz} Hz"
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freqLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = String.format("%+.1f dB", band.gainDb),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (band.gainDb > 0) AuraCyanPrimary else if (band.gainDb < 0) AuraVioletSecondary else AuraTextSecondary
                            )
                        }

                        Slider(
                            value = band.gainDb,
                            onValueChange = { onBandGainChange(band.index, it) },
                            valueRange = -12f..12f,
                            steps = 47, // 0.5 dB steps
                            enabled = isEnabled,
                            modifier = Modifier
                                .testTag("slider_band_${band.index}")
                                .fillMaxWidth()
                                .height(28.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = AuraCyanPrimary,
                                activeTrackColor = AuraCyanPrimary,
                                inactiveTrackColor = AuraDarkSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bass Boost & Virtualizer Soundstage
        Text(
            text = "DSP SOUNDSTAGE ENHANCEMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = AuraTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bass Boost Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Waves,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bass Boost",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(bassBoost / 10)}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraCyanPrimary
                    )
                    Slider(
                        value = bassBoost.toFloat(),
                        onValueChange = { onBassBoostChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEnabled,
                        modifier = Modifier.testTag("bass_boost_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = AuraCyanPrimary,
                            activeTrackColor = AuraCyanPrimary,
                            inactiveTrackColor = AuraDarkSurfaceVariant
                        )
                    )
                }
            }

            // Virtualizer 3D Soundstage Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AuraVioletSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3D Staging",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(virtualizer / 10)}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraVioletSecondary
                    )
                    Slider(
                        value = virtualizer.toFloat(),
                        onValueChange = { onVirtualizerChange(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEnabled,
                        modifier = Modifier.testTag("virtualizer_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = AuraVioletSecondary,
                            activeTrackColor = AuraVioletSecondary,
                            inactiveTrackColor = AuraDarkSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seamless Gapless Playback Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seamless Gapless Playback",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Eliminates silence between consecutive tracks with sample-accurate audio buffer chaining.",
                        fontSize = 12.sp,
                        color = AuraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isGaplessEnabled,
                    onCheckedChange = onToggleGapless,
                    modifier = Modifier.testTag("gapless_playback_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AuraCyanPrimary,
                        checkedTrackColor = AuraCyanPrimary.copy(alpha = 0.35f),
                        uncheckedThumbColor = AuraTextMuted,
                        uncheckedTrackColor = AuraDarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

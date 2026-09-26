package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerBand
import com.example.data.model.EqualizerPreset
import com.example.ui.components.RealtimeVisualizerControlPanel
import com.example.ui.components.VisualEqualizer
import com.example.visualizer.VisualizerFrame
import com.example.visualizer.VisualizerStyle
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

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
    audioSessionId: Int = 0,
    onResetToFlat: () -> Unit = {},
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
        // Screen Header
        Text(
            text = "Lossless Audio DSP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextPrimary
        )
        Text(
            text = "Parametric 5-Band Equalizer for Media3 ExoPlayer",
            fontSize = 13.sp,
            color = AuraTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Equalizer UI Component (60Hz, 250Hz, 1kHz, 4kHz, 16kHz)
        VisualEqualizer(
            bands = bands,
            isEnabled = isEnabled,
            audioSessionId = audioSessionId,
            onBandGainChange = onBandGainChange,
            onToggleEnabled = onToggleFxEnabled,
            onResetToFlat = onResetToFlat,
            currentPresetName = currentPresetName,
            presets = presets,
            onApplyPreset = onApplyPreset,
            visualizerFrame = visualizerFrame,
            isPlaying = isPlaying,
            bassBoost = bassBoost,
            onBassBoostChange = onBassBoostChange,
            virtualizer = virtualizer,
            onVirtualizerChange = onVirtualizerChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Real-time Audio Spectrum Engine Control
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

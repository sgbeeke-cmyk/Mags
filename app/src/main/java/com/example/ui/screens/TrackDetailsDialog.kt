package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraGoldTertiary
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary

@Composable
fun TrackDetailsDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AuraDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = AuraCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Audio Specifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )
                    Text(
                        text = "Lossless FLAC Stream Details",
                        fontSize = 12.sp,
                        color = AuraTextSecondary
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(
                    icon = Icons.Default.GraphicEq,
                    label = "Container & Codec",
                    value = "${track.format} (Free Lossless Audio Codec)",
                    valueColor = AuraCyanPrimary
                )
                SpecRow(
                    icon = Icons.Default.Speed,
                    label = "Sample Rate",
                    value = "${track.sampleRate} Hz (${track.sampleRate / 1000.0} kHz)",
                    valueColor = if (track.sampleRate >= 96000) AuraGoldTertiary else AuraCyanPrimary
                )
                SpecRow(
                    icon = Icons.Default.Memory,
                    label = "Bit Depth",
                    value = "${track.bitDepth}-bit Integer PCM",
                    valueColor = if (track.bitDepth >= 24) AuraGoldTertiary else AuraCyanPrimary
                )
                SpecRow(
                    icon = Icons.Default.AudioFile,
                    label = "Lossless Bitrate",
                    value = "${track.bitrateKbps} kbps VBR"
                )
                SpecRow(
                    icon = Icons.Default.GraphicEq,
                    label = "Channels",
                    value = "${track.channels} (Stereo Channel Array)"
                )
                SpecRow(
                    icon = Icons.Default.Album,
                    label = "Album & Artist",
                    value = "${track.album} • ${track.artist}"
                )
                SpecRow(
                    icon = Icons.Default.Lyrics,
                    label = "Embedded Lyrics",
                    value = if (!track.lyricsRaw.isNullOrBlank()) "Vorbis Comment Embedded (Synchronized)" else "None",
                    valueColor = if (!track.lyricsRaw.isNullOrBlank()) AuraCyanPrimary else AuraTextMuted
                )
                if (track.fileSize > 0) {
                    val sizeMb = String.format("%.2f MB", track.fileSize / (1024.0 * 1024.0))
                    SpecRow(
                        icon = Icons.Default.Info,
                        label = "File Size",
                        value = sizeMb
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_button"),
                colors = ButtonDefaults.textButtonColors(contentColor = AuraCyanPrimary)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SpecRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = AuraTextPrimary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AuraDarkCard)
            .border(1.dp, AuraDarkBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraVioletSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AuraTextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.SleepTimerState
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceVariant
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaPlayerSettingsDialog(
    sleepTimerState: SleepTimerState,
    isAutoPlayNext: Boolean,
    isGaplessEnabled: Boolean,
    onToggleAutoPlayNext: (Boolean) -> Unit,
    onToggleGapless: (Boolean) -> Unit,
    onOpenFullSleepTimerDialog: () -> Unit,
    onStartSleepTimerPreset: (minutes: Int) -> Unit,
    onStartSleepTimerEndOfTrack: () -> Unit,
    onCancelSleepTimer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .testTag("media_player_settings_dialog")
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        containerColor = AuraDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AuraCyanPrimary.copy(alpha = 0.25f), AuraVioletSecondary.copy(alpha = 0.25f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Player Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary
                        )
                        Text(
                            text = "Playback & Audio Preferences",
                            fontSize = 11.sp,
                            color = AuraTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = AuraTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SLEEP TIMER SETTINGS CARD
                Card(
                    modifier = Modifier
                        .testTag("settings_sleep_timer_card")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (sleepTimerState.isActive) AuraCyanPrimary.copy(alpha = 0.6f) else AuraDarkBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sleepTimerState.isActive) AuraCyanPrimary.copy(alpha = 0.2f)
                                            else AuraDarkSurfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = "Sleep Timer",
                                        tint = if (sleepTimerState.isActive) AuraCyanPrimary else AuraTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Sleep Timer",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTextPrimary
                                    )
                                    Text(
                                        text = if (sleepTimerState.isActive) {
                                            "Active: ${sleepTimerState.formattedRemaining}"
                                        } else {
                                            "Pause playback on countdown"
                                        },
                                        fontSize = 11.sp,
                                        color = if (sleepTimerState.isActive) AuraCyanPrimary else AuraTextSecondary,
                                        fontWeight = if (sleepTimerState.isActive) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            if (sleepTimerState.isActive) {
                                TextButton(
                                    onClick = onCancelSleepTimer,
                                    modifier = Modifier.testTag("settings_cancel_sleep_timer_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TimerOff,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancel", color = Color(0xFFFF6B6B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Presets Row
                        Text(
                            text = "QUICK PRESETS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextMuted,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf(15, 30, 45, 60)
                            presets.forEach { mins ->
                                val isSelected = sleepTimerState.isActive && !sleepTimerState.isWaitingForTrackEnd && (sleepTimerState.totalSeconds == mins * 60L)
                                Box(
                                    modifier = Modifier
                                        .testTag("settings_preset_${mins}m")
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) AuraCyanPrimary
                                            else AuraDarkSurfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onStartSleepTimerPreset(mins) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AuraDarkSurface else AuraTextPrimary
                                    )
                                }
                            }

                            // End of track preset
                            Box(
                                modifier = Modifier
                                    .testTag("settings_preset_end_of_track")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (sleepTimerState.isWaitingForTrackEnd) AuraCyanPrimary
                                        else AuraDarkSurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (sleepTimerState.isWaitingForTrackEnd) AuraCyanPrimary else AuraDarkBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onStartSleepTimerEndOfTrack() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "End of Song",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sleepTimerState.isWaitingForTrackEnd) AuraDarkSurface else AuraTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Link to full customized dialog
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenFullSleepTimerDialog)
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Customize timer slider & options...",
                                fontSize = 12.sp,
                                color = AuraCyanPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AuraCyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // 2. AUTO PLAY NEXT SONG SETTINGS CARD
                Card(
                    modifier = Modifier
                        .testTag("settings_auto_play_next_card")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isAutoPlayNext) AuraCyanPrimary.copy(alpha = 0.2f)
                                        else AuraDarkSurfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = if (isAutoPlayNext) AuraCyanPrimary else AuraTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto-Play Next Song",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = if (isAutoPlayNext) "Continuous playback enabled" else "Pause at track completion",
                                    fontSize = 11.sp,
                                    color = AuraTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isAutoPlayNext,
                            onCheckedChange = onToggleAutoPlayNext,
                            modifier = Modifier.testTag("auto_play_next_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuraDarkSurface,
                                checkedTrackColor = AuraCyanPrimary,
                                uncheckedThumbColor = AuraTextMuted,
                                uncheckedTrackColor = AuraDarkSurfaceVariant
                            )
                        )
                    }
                }

                // 3. GAPLESS BIT-PERFECT PLAYBACK CARD
                Card(
                    modifier = Modifier
                        .testTag("settings_gapless_card")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isGaplessEnabled) AuraCyanPrimary.copy(alpha = 0.2f)
                                        else AuraDarkSurfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isGaplessEnabled) AuraCyanPrimary else AuraTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Gapless Audio Output",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "Zero-latency studio transitions",
                                    fontSize = 11.sp,
                                    color = AuraTextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isGaplessEnabled,
                            onCheckedChange = onToggleGapless,
                            modifier = Modifier.testTag("gapless_playback_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuraDarkSurface,
                                checkedTrackColor = AuraCyanPrimary,
                                uncheckedThumbColor = AuraTextMuted,
                                uncheckedTrackColor = AuraDarkSurfaceVariant
                            )
                        )
                    }
                }

                // 4. EQUALIZER QUICK SHORTCUT
                Card(
                    modifier = Modifier
                        .testTag("settings_equalizer_shortcut_card")
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenEqualizer()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AuraDarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = null,
                                    tint = AuraCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Equalizer & DSP Engine",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "5-band hardware tuning & bass boost",
                                    fontSize = 11.sp,
                                    color = AuraTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open Equalizer",
                            tint = AuraTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("settings_done_btn")
            ) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Bold,
                    color = AuraCyanPrimary,
                    fontSize = 14.sp
                )
            }
        }
    )
}

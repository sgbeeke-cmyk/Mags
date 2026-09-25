package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.SleepTimerState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
    sleepTimerState: SleepTimerState,
    onStartTimer: (minutes: Int, finishTrack: Boolean) -> Unit,
    onStartEndOfTrack: () -> Unit,
    onAddMinutes: (minutes: Int) -> Unit,
    onCancelTimer: () -> Unit,
    onToggleFinishTrack: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val presetOptions = listOf(5, 10, 15, 30, 45, 60, 90)
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var isEndOfTrackSelected by remember { mutableStateOf(false) }
    var finishCurrentTrackFirst by remember { mutableStateOf(false) }
    var customSliderValue by remember { mutableFloatStateOf(30f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .testTag("sleep_timer_dialog")
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sleep Timer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary
                        )
                        Text(
                            text = if (sleepTimerState.isActive) "Active countdown" else "Stop playback automatically",
                            fontSize = 11.sp,
                            color = AuraTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("sleep_timer_dismiss_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AuraTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (sleepTimerState.isActive) {
                    // ACTIVE TIMER DISPLAY
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PLAYBACK WILL STOP IN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = AuraTextMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = sleepTimerState.formattedRemaining,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraCyanPrimary,
                                modifier = Modifier.testTag("sleep_timer_countdown_text")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { sleepTimerState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AuraCyanPrimary,
                                trackColor = Color(0xFF1B2333)
                            )

                            if (sleepTimerState.isWaitingForTrackEnd) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Waiting for the current track to finish...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraVioletSecondary
                                )
                            }
                        }
                    }

                    // Add more time quick actions
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Extend timer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAddMinutes(5) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("sleep_timer_add_5_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuraDarkSurfaceVariant,
                                    contentColor = AuraTextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+5m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAddMinutes(15) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("sleep_timer_add_15_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuraDarkSurfaceVariant,
                                    contentColor = AuraTextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+15m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAddMinutes(30) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("sleep_timer_add_30_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuraDarkSurfaceVariant,
                                    contentColor = AuraTextPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+30m", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Toggle: Wait for current song to finish
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraDarkCard)
                            .clickable { onToggleFinishTrack(!sleepTimerState.finishCurrentTrackFirst) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Finish current song first",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = "Plays until current track ends",
                                fontSize = 11.sp,
                                color = AuraTextMuted
                            )
                        }

                        Switch(
                            checked = sleepTimerState.finishCurrentTrackFirst,
                            onCheckedChange = { onToggleFinishTrack(it) },
                            modifier = Modifier.testTag("sleep_timer_finish_track_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AuraDarkBackground,
                                checkedTrackColor = AuraCyanPrimary,
                                uncheckedThumbColor = AuraTextMuted,
                                uncheckedTrackColor = AuraDarkSurfaceVariant
                            )
                        )
                    }

                    // Turn off button
                    Button(
                        onClick = onCancelTimer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sleep_timer_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33FF5252),
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF5252))
                    ) {
                        Icon(imageVector = Icons.Default.TimerOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Turn Off Sleep Timer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    // INACTIVE: SELECT DURATION
                    Text(
                        text = "Select duration",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTextSecondary
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetOptions.forEach { minutes ->
                            val isSelected = !isEndOfTrackSelected && selectedMinutes == minutes
                            Box(
                                modifier = Modifier
                                    .testTag("sleep_timer_preset_$minutes")
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AuraCyanPrimary.copy(alpha = 0.2f) else AuraDarkCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) AuraCyanPrimary else AuraDarkBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isEndOfTrackSelected = false
                                        selectedMinutes = minutes
                                        customSliderValue = minutes.toFloat()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "$minutes min",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AuraCyanPrimary else AuraTextPrimary
                                )
                            }
                        }

                        // End of current track preset
                        val isEndSelected = isEndOfTrackSelected
                        Box(
                            modifier = Modifier
                                .testTag("sleep_timer_preset_end_of_track")
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isEndSelected) AuraCyanPrimary.copy(alpha = 0.2f) else AuraDarkCard)
                                .border(
                                    1.dp,
                                    if (isEndSelected) AuraCyanPrimary else AuraDarkBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isEndOfTrackSelected = true
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isEndSelected) AuraCyanPrimary else AuraTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "End of Song",
                                    fontSize = 12.sp,
                                    fontWeight = if (isEndSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isEndSelected) AuraCyanPrimary else AuraTextPrimary
                                )
                            }
                        }
                    }

                    if (!isEndOfTrackSelected) {
                        // Custom Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Duration",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraTextSecondary
                                )
                                Text(
                                    text = "$selectedMinutes minutes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraCyanPrimary
                                )
                            }

                            Slider(
                                value = customSliderValue,
                                onValueChange = {
                                    customSliderValue = it
                                    selectedMinutes = it.toInt()
                                },
                                valueRange = 1f..120f,
                                modifier = Modifier
                                    .testTag("sleep_timer_slider")
                                    .fillMaxWidth()
                                    .height(24.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = AuraCyanPrimary,
                                    activeTrackColor = AuraCyanPrimary,
                                    inactiveTrackColor = Color(0xFF1B2333)
                                )
                            )
                        }

                        // Toggle: Wait until current song finishes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraDarkCard)
                                .clickable { finishCurrentTrackFirst = !finishCurrentTrackFirst }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Finish current song first",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "Waits for song to finish if timer ends during playback",
                                    fontSize = 11.sp,
                                    color = AuraTextMuted
                                )
                            }

                            Switch(
                                checked = finishCurrentTrackFirst,
                                onCheckedChange = { finishCurrentTrackFirst = it },
                                modifier = Modifier.testTag("sleep_timer_finish_track_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AuraDarkBackground,
                                    checkedTrackColor = AuraCyanPrimary,
                                    uncheckedThumbColor = AuraTextMuted,
                                    uncheckedTrackColor = AuraDarkSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!sleepTimerState.isActive) {
                Button(
                    onClick = {
                        if (isEndOfTrackSelected) {
                            onStartEndOfTrack()
                        } else {
                            onStartTimer(selectedMinutes, finishCurrentTrackFirst)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("sleep_timer_start_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraCyanPrimary,
                        contentColor = AuraDarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Timer", fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("sleep_timer_done_button")
                ) {
                    Text("Done", color = AuraCyanPrimary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!sleepTimerState.isActive) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AuraTextSecondary)
                }
            }
        }
    )
}

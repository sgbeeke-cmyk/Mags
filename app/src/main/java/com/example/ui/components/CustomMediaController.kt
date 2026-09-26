package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraVioletSecondary

/**
 * Custom MediaController UI Component for Media3 ExoPlayer session.
 * Provides high-precision play/pause, track skip (previous/next), seek scrubbing slider,
 * step seek forward/backward (+10s / -10s), and live FLAC lossless audio stream indicators.
 */
@Composable
fun CustomMediaController(
    track: Track?,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekForward: () -> Unit = { onSeekTo((playbackPositionMs + 10_000L).coerceAtMost(durationMs)) },
    onSeekBack: () -> Unit = { onSeekTo((playbackPositionMs - 10_000L).coerceAtLeast(0L)) },
    modifier: Modifier = Modifier,
    isMediaSessionActive: Boolean = true,
    showTrackHeader: Boolean = true
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPosition by remember { mutableFloatStateOf(0f) }

    val currentPosition = if (isDraggingSlider) {
        sliderDragPosition
    } else {
        playbackPositionMs.toFloat()
    }

    val totalDuration = durationMs.coerceAtLeast(1L).toFloat()
    val progressFraction = (currentPosition / totalDuration).coerceIn(0f, 1f)

    // Subtle pulsing indicator for active Media3 session
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .testTag("custom_media_controller")
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
        border = BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Media3 Session Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Session Pill
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
                                color = if (isMediaSessionActive) {
                                    AuraCyanPrimary.copy(alpha = if (isPlaying) pulseAlpha else 0.8f)
                                } else {
                                    Color.Gray
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MEDIA3 EXOPLAYER SESSION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMediaSessionActive) AuraCyanPrimary else AuraTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }

                // FLAC Lossless Specification Tag
                if (track != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuraCyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, AuraCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (track.isHiRes) "${track.sampleRate} / ${track.bitDepth}" else "FLAC LOSSLESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraCyanPrimary
                        )
                    }
                }
            }

            // Track Header if requested
            if (showTrackHeader && track != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${track.artist} • ${track.album}",
                            fontSize = 12.sp,
                            color = AuraTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Precision Seek Bar / Scrubber Slider
            Slider(
                value = progressFraction,
                onValueChange = { fraction ->
                    isDraggingSlider = true
                    sliderDragPosition = fraction * totalDuration
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    onSeekTo(sliderDragPosition.toLong())
                },
                modifier = Modifier
                    .testTag("media_controller_seek_slider")
                    .fillMaxWidth()
                    .height(24.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AuraCyanPrimary,
                    activeTrackColor = AuraCyanPrimary,
                    inactiveTrackColor = Color(0xFF1E2838)
                )
            )

            // Elapsed and Remaining Timestamps + Quick Seek Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(currentPosition.toLong()),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = AuraCyanPrimary
                )

                // Quick Seek shortcuts (-10s / +10s)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AuraDarkSurface)
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(6.dp))
                            .clickable { onSeekBack() }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-10s",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AuraDarkSurface)
                            .border(1.dp, AuraDarkBorder, RoundedCornerShape(6.dp))
                            .clickable { onSeekForward() }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+10s",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextMuted
                        )
                    }
                }

                val remainingMs = (durationMs - currentPosition.toLong()).coerceAtLeast(0L)
                Text(
                    text = "-${formatTime(remainingMs)}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = AuraTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Media Controls Row: Skip Previous, Seek Back 10, Play/Pause, Seek Forward 10, Skip Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("media_controller_transport_row"),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Previous Track Button
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .testTag("media_controller_skip_previous")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Skip to Previous FLAC track",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Step Rewind 10 Seconds
                IconButton(
                    onClick = onSeekBack,
                    modifier = Modifier
                        .testTag("media_controller_seek_back_button")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Seek backward 10 seconds",
                        tint = AuraCyanPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause Primary Action Button
                Box(
                    modifier = Modifier
                        .testTag("media_controller_play_pause")
                        .size(64.dp)
                        .clip(CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = AuraCyanPrimary)
                        .background(
                            Brush.linearGradient(
                                listOf(AuraCyanPrimary, AuraVioletSecondary)
                            )
                        )
                        .clickable(onClick = onTogglePlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isPlaying, label = "playPauseCrossfade") { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause FLAC playback" else "Play FLAC track",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Step Forward 10 Seconds
                IconButton(
                    onClick = onSeekForward,
                    modifier = Modifier
                        .testTag("media_controller_seek_forward_button")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Seek forward 10 seconds",
                        tint = AuraCyanPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Skip Next Track Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .testTag("media_controller_skip_next")
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip to Next FLAC track",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraGoldTertiary
import com.example.ui.theme.AuraVioletSecondary

@Composable
fun HiResBadge(
    track: Track,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isMaster = track.sampleRate >= 96000 && track.bitDepth >= 24
    val isHiRes = track.isHiRes || track.sampleRate > 48000 || track.bitDepth > 16

    val gradient = when {
        isMaster -> Brush.horizontalGradient(listOf(AuraGoldTertiary, AuraCyanPrimary))
        isHiRes -> Brush.horizontalGradient(listOf(AuraCyanPrimary, AuraVioletSecondary))
        else -> Brush.horizontalGradient(listOf(Color(0xFF8899A6), Color(0xFF5A6677)))
    }

    val label = when {
        compact && isMaster -> "24/96 FLAC"
        compact && isHiRes -> "HI-RES"
        compact -> "FLAC"
        isMaster -> "FLAC • 24-BIT / 96kHz MASTER"
        isHiRes -> "FLAC • ${track.bitDepth}-BIT / ${track.sampleRate / 1000}kHz LOSSLESS"
        else -> "FLAC LOSSLESS"
    }

    Box(
        modifier = modifier
            .testTag("hires_badge")
            .border(
                width = 1.dp,
                brush = gradient,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = Color(0xFF10141D).copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = if (compact) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (isMaster) AuraGoldTertiary else if (isHiRes) AuraCyanPrimary else Color(0xFFCAD5E2)
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryStorageState
import com.example.model.SuperIslandMode
import com.example.model.SuperIslandState
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.SpatialViolet
import com.example.ui.theme.SpecularRimTop
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SuperIslandPill(
    state: SuperIslandState,
    battery: BatteryStorageState,
    onToggleExpand: () -> Unit,
    onStartTimerPreset: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onToggleMedia: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = if (state.isExpanded) 30.dp else 50.dp
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .widthIn(
                    min = if (state.isExpanded) 310.dp else 168.dp,
                    max = if (state.isExpanded) 420.dp else 280.dp
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF00B0E16),
                            Color(0xE6111623)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(SpecularRimTop, Color.White.copy(alpha = 0.08f))
                    ),
                    shape = shape
                )
                .clickable(onClick = onToggleExpand)
                .padding(
                    horizontal = if (state.isExpanded) 18.dp else 14.dp,
                    vertical = if (state.isExpanded) 16.dp else 9.dp
                )
                .testTag("super_island_pill")
        ) {
            if (!state.isExpanded) {
                CompactSuperIslandContent(mode = state.mode, battery = battery)
            } else {
                ExpandedSuperIslandContent(
                    state = state,
                    battery = battery,
                    onCollapse = onToggleExpand,
                    onStartTimerPreset = onStartTimerPreset,
                    onToggleTimer = onToggleTimer,
                    onResetTimer = onResetTimer,
                    onToggleMedia = onToggleMedia,
                    onNextTrack = onNextTrack,
                    onPrevTrack = onPrevTrack
                )
            }
        }
    }
}

@Composable
private fun CompactSuperIslandContent(
    mode: SuperIslandMode,
    battery: BatteryStorageState
) {
    AnimatedContent(targetState = mode, label = "island_compact_mode") { currentMode ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (currentMode) {
                is SuperIslandMode.Timer -> {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Active timer",
                        tint = AmberWarm,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatCountdown(currentMode.remainingSeconds),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AmberWarm
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (currentMode.isRunning) EmeraldLive else TextSecondary)
                    )
                }

                is SuperIslandMode.Media -> {
                    AudioWaveBars(isPlaying = currentMode.isPlaying, color = SpatialViolet)
                    Text(
                        text = currentMode.trackTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = currentMode.sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = HyperBlue
                    )
                }

                is SuperIslandMode.Charging -> {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Charging",
                        tint = EmeraldLive,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = currentMode.statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${currentMode.percent}%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldLive
                    )
                }

                SuperIslandMode.Idle -> {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(HyperBlue)
                    )
                    Text(
                        text = "Super Island",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${battery.batteryPercent}%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedSuperIslandContent(
    state: SuperIslandState,
    battery: BatteryStorageState,
    onCollapse: () -> Unit,
    onStartTimerPreset: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onToggleMedia: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.GraphicEq,
                    contentDescription = "Island status",
                    tint = if (battery.isCharging) EmeraldLive else HyperBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (battery.isCharging) "${battery.chargingLabel} · ${battery.batteryPercent}%"
                    else "Aether Super Island",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("super_island_close")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Collapse Super Island",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Media Deck
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AudioWaveBars(isPlaying = state.mediaPlaying, color = SpatialViolet)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = state.mediaLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (state.mediaPlaying) "Media Active" else "Tap to control",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPrevTrack,
                    modifier = Modifier.size(40.dp).testTag("island_media_prev")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous track",
                        tint = TextPrimary
                    )
                }
                FilledTonalIconButton(
                    onClick = onToggleMedia,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = HyperBlue,
                        contentColor = ObsidianCanvas
                    ),
                    modifier = Modifier.size(40.dp).testTag("island_media_play_pause")
                ) {
                    Icon(
                        imageVector = if (state.mediaPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.mediaPlaying) "Pause media" else "Play media"
                    )
                }
                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier.size(40.dp).testTag("island_media_next")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next track",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Focus / Quick Timer Deck
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Focus timer",
                        tint = AmberWarm,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCountdown(state.timerRemainingSeconds),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onResetTimer,
                        modifier = Modifier.size(38.dp).testTag("island_timer_reset")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset timer",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onToggleTimer,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (state.isTimerRunning) AmberWarm else EmeraldLive,
                            contentColor = ObsidianCanvas
                        ),
                        modifier = Modifier.size(38.dp).testTag("island_timer_toggle")
                    ) {
                        Icon(
                            imageVector = if (state.isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (state.isTimerRunning) "Pause timer" else "Start timer",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(60 to "1m", 300 to "5m", 900 to "15m", 1500 to "25m").forEach { (sec, label) ->
                    val selected = state.timerTotalSeconds == sec
                    Surface(
                        onClick = { onStartTimerPreset(sec) },
                        color = if (selected) HyperBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("island_preset_$label")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = if (selected) HyperBlue else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioWaveBars(isPlaying: Boolean, color: Color) {
    val transition = rememberInfiniteTransition(label = "wave_bars")
    val bar1 by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isPlaying) 1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = if (isPlaying) 0.25f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.9f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(490, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(14.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((14f * fraction).dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private fun formatCountdown(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

package com.example.ui.components

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ControlCenterState
import com.example.model.SuperIslandState
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.SpatialViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ControlCenterOverlay(
    state: ControlCenterState,
    islandState: SuperIslandState,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleTorch: () -> Unit,
    onCycleRinger: () -> Unit,
    onOpenWifiPanel: () -> Unit,
    onOpenInternetPanel: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onToggleMedia: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onStartQuickTimer: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onOpenLauncherSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCanvas.copy(alpha = 0.80f))
            .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("control_center_overlay")
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { } }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Control Center",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("control_center_close")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Control Center",
                        tint = TextPrimary
                    )
                }
            }

            // Top Bento: Connectivity Cluster + Media Deck
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LiquidGlassBox(
                    cornerRadius = 26.dp,
                    elevated = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ConnectivityBubble(
                                icon = Icons.Rounded.Wifi,
                                label = "Wi-Fi",
                                active = state.isWifiConnected,
                                activeColor = HyperBlue,
                                testTag = "cc_wifi_tile",
                                onClick = onOpenWifiPanel,
                                modifier = Modifier.weight(1f)
                            )
                            ConnectivityBubble(
                                icon = Icons.Rounded.SignalCellularAlt,
                                label = "Data",
                                active = state.isCellularConnected,
                                activeColor = EmeraldLive,
                                testTag = "cc_cellular_tile",
                                onClick = onOpenInternetPanel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ConnectivityBubble(
                                icon = Icons.Rounded.Bluetooth,
                                label = "Bluetooth",
                                active = state.isBluetoothOn,
                                activeColor = HyperBlue,
                                testTag = "cc_bluetooth_tile",
                                onClick = onOpenBluetooth,
                                modifier = Modifier.weight(1f)
                            )
                            ConnectivityBubble(
                                icon = Icons.Rounded.Settings,
                                label = "System",
                                active = false,
                                activeColor = SpatialViolet,
                                testTag = "cc_system_settings_tile",
                                onClick = onOpenSystemSettings,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Now Playing Glass Card
                LiquidGlassBox(
                    cornerRadius = 26.dp,
                    elevated = true,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "MEDIA",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpatialViolet
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = islandState.mediaLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = if (islandState.mediaPlaying) "Playing" else "Ready",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = onPrevTrack,
                                modifier = Modifier.size(40.dp).testTag("cc_media_prev")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = onToggleMedia,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HyperBlue)
                                    .testTag("cc_media_play")
                            ) {
                                Icon(
                                    imageVector = if (islandState.mediaPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play or Pause",
                                    tint = ObsidianCanvas
                                )
                            }
                            IconButton(
                                onClick = onNextTrack,
                                modifier = Modifier.size(40.dp).testTag("cc_media_next")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SkipNext,
                                    contentDescription = "Next",
                                    tint = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Brightness & Volume Sliders
            LiquidGlassBox(
                cornerRadius = 26.dp,
                elevated = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    GlassControlSliderRow(
                        icon = Icons.Rounded.BrightnessHigh,
                        label = "Brightness",
                        value = state.brightness,
                        accentColor = AmberWarm,
                        testTag = "cc_brightness_slider",
                        onValueChange = onBrightnessChange
                    )
                    GlassControlSliderRow(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        label = "Media Volume",
                        value = state.volume,
                        accentColor = HyperBlue,
                        testTag = "cc_volume_slider",
                        onValueChange = onVolumeChange
                    )
                }
            }

            // Quick Action Tiles
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionTile(
                    icon = if (state.isTorchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                    title = "Flashlight",
                    subtitle = if (!state.hasTorch) "No Flash" else if (state.isTorchOn) "On" else "Off",
                    active = state.isTorchOn,
                    activeColor = AmberWarm,
                    testTag = "cc_torch_tile",
                    onClick = onToggleTorch,
                    modifier = Modifier.weight(1f)
                )

                val (ringerIcon, ringerLabel) = when (state.ringerMode) {
                    AudioManager.RINGER_MODE_VIBRATE -> Icons.Rounded.Vibration to "Vibrate"
                    AudioManager.RINGER_MODE_SILENT -> Icons.Rounded.NotificationsOff to "Silent"
                    else -> Icons.Rounded.NotificationsActive to "Ring"
                }
                QuickActionTile(
                    icon = ringerIcon,
                    title = "Sound Mode",
                    subtitle = ringerLabel,
                    active = state.ringerMode != AudioManager.RINGER_MODE_NORMAL,
                    activeColor = SpatialViolet,
                    testTag = "cc_ringer_tile",
                    onClick = onCycleRinger,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionTile(
                    icon = Icons.Rounded.Timer,
                    title = "5m Focus",
                    subtitle = "Super Island",
                    active = islandState.isTimerRunning,
                    activeColor = EmeraldLive,
                    testTag = "cc_timer_tile",
                    onClick = onStartQuickTimer,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.Wallpaper,
                    title = "Wallpapers",
                    subtitle = "Studio",
                    active = false,
                    activeColor = HyperBlue,
                    testTag = "cc_wallpaper_tile",
                    onClick = onOpenWallpaperStudio,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.Tune,
                    title = "Launcher",
                    subtitle = "Settings",
                    active = false,
                    activeColor = HyperBlue,
                    testTag = "cc_launcher_settings_tile",
                    onClick = onOpenLauncherSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ConnectivityBubble(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (active) activeColor else Color.White.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) ObsidianCanvas else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun GlassControlSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    accentColor: Color,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.16f)
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    active: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassBox(
        cornerRadius = 22.dp,
        fillColor = if (active) activeColor.copy(alpha = 0.28f) else Color(0x99141923),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) activeColor else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) activeColor else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClockStyle
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.WallpaperPreset
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.SpatialViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditModeBar(
    settings: LauncherSettings,
    onToggleClockWidget: (Boolean) -> Unit,
    onToggleWeatherWidget: (Boolean) -> Unit,
    onToggleBatteryWidget: (Boolean) -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
            .testTag("edit_mode_overlay")
    ) {
        LiquidGlassBox(
            cornerRadius = 28.dp,
            elevated = true,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = "Edit home widgets",
                            tint = HyperBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customize Home",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HyperBlue,
                            contentColor = ObsidianCanvas
                        ),
                        modifier = Modifier.testTag("edit_mode_done_button")
                    ) {
                        Text("Done")
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WidgetToggleChip(
                        label = "Clock",
                        selected = settings.showClockWidget,
                        testTag = "edit_toggle_clock",
                        onClick = { onToggleClockWidget(!settings.showClockWidget) },
                        modifier = Modifier.weight(1f)
                    )
                    WidgetToggleChip(
                        label = "Weather",
                        selected = settings.showWeatherWidget,
                        testTag = "edit_toggle_weather",
                        onClick = { onToggleWeatherWidget(!settings.showWeatherWidget) },
                        modifier = Modifier.weight(1f)
                    )
                    WidgetToggleChip(
                        label = "Battery",
                        selected = settings.showBatteryWidget,
                        testTag = "edit_toggle_battery",
                        onClick = { onToggleBatteryWidget(!settings.showBatteryWidget) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onOpenWallpaperPicker,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("edit_open_wallpapers")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = "Wallpapers",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wallpapers")
                    }
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("edit_open_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings")
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetToggleChip(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "$label enabled",
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = HyperBlue.copy(alpha = 0.25f),
            selectedLabelColor = HyperBlue,
            selectedLeadingIconColor = HyperBlue,
            labelColor = TextSecondary
        ),
        modifier = modifier.testTag(testTag)
    )
}

@Composable
fun WallpaperPickerOverlay(
    currentPreset: WallpaperPreset,
    onSelectPreset: (WallpaperPreset) -> Unit,
    onPickFromGallery: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCanvas.copy(alpha = 0.72f))
            .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
            .navigationBarsPadding()
            .statusBarsPadding()
            .testTag("wallpaper_picker_overlay")
    ) {
        LiquidGlassBox(
            cornerRadius = 30.dp,
            elevated = true,
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) { detectTapGestures { } }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Wallpaper Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp).testTag("wallpaper_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Wallpaper Studio",
                            tint = TextPrimary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WallpaperThumbCard(
                        title = "Liquid Glass",
                        drawableRes = R.drawable.img_wall_liquid_glass_1794742222253,
                        selected = currentPreset == WallpaperPreset.LIQUID_GLASS,
                        testTag = "wallpaper_preset_liquid_glass",
                        onClick = { onSelectPreset(WallpaperPreset.LIQUID_GLASS) },
                        modifier = Modifier.weight(1f)
                    )
                    WallpaperThumbCard(
                        title = "Hyper Aurora",
                        drawableRes = R.drawable.img_wall_hyper_aurora_1794742222966,
                        selected = currentPreset == WallpaperPreset.HYPER_AURORA,
                        testTag = "wallpaper_preset_hyper_aurora",
                        onClick = { onSelectPreset(WallpaperPreset.HYPER_AURORA) },
                        modifier = Modifier.weight(1f)
                    )
                    WallpaperThumbCard(
                        title = "Obsidian",
                        drawableRes = R.drawable.img_wall_obsidian_dunes_1794742224328,
                        selected = currentPreset == WallpaperPreset.OBSIDIAN_DUNES,
                        testTag = "wallpaper_preset_obsidian",
                        onClick = { onSelectPreset(WallpaperPreset.OBSIDIAN_DUNES) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onPickFromGallery,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HyperBlue,
                            contentColor = ObsidianCanvas
                        ),
                        modifier = Modifier.weight(1f).testTag("wallpaper_pick_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Pick photo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Photo")
                    }

                    Button(
                        onClick = { onSelectPreset(WallpaperPreset.SYSTEM_LIVE) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPreset == WallpaperPreset.SYSTEM_LIVE)
                                SpatialViolet else Color.White.copy(alpha = 0.12f),
                            contentColor = if (currentPreset == WallpaperPreset.SYSTEM_LIVE)
                                ObsidianCanvas else TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("wallpaper_system_live_button")
                    ) {
                        Text("System Backdrop")
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperThumbCard(
    title: String,
    drawableRes: Int,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) HyperBlue else Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(HyperBlue)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Selected",
                        tint = ObsidianCanvas,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) HyperBlue else TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
fun LauncherSettingsOverlay(
    settings: LauncherSettings,
    isDefaultLauncher: Boolean,
    onSetGridColumns: (Int) -> Unit,
    onSetIconShape: (IconShapeStyle) -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onToggleAppDrawer: (Boolean) -> Unit,
    onToggleAppLibrary: (Boolean) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onOpenDefaultHomeSettings: () -> Unit,
    onResetLayout: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCanvas.copy(alpha = 0.84f))
            .pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("launcher_settings_overlay")
    ) {
        LiquidGlassBox(
            cornerRadius = 30.dp,
            elevated = true,
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) { detectTapGestures { } }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Launcher Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp).testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Settings",
                            tint = TextPrimary
                        )
                    }
                }

                // Default Launcher Banner
                LiquidGlassBox(
                    cornerRadius = 22.dp,
                    fillColor = if (isDefaultLauncher) EmeraldLive.copy(alpha = 0.16f)
                    else HyperBlue.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = "Default home app",
                                tint = if (isDefaultLauncher) EmeraldLive else HyperBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isDefaultLauncher) "Active Default Launcher" else "Set as Default Home",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isDefaultLauncher) "Aether Launcher handles Home button"
                                    else "Open Android Home app chooser",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        TextButton(
                            onClick = onOpenDefaultHomeSettings,
                            modifier = Modifier.testTag("settings_default_home_button")
                        ) {
                            Text(if (isDefaultLauncher) "Change" else "Set")
                        }
                    }
                }

                // Grid Columns
                Text(
                    text = "Home Grid Columns",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(4 to "4 Columns", 5 to "5 Columns").forEach { (cols, label) ->
                        FilterChip(
                            selected = settings.gridColumns == cols,
                            onClick = { onSetGridColumns(cols) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("settings_grid_$cols")
                        )
                    }
                }

                // Icon Shape
                Text(
                    text = "Icon Mask Shape",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconShapeStyle.entries.forEach { shape ->
                        FilterChip(
                            selected = settings.iconShape == shape,
                            onClick = { onSetIconShape(shape) },
                            label = { Text(shape.label) },
                            modifier = Modifier.testTag("settings_shape_${shape.id}")
                        )
                    }
                }

                // Clock Style
                Text(
                    text = "Home Clock Style",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClockStyle.entries.forEach { style ->
                        FilterChip(
                            selected = settings.clockStyle == style,
                            onClick = { onSetClockStyle(style) },
                            label = { Text(style.label) },
                            modifier = Modifier.testTag("settings_clock_${style.id}")
                        )
                    }
                }

                // Feature Toggles
                SettingSwitchRow(
                    title = "Vertical App Drawer",
                    subtitle = "Swipe up from home for A–Z drawer",
                    checked = settings.showAppDrawer,
                    testTag = "settings_toggle_drawer",
                    onCheckedChange = onToggleAppDrawer
                )
                SettingSwitchRow(
                    title = "iOS 27 App Library Page",
                    subtitle = "Smart categorized folders on last page",
                    checked = settings.showAppLibraryPage,
                    testTag = "settings_toggle_library",
                    onCheckedChange = onToggleAppLibrary
                )
                SettingSwitchRow(
                    title = "24-Hour Clock",
                    subtitle = "Display time in 24-hour format",
                    checked = settings.is24HourFormat,
                    testTag = "settings_toggle_24h",
                    onCheckedChange = onToggle24Hour
                )
                SettingSwitchRow(
                    title = "Material You Dynamic Accent",
                    subtitle = "Use Android 12+ wallpaper accent colors",
                    checked = settings.useDynamicColors,
                    testTag = "settings_toggle_dynamic_color",
                    onCheckedChange = onToggleDynamicColors
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Button(
                        onClick = onOpenWallpaperStudio,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("settings_open_wallpapers")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallpaper,
                            contentDescription = "Wallpaper Studio",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wallpapers")
                    }
                    Button(
                        onClick = onResetLayout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("settings_reset_layout")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset layout",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Layout")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianCanvas,
                checkedTrackColor = HyperBlue
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

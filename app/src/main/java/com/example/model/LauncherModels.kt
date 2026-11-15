package com.example.model

import androidx.compose.ui.graphics.ImageBitmap

enum class AppCategory(val displayName: String) {
    PRODUCTIVITY("Productivity"),
    SOCIAL("Social & Chat"),
    MEDIA("Media & Photo"),
    GAMES("Games"),
    UTILITIES("Utilities"),
    SYSTEM("System")
}

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val category: AppCategory,
    val isSystemApp: Boolean,
    val iconBitmap: ImageBitmap? = null
) {
    val componentKey: String get() = "$packageName/$activityName"
}

enum class WallpaperPreset(val id: String, val title: String, val subtitle: String) {
    LIQUID_GLASS("liquid_glass", "Liquid Glass", "iOS 27 Sapphire Ribbon"),
    HYPER_AURORA("hyper_aurora", "Hyper Aurora", "HyperOS 4 Cosmic Mist"),
    OBSIDIAN_DUNES("obsidian_dunes", "Obsidian Dunes", "Sculpted Titanium"),
    SYSTEM_LIVE("system_live", "System Wallpaper", "Transparent Live Backdrop"),
    CUSTOM_PHOTO("custom_photo", "Custom Photo", "Selected From Gallery")
}

enum class ClockStyle(val id: String, val label: String) {
    SPATIAL_DEPTH("spatial_depth", "Spatial Depth"),
    HYPER_BOLD("hyper_bold", "HyperOS Bold"),
    MINIMAL_GLASS("minimal_glass", "Liquid Minimal")
}

enum class IconShapeStyle(val id: String, val label: String) {
    SQUIRCLE("squircle", "Squircle"),
    ROUNDED_SQUARE("rounded", "Rounded"),
    CIRCLE("circle", "Circle")
}

data class LauncherSettings(
    val gridColumns: Int = 4,
    val iconShape: IconShapeStyle = IconShapeStyle.SQUIRCLE,
    val showAppDrawer: Boolean = true,
    val showAppLibraryPage: Boolean = true,
    val clockStyle: ClockStyle = ClockStyle.SPATIAL_DEPTH,
    val showClockWidget: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showBatteryWidget: Boolean = true,
    val wallpaperPreset: WallpaperPreset = WallpaperPreset.LIQUID_GLASS,
    val customWallpaperUri: String? = null,
    val weatherCityName: String = "San Francisco",
    val is24HourFormat: Boolean = false,
    val useDynamicColors: Boolean = false
)

sealed interface SuperIslandMode {
    data object Idle : SuperIslandMode
    data class Charging(val percent: Int, val statusLabel: String) : SuperIslandMode
    data class Timer(
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val isRunning: Boolean
    ) : SuperIslandMode
    data class Media(
        val trackTitle: String,
        val sourceLabel: String,
        val isPlaying: Boolean
    ) : SuperIslandMode
}

data class SuperIslandState(
    val mode: SuperIslandMode = SuperIslandMode.Idle,
    val isExpanded: Boolean = false,
    val timerTotalSeconds: Int = 300,
    val timerRemainingSeconds: Int = 300,
    val isTimerRunning: Boolean = false,
    val isMediaActive: Boolean = false,
    val mediaPlaying: Boolean = false,
    val mediaLabel: String = "System Audio"
)

data class ControlCenterState(
    val brightness: Float = 0.72f,
    val volume: Float = 0.6f,
    val isTorchOn: Boolean = false,
    val hasTorch: Boolean = true,
    val isWifiConnected: Boolean = false,
    val wifiSsidLabel: String = "Wi-Fi",
    val isCellularConnected: Boolean = false,
    val isBluetoothOn: Boolean = false,
    val ringerMode: Int = 2 // AudioManager.RINGER_MODE_NORMAL
)

data class WeatherState(
    val city: String = "San Francisco",
    val tempCelsius: Int? = null,
    val conditionLabel: String = "Syncing weather…",
    val weatherCode: Int = 0,
    val highTemp: Int? = null,
    val lowTemp: Int? = null,
    val humidity: Int? = null,
    val windKmh: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class BatteryStorageState(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val chargingLabel: String = "Discharging",
    val temperatureC: Float = 31.0f,
    val usedStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f
) {
    val storageFraction: Float
        get() = if (totalStorageGb > 0f) (usedStorageGb / totalStorageGb).coerceIn(0f, 1f) else 0f
}

data class ContactResult(
    val id: String,
    val name: String,
    val phoneNumber: String
)

sealed interface LauncherOverlay {
    data object None : LauncherOverlay
    data object AppDrawer : LauncherOverlay
    data object SpotlightSearch : LauncherOverlay
    data object ControlCenter : LauncherOverlay
    data object EditMode : LauncherOverlay
    data object WallpaperPicker : LauncherOverlay
    data object LauncherSettings : LauncherOverlay
}

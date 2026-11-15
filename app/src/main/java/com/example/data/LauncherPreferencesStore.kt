package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ClockStyle
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.WallpaperPreset
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aether_launcher_prefs"
)

data class StoredLayoutData(
    val settings: LauncherSettings = LauncherSettings(),
    val homePackages: List<String> = emptyList(),
    val dockPackages: List<String> = emptyList(),
    val hasInitializedLayout: Boolean = false
)

class LauncherPreferencesStore(private val context: Context) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val listAdapter = moshi.adapter<List<String>>(stringListType)

    private object Keys {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val SHOW_APP_DRAWER = booleanPreferencesKey("show_app_drawer")
        val SHOW_APP_LIBRARY = booleanPreferencesKey("show_app_library")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val SHOW_CLOCK_WIDGET = booleanPreferencesKey("show_clock_widget")
        val SHOW_WEATHER_WIDGET = booleanPreferencesKey("show_weather_widget")
        val SHOW_BATTERY_WIDGET = booleanPreferencesKey("show_battery_widget")
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val HOME_PACKAGES_JSON = stringPreferencesKey("home_packages_json")
        val DOCK_PACKAGES_JSON = stringPreferencesKey("dock_packages_json")
        val LAYOUT_INITIALIZED = booleanPreferencesKey("layout_initialized")
    }

    val storedDataFlow: Flow<StoredLayoutData> = context.launcherDataStore.data.map { prefs ->
        val settings = LauncherSettings(
            gridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
            iconShape = IconShapeStyle.entries.firstOrNull { it.id == prefs[Keys.ICON_SHAPE] }
                ?: IconShapeStyle.SQUIRCLE,
            showAppDrawer = prefs[Keys.SHOW_APP_DRAWER] ?: true,
            showAppLibraryPage = prefs[Keys.SHOW_APP_LIBRARY] ?: true,
            clockStyle = ClockStyle.entries.firstOrNull { it.id == prefs[Keys.CLOCK_STYLE] }
                ?: ClockStyle.SPATIAL_DEPTH,
            showClockWidget = prefs[Keys.SHOW_CLOCK_WIDGET] ?: true,
            showWeatherWidget = prefs[Keys.SHOW_WEATHER_WIDGET] ?: true,
            showBatteryWidget = prefs[Keys.SHOW_BATTERY_WIDGET] ?: true,
            wallpaperPreset = WallpaperPreset.entries.firstOrNull {
                it.id == prefs[Keys.WALLPAPER_PRESET]
            } ?: WallpaperPreset.LIQUID_GLASS,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            weatherCityName = prefs[Keys.WEATHER_CITY] ?: "San Francisco",
            is24HourFormat = prefs[Keys.IS_24_HOUR] ?: false,
            useDynamicColors = prefs[Keys.USE_DYNAMIC_COLORS] ?: false
        )

        val homeList = prefs[Keys.HOME_PACKAGES_JSON]?.let {
            runCatching { listAdapter.fromJson(it) }.getOrNull()
        } ?: emptyList()

        val dockList = prefs[Keys.DOCK_PACKAGES_JSON]?.let {
            runCatching { listAdapter.fromJson(it) }.getOrNull()
        } ?: emptyList()

        StoredLayoutData(
            settings = settings,
            homePackages = homeList,
            dockPackages = dockList,
            hasInitializedLayout = prefs[Keys.LAYOUT_INITIALIZED] ?: false
        )
    }

    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        context.launcherDataStore.edit { prefs ->
            val current = LauncherSettings(
                gridColumns = prefs[Keys.GRID_COLUMNS] ?: 4,
                iconShape = IconShapeStyle.entries.firstOrNull { it.id == prefs[Keys.ICON_SHAPE] }
                    ?: IconShapeStyle.SQUIRCLE,
                showAppDrawer = prefs[Keys.SHOW_APP_DRAWER] ?: true,
                showAppLibraryPage = prefs[Keys.SHOW_APP_LIBRARY] ?: true,
                clockStyle = ClockStyle.entries.firstOrNull { it.id == prefs[Keys.CLOCK_STYLE] }
                    ?: ClockStyle.SPATIAL_DEPTH,
                showClockWidget = prefs[Keys.SHOW_CLOCK_WIDGET] ?: true,
                showWeatherWidget = prefs[Keys.SHOW_WEATHER_WIDGET] ?: true,
                showBatteryWidget = prefs[Keys.SHOW_BATTERY_WIDGET] ?: true,
                wallpaperPreset = WallpaperPreset.entries.firstOrNull {
                    it.id == prefs[Keys.WALLPAPER_PRESET]
                } ?: WallpaperPreset.LIQUID_GLASS,
                customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
                weatherCityName = prefs[Keys.WEATHER_CITY] ?: "San Francisco",
                is24HourFormat = prefs[Keys.IS_24_HOUR] ?: false,
                useDynamicColors = prefs[Keys.USE_DYNAMIC_COLORS] ?: false
            )
            val updated = transform(current)
            prefs[Keys.GRID_COLUMNS] = updated.gridColumns
            prefs[Keys.ICON_SHAPE] = updated.iconShape.id
            prefs[Keys.SHOW_APP_DRAWER] = updated.showAppDrawer
            prefs[Keys.SHOW_APP_LIBRARY] = updated.showAppLibraryPage
            prefs[Keys.CLOCK_STYLE] = updated.clockStyle.id
            prefs[Keys.SHOW_CLOCK_WIDGET] = updated.showClockWidget
            prefs[Keys.SHOW_WEATHER_WIDGET] = updated.showWeatherWidget
            prefs[Keys.SHOW_BATTERY_WIDGET] = updated.showBatteryWidget
            prefs[Keys.WALLPAPER_PRESET] = updated.wallpaperPreset.id
            if (updated.customWallpaperUri != null) {
                prefs[Keys.CUSTOM_WALLPAPER_URI] = updated.customWallpaperUri
            } else {
                prefs.remove(Keys.CUSTOM_WALLPAPER_URI)
            }
            prefs[Keys.WEATHER_CITY] = updated.weatherCityName
            prefs[Keys.IS_24_HOUR] = updated.is24HourFormat
            prefs[Keys.USE_DYNAMIC_COLORS] = updated.useDynamicColors
        }
    }

    suspend fun saveLayout(homePackages: List<String>, dockPackages: List<String>) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.HOME_PACKAGES_JSON] = listAdapter.toJson(homePackages)
            prefs[Keys.DOCK_PACKAGES_JSON] = listAdapter.toJson(dockPackages)
            prefs[Keys.LAYOUT_INITIALIZED] = true
        }
    }

    suspend fun resetLayoutFlag() {
        context.launcherDataStore.edit { prefs ->
            prefs.remove(Keys.HOME_PACKAGES_JSON)
            prefs.remove(Keys.DOCK_PACKAGES_JSON)
            prefs[Keys.LAYOUT_INITIALIZED] = false
        }
    }
}

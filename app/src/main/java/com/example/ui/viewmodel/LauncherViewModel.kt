package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.LauncherPreferencesStore
import com.example.data.MathEvaluator
import com.example.data.StoredLayoutData
import com.example.data.SystemControlRepository
import com.example.data.WeatherRepository
import com.example.model.AppCategory
import com.example.model.BatteryStorageState
import com.example.model.ClockStyle
import com.example.model.ContactResult
import com.example.model.ControlCenterState
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.model.LauncherOverlay
import com.example.model.LauncherSettings
import com.example.model.SuperIslandMode
import com.example.model.SuperIslandState
import com.example.model.WallpaperPreset
import com.example.model.WeatherState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepo = AppRepository(application)
    private val systemRepo = SystemControlRepository(application)
    private val weatherRepo = WeatherRepository()
    private val prefsStore = LauncherPreferencesStore(application)

    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val allApps: StateFlow<List<InstalledApp>> = _allApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _storedLayout = MutableStateFlow(StoredLayoutData())
    val settings: StateFlow<LauncherSettings> = _storedLayout
        .combine(_allApps) { layout, _ -> layout.settings }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings())

    val homeApps: StateFlow<List<InstalledApp>> = combine(_allApps, _storedLayout) { apps, layout ->
        if (apps.isEmpty()) return@combine emptyList()
        val appMap = apps.associateBy { it.componentKey }
        val pinned = layout.homePackages.mapNotNull { appMap[it] }
        if (pinned.isNotEmpty() || layout.hasInitializedLayout) pinned else apps
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dockApps: StateFlow<List<InstalledApp>> = combine(_allApps, _storedLayout) { apps, layout ->
        if (apps.isEmpty()) return@combine emptyList()
        val appMap = apps.associateBy { it.componentKey }
        layout.dockPackages.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val appsByCategory: StateFlow<Map<AppCategory, List<InstalledApp>>> = _allApps
        .combine(_storedLayout) { apps, _ ->
            apps.groupBy { it.category }.toSortedMap(compareBy { it.ordinal })
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _overlay = MutableStateFlow<LauncherOverlay>(LauncherOverlay.None)
    val overlay: StateFlow<LauncherOverlay> = _overlay.asStateFlow()

    private val _selectedAppForMenu = MutableStateFlow<InstalledApp?>(null)
    val selectedAppForMenu: StateFlow<InstalledApp?> = _selectedAppForMenu.asStateFlow()

    private val _batteryStorage = MutableStateFlow(systemRepo.readBatteryAndStorage())
    val batteryStorage: StateFlow<BatteryStorageState> = _batteryStorage.asStateFlow()

    private val _controlCenter = MutableStateFlow(systemRepo.readControlCenterState())
    val controlCenter: StateFlow<ControlCenterState> = _controlCenter.asStateFlow()

    private val _weather = MutableStateFlow(WeatherState())
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()

    private val _superIsland = MutableStateFlow(SuperIslandState())
    val superIsland: StateFlow<SuperIslandState> = _superIsland.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchContacts = MutableStateFlow<List<ContactResult>>(emptyList())
    val searchContacts: StateFlow<List<ContactResult>> = _searchContacts.asStateFlow()

    private val _hasContactsPermission = MutableStateFlow(systemRepo.hasContactsPermission())
    val hasContactsPermission: StateFlow<Boolean> = _hasContactsPermission.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(systemRepo.isDefaultLauncher())
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    val searchApps: StateFlow<List<InstalledApp>> = combine(_allApps, _searchQuery) { apps, q ->
        val trimmed = q.trim()
        if (trimmed.isEmpty()) {
            apps.take(8)
        } else {
            apps.filter {
                it.label.contains(trimmed, ignoreCase = true) ||
                    it.packageName.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val mathResult: StateFlow<String?> = _searchQuery
        .combine(_allApps) { q, _ -> MathEvaluator.evaluate(q) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var timerJob: Job? = null
    private var contactsSearchJob: Job? = null
    private var lastLoadedCity: String? = null

    init {
        viewModelScope.launch {
            prefsStore.storedDataFlow.collect { stored ->
                _storedLayout.value = stored
                if (lastLoadedCity != stored.settings.weatherCityName) {
                    lastLoadedCity = stored.settings.weatherCityName
                    refreshWeatherByCity(stored.settings.weatherCityName)
                }
            }
        }
        loadApps(initialBootstrap = true)
        refreshSystemStates()
    }

    fun loadApps(initialBootstrap: Boolean = false) {
        viewModelScope.launch {
            if (initialBootstrap) _isLoadingApps.value = true
            val installed = appRepo.loadInstalledApps()
            _allApps.value = installed
            _isLoadingApps.value = false

            val currentPrefs = prefsStore.storedDataFlow.first()
            if (!currentPrefs.hasInitializedLayout && installed.isNotEmpty()) {
                val defaultDock = pickSmartDock(installed)
                val dockKeys = defaultDock.map { it.componentKey }.toSet()
                val defaultHome = installed
                    .filterNot { it.componentKey in dockKeys }
                    .map { it.componentKey }
                prefsStore.saveLayout(
                    homePackages = defaultHome,
                    dockPackages = defaultDock.map { it.componentKey }
                )
            }
        }
    }

    private fun pickSmartDock(apps: List<InstalledApp>): List<InstalledApp> {
        val priorities = listOf("dialer", "phone", "message", "sms", "chrome", "browser", "camera", "settings")
        val picked = mutableListOf<InstalledApp>()
        for (keyword in priorities) {
            if (picked.size >= 4) break
            val match = apps.firstOrNull { app ->
                app !in picked && (
                    app.packageName.contains(keyword, ignoreCase = true) ||
                        app.label.contains(keyword, ignoreCase = true)
                    )
            }
            if (match != null) picked.add(match)
        }
        if (picked.size < 4) {
            for (app in apps) {
                if (picked.size >= 4) break
                if (app !in picked) picked.add(app)
            }
        }
        return picked
    }

    fun onPackageChanged(packageName: String?) {
        packageName?.let { appRepo.clearIconCacheForPackage(it) }
        loadApps(initialBootstrap = false)
    }

    fun refreshSystemStates(batteryIntent: Intent? = null) {
        val bat = systemRepo.readBatteryAndStorage(batteryIntent)
        _batteryStorage.value = bat
        val cc = systemRepo.readControlCenterState()
        val mediaPlaying = systemRepo.isMediaPlaying()
        _controlCenter.value = cc
        _isDefaultLauncher.value = systemRepo.isDefaultLauncher()
        _hasContactsPermission.value = systemRepo.hasContactsPermission()
        _superIsland.update { current ->
            val updated = current.copy(
                mediaPlaying = if (mediaPlaying) true else current.mediaPlaying,
                isMediaActive = current.isMediaActive || mediaPlaying
            )
            recomputeIslandMode(updated, bat)
        }
    }

    private fun recomputeIslandMode(
        state: SuperIslandState,
        battery: BatteryStorageState = _batteryStorage.value
    ): SuperIslandState {
        val mode = when {
            state.isTimerRunning || (state.timerRemainingSeconds < state.timerTotalSeconds && state.timerRemainingSeconds > 0) ->
                SuperIslandMode.Timer(
                    totalSeconds = state.timerTotalSeconds,
                    remainingSeconds = state.timerRemainingSeconds,
                    isRunning = state.isTimerRunning
                )
            state.isMediaActive ->
                SuperIslandMode.Media(
                    trackTitle = state.mediaLabel,
                    sourceLabel = if (state.mediaPlaying) "Playing" else "Paused",
                    isPlaying = state.mediaPlaying
                )
            battery.isCharging ->
                SuperIslandMode.Charging(
                    percent = battery.batteryPercent,
                    statusLabel = battery.chargingLabel
                )
            else -> SuperIslandMode.Idle
        }
        return state.copy(mode = mode)
    }

    fun setOverlay(target: LauncherOverlay) {
        if (target == LauncherOverlay.ControlCenter) {
            refreshSystemStates()
        }
        if (target == LauncherOverlay.SpotlightSearch) {
            _hasContactsPermission.value = systemRepo.hasContactsPermission()
        }
        _overlay.value = target
    }

    fun dismissOverlay() {
        _overlay.value = LauncherOverlay.None
        _searchQuery.value = ""
        _searchContacts.value = emptyList()
    }

    fun openAppMenu(app: InstalledApp) {
        _selectedAppForMenu.value = app
    }

    fun closeAppMenu() {
        _selectedAppForMenu.value = null
    }

    fun launchApp(app: InstalledApp) {
        _selectedAppForMenu.value = null
        dismissOverlay()
        appRepo.launchApp(app)
    }

    fun openAppInfo(app: InstalledApp) {
        _selectedAppForMenu.value = null
        appRepo.openAppInfo(app.packageName)
    }

    fun uninstallApp(app: InstalledApp) {
        _selectedAppForMenu.value = null
        appRepo.requestUninstall(app.packageName)
    }

    fun isPinnedToHome(app: InstalledApp): Boolean =
        _storedLayout.value.homePackages.contains(app.componentKey)

    fun isPinnedToDock(app: InstalledApp): Boolean =
        _storedLayout.value.dockPackages.contains(app.componentKey)

    fun togglePinToHome(app: InstalledApp) {
        _selectedAppForMenu.value = null
        viewModelScope.launch {
            val current = _storedLayout.value
            val home = current.homePackages.toMutableList()
            if (home.contains(app.componentKey)) {
                home.remove(app.componentKey)
            } else {
                home.add(0, app.componentKey)
            }
            prefsStore.saveLayout(home, current.dockPackages)
        }
    }

    fun togglePinToDock(app: InstalledApp) {
        _selectedAppForMenu.value = null
        viewModelScope.launch {
            val current = _storedLayout.value
            val dock = current.dockPackages.toMutableList()
            val home = current.homePackages.toMutableList()
            if (dock.contains(app.componentKey)) {
                dock.remove(app.componentKey)
                if (!home.contains(app.componentKey)) home.add(0, app.componentKey)
            } else {
                if (dock.size >= 4) {
                    val displaced = dock.removeAt(dock.lastIndex)
                    if (!home.contains(displaced)) home.add(0, displaced)
                }
                home.remove(app.componentKey)
                dock.add(app.componentKey)
            }
            prefsStore.saveLayout(home, dock)
        }
    }

    // Super Island Controls
    fun toggleSuperIslandExpanded() {
        _superIsland.update { it.copy(isExpanded = !it.isExpanded) }
    }

    fun collapseSuperIsland() {
        _superIsland.update { it.copy(isExpanded = false) }
    }

    fun startTimerPreset(seconds: Int) {
        timerJob?.cancel()
        _superIsland.update { state ->
            recomputeIslandMode(
                state.copy(
                    timerTotalSeconds = seconds,
                    timerRemainingSeconds = seconds,
                    isTimerRunning = true
                )
            )
        }
        launchTimerLoop()
    }

    fun toggleTimerRunning() {
        val current = _superIsland.value
        if (current.isTimerRunning) {
            timerJob?.cancel()
            _superIsland.update { recomputeIslandMode(it.copy(isTimerRunning = false)) }
        } else {
            val remaining = if (current.timerRemainingSeconds <= 0) current.timerTotalSeconds else current.timerRemainingSeconds
            _superIsland.update {
                recomputeIslandMode(it.copy(timerRemainingSeconds = remaining, isTimerRunning = true))
            }
            launchTimerLoop()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _superIsland.update {
            recomputeIslandMode(
                it.copy(
                    isTimerRunning = false,
                    timerRemainingSeconds = it.timerTotalSeconds
                )
            )
        }
    }

    private fun launchTimerLoop() {
        timerJob = viewModelScope.launch {
            while (_superIsland.value.isTimerRunning && _superIsland.value.timerRemainingSeconds > 0) {
                delay(1000L)
                _superIsland.update { state ->
                    val next = (state.timerRemainingSeconds - 1).coerceAtLeast(0)
                    val stillRunning = next > 0 && state.isTimerRunning
                    recomputeIslandMode(
                        state.copy(
                            timerRemainingSeconds = next,
                            isTimerRunning = stillRunning
                        )
                    )
                }
            }
        }
    }

    fun toggleMediaPlayback() {
        systemRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        _superIsland.update {
            val nextPlaying = !it.mediaPlaying
            recomputeIslandMode(
                it.copy(
                    isMediaActive = true,
                    mediaPlaying = nextPlaying,
                    mediaLabel = "System Media"
                )
            )
        }
    }

    fun nextMediaTrack() {
        systemRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        _superIsland.update {
            recomputeIslandMode(it.copy(isMediaActive = true, mediaPlaying = true))
        }
    }

    fun previousMediaTrack() {
        systemRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        _superIsland.update {
            recomputeIslandMode(it.copy(isMediaActive = true, mediaPlaying = true))
        }
    }

    // Control Center Actions
    fun toggleTorch() {
        val newState = systemRepo.toggleTorch()
        _controlCenter.update { it.copy(isTorchOn = newState) }
    }

    fun updateBrightness(fraction: Float, activity: Activity?) {
        systemRepo.setBrightness(fraction, activity)
        _controlCenter.update { it.copy(brightness = fraction.coerceIn(0.05f, 1f)) }
    }

    fun updateVolume(fraction: Float) {
        systemRepo.setMediaVolume(fraction)
        _controlCenter.update { it.copy(volume = fraction.coerceIn(0f, 1f)) }
    }

    fun cycleRingerMode() {
        val nextMode = systemRepo.cycleRingerMode()
        _controlCenter.update { it.copy(ringerMode = nextMode) }
    }

    fun openConnectivityPanel(isWifi: Boolean) = systemRepo.openConnectivityPanel(isWifi)
    fun openBluetoothSettings() = systemRepo.openBluetoothSettings()
    fun openSystemSettings() = systemRepo.openSystemSettings()
    fun openDefaultHomeSettings() = systemRepo.openDefaultHomeSettings()

    // Spotlight Search
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        contactsSearchJob?.cancel()
        if (query.trim().length >= 2 && _hasContactsPermission.value) {
            contactsSearchJob = viewModelScope.launch {
                delay(120L)
                _searchContacts.value = systemRepo.searchContacts(query)
            }
        } else {
            _searchContacts.value = emptyList()
        }
    }

    fun refreshContactsPermission() {
        _hasContactsPermission.value = systemRepo.hasContactsPermission()
        if (_hasContactsPermission.value && _searchQuery.value.trim().length >= 2) {
            updateSearchQuery(_searchQuery.value)
        }
    }

    fun dialContact(phone: String) = systemRepo.dialNumber(phone)
    fun smsContact(phone: String) = systemRepo.smsNumber(phone)
    fun searchWeb(query: String) {
        dismissOverlay()
        systemRepo.searchWeb(query)
    }

    // Weather
    fun refreshWeatherByCity(city: String) {
        viewModelScope.launch {
            _weather.update { it.copy(isLoading = true, city = city) }
            val result = weatherRepo.fetchWeatherByCity(city)
            _weather.value = result
            if (result.error == null) {
                prefsStore.updateSettings { it.copy(weatherCityName = result.city) }
            }
        }
    }

    fun refreshWeatherByLocation() {
        viewModelScope.launch {
            _weather.update { it.copy(isLoading = true) }
            val coords = systemRepo.getApproximateCoordinates()
            if (coords != null) {
                val result = weatherRepo.fetchWeatherByCoordinates(
                    latitude = coords.first,
                    longitude = coords.second,
                    displayCity = coords.third
                )
                _weather.value = result
                if (result.error == null) {
                    prefsStore.updateSettings { it.copy(weatherCityName = result.city) }
                }
            } else {
                refreshWeatherByCity(settings.value.weatherCityName)
            }
        }
    }

    // Settings & Customization
    fun setGridColumns(columns: Int) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(gridColumns = columns) } }
    }

    fun setIconShape(shape: IconShapeStyle) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(iconShape = shape) } }
    }

    fun setClockStyle(style: ClockStyle) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(clockStyle = style) } }
    }

    fun toggleAppDrawerEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(showAppDrawer = enabled) } }
    }

    fun toggleAppLibraryEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(showAppLibraryPage = enabled) } }
    }

    fun toggleWidget(clock: Boolean? = null, weather: Boolean? = null, battery: Boolean? = null) {
        viewModelScope.launch {
            prefsStore.updateSettings { s ->
                s.copy(
                    showClockWidget = clock ?: s.showClockWidget,
                    showWeatherWidget = weather ?: s.showWeatherWidget,
                    showBatteryWidget = battery ?: s.showBatteryWidget
                )
            }
        }
    }

    fun toggle24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(is24HourFormat = is24Hour) } }
    }

    fun toggleDynamicColors(enabled: Boolean) {
        viewModelScope.launch { prefsStore.updateSettings { it.copy(useDynamicColors = enabled) } }
    }

    fun selectWallpaperPreset(preset: WallpaperPreset) {
        viewModelScope.launch {
            prefsStore.updateSettings { it.copy(wallpaperPreset = preset) }
        }
    }

    fun setCustomWallpaperUri(uriString: String) {
        viewModelScope.launch {
            prefsStore.updateSettings {
                it.copy(
                    wallpaperPreset = WallpaperPreset.CUSTOM_PHOTO,
                    customWallpaperUri = uriString
                )
            }
        }
    }

    fun resetHomeLayout() {
        viewModelScope.launch {
            prefsStore.resetLayoutFlag()
            loadApps(initialBootstrap = false)
        }
    }
}

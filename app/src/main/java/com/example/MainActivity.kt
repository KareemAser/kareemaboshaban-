package com.example

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.LauncherOverlay
import com.example.model.WallpaperPreset
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.ControlCenterOverlay
import com.example.ui.components.EditModeBar
import com.example.ui.components.HomeWorkspace
import com.example.ui.components.LauncherSettingsOverlay
import com.example.ui.components.SpotlightSearchOverlay
import com.example.ui.components.SuperIslandPill
import com.example.ui.components.WallpaperPickerOverlay
import com.example.ui.theme.AetherLauncherTheme
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            AetherLauncherTheme(useDynamicColor = settings.useDynamicColors) {
                AetherLauncherRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AetherLauncherRoot(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val homeApps by viewModel.homeApps.collectAsStateWithLifecycle()
    val dockApps by viewModel.dockApps.collectAsStateWithLifecycle()
    val appsByCategory by viewModel.appsByCategory.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val overlay by viewModel.overlay.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedAppForMenu.collectAsStateWithLifecycle()
    val batteryStorage by viewModel.batteryStorage.collectAsStateWithLifecycle()
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val superIsland by viewModel.superIsland.collectAsStateWithLifecycle()
    val controlCenter by viewModel.controlCenter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchApps by viewModel.searchApps.collectAsStateWithLifecycle()
    val searchContacts by viewModel.searchContacts.collectAsStateWithLifecycle()
    val mathResult by viewModel.mathResult.collectAsStateWithLifecycle()
    val hasContactsPerm by viewModel.hasContactsPermission.collectAsStateWithLifecycle()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()

    // Runtime Permission Launchers (Contacts & Coarse Location)
    val contactsPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.refreshContactsPermission()
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.refreshWeatherByLocation()
        }
    }

    // Zero-permission Photo Picker for Custom Wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.setCustomWallpaperUri(uri.toString())
        }
    }

    // Lifecycle-aware Battery & Package Receivers
    DisposableEffect(context) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.refreshSystemStates(intent)
            }
        }
        val packageReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.onPackageChanged(intent?.data?.schemeSpecificPart)
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(packageReceiver, pkgFilter)

        onDispose {
            runCatching { context.unregisterReceiver(batteryReceiver) }
            runCatching { context.unregisterReceiver(packageReceiver) }
        }
    }

    // BackHandler for every active overlay or expanded Super Island
    BackHandler(enabled = overlay != LauncherOverlay.None || superIsland.isExpanded || selectedApp != null) {
        when {
            selectedApp != null -> viewModel.closeAppMenu()
            superIsland.isExpanded -> viewModel.collapseSuperIsland()
            overlay != LauncherOverlay.None -> viewModel.dismissOverlay()
        }
    }

    val hasBlurOverlay = overlay != LauncherOverlay.None && overlay != LauncherOverlay.EditMode
    val backdropModifier = if (hasBlurOverlay && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.fillMaxSize().blur(18.dp)
    } else {
        Modifier.fillMaxSize()
    }

    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }
    var dragStartX by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper Layer
        Box(modifier = backdropModifier) {
            WallpaperBackdrop(
                preset = settings.wallpaperPreset,
                customUri = settings.customWallpaperUri
            )
            // Subtle legibility scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ObsidianCanvas.copy(alpha = 0.25f),
                                Color.Transparent,
                                ObsidianCanvas.copy(alpha = 0.42f)
                            )
                        )
                    )
            )
        }

        // Main Interactive Home Shell + Gesture Detector
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .pointerInput(overlay, settings.showAppDrawer) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            dragAccumulatedY = 0f
                            dragStartX = offset.x
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragAccumulatedY += dragAmount
                        },
                        onDragEnd = {
                            if (overlay == LauncherOverlay.None) {
                                when {
                                    dragAccumulatedY < -110f && settings.showAppDrawer -> {
                                        viewModel.setOverlay(LauncherOverlay.AppDrawer)
                                    }
                                    dragAccumulatedY > 110f -> {
                                        val isRightSide = dragStartX > size.width * 0.62f
                                        viewModel.setOverlay(
                                            if (isRightSide) LauncherOverlay.ControlCenter
                                            else LauncherOverlay.SpotlightSearch
                                        )
                                    }
                                }
                            }
                            dragAccumulatedY = 0f
                        }
                    )
                }
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            SuperIslandPill(
                state = superIsland,
                battery = batteryStorage,
                onToggleExpand = viewModel::toggleSuperIslandExpanded,
                onStartTimerPreset = viewModel::startTimerPreset,
                onToggleTimer = viewModel::toggleTimerRunning,
                onResetTimer = viewModel::resetTimer,
                onToggleMedia = viewModel::toggleMediaPlayback,
                onNextTrack = viewModel::nextMediaTrack,
                onPrevTrack = viewModel::previousMediaTrack,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            HomeWorkspace(
                homeApps = homeApps,
                dockApps = dockApps,
                appsByCategory = appsByCategory,
                settings = settings,
                weather = weather,
                battery = batteryStorage,
                isLoadingApps = isLoadingApps,
                isEditMode = overlay == LauncherOverlay.EditMode,
                onLaunchApp = viewModel::launchApp,
                onLongPressApp = viewModel::openAppMenu,
                onLongPressEmpty = { viewModel.setOverlay(LauncherOverlay.EditMode) },
                onOpenSearch = { viewModel.setOverlay(LauncherOverlay.SpotlightSearch) },
                onOpenDrawer = { viewModel.setOverlay(LauncherOverlay.AppDrawer) },
                onOpenControlCenter = { viewModel.setOverlay(LauncherOverlay.ControlCenter) },
                onCycleClockStyle = viewModel::setClockStyle,
                onRefreshWeather = { viewModel.refreshWeatherByCity(settings.weatherCityName) },
                onRequestLocationWeather = {
                    locationPermLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                onUpdateCity = viewModel::refreshWeatherByCity,
                modifier = Modifier.weight(1f)
            )
        }

        // HyperOS 4 App Drawer Overlay
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.AppDrawer,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            AppDrawerSheet(
                apps = allApps,
                gridColumns = settings.gridColumns,
                iconShape = settings.iconShape,
                onLaunchApp = viewModel::launchApp,
                onLongPressApp = viewModel::openAppMenu,
                onDismiss = viewModel::dismissOverlay
            )
        }

        // Global Spotlight Search Overlay
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.SpotlightSearch,
            enter = slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut()
        ) {
            SpotlightSearchOverlay(
                query = searchQuery,
                matchingApps = searchApps,
                matchingContacts = searchContacts,
                mathResult = mathResult,
                hasContactsPermission = hasContactsPerm,
                iconShape = settings.iconShape,
                onQueryChange = viewModel::updateSearchQuery,
                onLaunchApp = viewModel::launchApp,
                onLongPressApp = viewModel::openAppMenu,
                onRequestContactsPermission = {
                    contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS)
                },
                onDialContact = viewModel::dialContact,
                onSmsContact = viewModel::smsContact,
                onSearchWeb = viewModel::searchWeb,
                onDismiss = viewModel::dismissOverlay
            )
        }

        // Control Center Overlay
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.ControlCenter,
            enter = slideInVertically(initialOffsetY = { -it / 3 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut()
        ) {
            ControlCenterOverlay(
                state = controlCenter,
                islandState = superIsland,
                onBrightnessChange = { viewModel.updateBrightness(it, activity) },
                onVolumeChange = viewModel::updateVolume,
                onToggleTorch = viewModel::toggleTorch,
                onCycleRinger = viewModel::cycleRingerMode,
                onOpenWifiPanel = { viewModel.openConnectivityPanel(isWifi = true) },
                onOpenInternetPanel = { viewModel.openConnectivityPanel(isWifi = false) },
                onOpenBluetooth = viewModel::openBluetoothSettings,
                onOpenSystemSettings = viewModel::openSystemSettings,
                onToggleMedia = viewModel::toggleMediaPlayback,
                onNextTrack = viewModel::nextMediaTrack,
                onPrevTrack = viewModel::previousMediaTrack,
                onStartQuickTimer = {
                    viewModel.startTimerPreset(300)
                    viewModel.dismissOverlay()
                },
                onOpenWallpaperStudio = { viewModel.setOverlay(LauncherOverlay.WallpaperPicker) },
                onOpenLauncherSettings = { viewModel.setOverlay(LauncherOverlay.LauncherSettings) },
                onDismiss = viewModel::dismissOverlay
            )
        }

        // Edit Mode Toolbar
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.EditMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            EditModeBar(
                settings = settings,
                onToggleClockWidget = { viewModel.toggleWidget(clock = it) },
                onToggleWeatherWidget = { viewModel.toggleWidget(weather = it) },
                onToggleBatteryWidget = { viewModel.toggleWidget(battery = it) },
                onOpenWallpaperPicker = { viewModel.setOverlay(LauncherOverlay.WallpaperPicker) },
                onOpenSettings = { viewModel.setOverlay(LauncherOverlay.LauncherSettings) },
                onDone = viewModel::dismissOverlay
            )
        }

        // Wallpaper Studio Overlay
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.WallpaperPicker,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            WallpaperPickerOverlay(
                currentPreset = settings.wallpaperPreset,
                onSelectPreset = viewModel::selectWallpaperPreset,
                onPickFromGallery = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onDismiss = viewModel::dismissOverlay
            )
        }

        // Launcher Settings Overlay
        AnimatedVisibility(
            visible = overlay == LauncherOverlay.LauncherSettings,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LauncherSettingsOverlay(
                settings = settings,
                isDefaultLauncher = isDefaultLauncher,
                onSetGridColumns = viewModel::setGridColumns,
                onSetIconShape = viewModel::setIconShape,
                onSetClockStyle = viewModel::setClockStyle,
                onToggleAppDrawer = viewModel::toggleAppDrawerEnabled,
                onToggleAppLibrary = viewModel::toggleAppLibraryEnabled,
                onToggle24Hour = viewModel::toggle24HourFormat,
                onToggleDynamicColors = viewModel::toggleDynamicColors,
                onOpenWallpaperStudio = { viewModel.setOverlay(LauncherOverlay.WallpaperPicker) },
                onOpenDefaultHomeSettings = viewModel::openDefaultHomeSettings,
                onResetLayout = viewModel::resetHomeLayout,
                onDismiss = viewModel::dismissOverlay
            )
        }

        // App Context Menu Dialog
        selectedApp?.let { app ->
            AppContextMenuDialog(
                app = app,
                isPinnedToHome = viewModel.isPinnedToHome(app),
                isPinnedToDock = viewModel.isPinnedToDock(app),
                onDismiss = viewModel::closeAppMenu,
                onLaunch = { viewModel.launchApp(app) },
                onToggleHome = { viewModel.togglePinToHome(app) },
                onToggleDock = { viewModel.togglePinToDock(app) },
                onAppInfo = { viewModel.openAppInfo(app) },
                onUninstall = { viewModel.uninstallApp(app) }
            )
        }
    }
}

@Composable
private fun WallpaperBackdrop(
    preset: WallpaperPreset,
    customUri: String?
) {
    when (preset) {
        WallpaperPreset.LIQUID_GLASS -> {
            Image(
                painter = painterResource(id = R.drawable.img_wall_liquid_glass_1794742222253),
                contentDescription = "Liquid Glass Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperPreset.HYPER_AURORA -> {
            Image(
                painter = painterResource(id = R.drawable.img_wall_hyper_aurora_1794742222966),
                contentDescription = "Hyper Aurora Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperPreset.OBSIDIAN_DUNES -> {
            Image(
                painter = painterResource(id = R.drawable.img_wall_obsidian_dunes_1794742224328),
                contentDescription = "Obsidian Dunes Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        WallpaperPreset.CUSTOM_PHOTO -> {
            if (!customUri.isNullOrBlank()) {
                AsyncImage(
                    model = customUri,
                    contentDescription = "Custom Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_wall_liquid_glass_1794742222253),
                    contentDescription = "Default Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        WallpaperPreset.SYSTEM_LIVE -> {
            // Transparent backdrop revealing Android system wallpaper behind windowShowWallpaper
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent))
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.AppCategory
import com.example.model.BatteryStorageState
import com.example.model.ClockStyle
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.WeatherState
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeWorkspace(
    homeApps: List<InstalledApp>,
    dockApps: List<InstalledApp>,
    appsByCategory: Map<AppCategory, List<InstalledApp>>,
    settings: LauncherSettings,
    weather: WeatherState,
    battery: BatteryStorageState,
    isLoadingApps: Boolean,
    isEditMode: Boolean,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onLongPressEmpty: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onCycleClockStyle: (ClockStyle) -> Unit,
    onRefreshWeather: () -> Unit,
    onRequestLocationWeather: () -> Unit,
    onUpdateCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val adaptiveColumns = if (maxWidth >= 600.dp) {
            (settings.gridColumns + 2).coerceAtMost(8)
        } else {
            settings.gridColumns.coerceIn(4, 5)
        }
        val widgetCount = listOf(
            settings.showClockWidget,
            settings.showWeatherWidget || settings.showBatteryWidget
        ).count { it }
        val firstPageCapacity = when (widgetCount) {
            2 -> adaptiveColumns * 2
            1 -> adaptiveColumns * 3
            else -> adaptiveColumns * 4
        }
        val regularPageCapacity = adaptiveColumns * 5

        val pages = remember(homeApps, firstPageCapacity, regularPageCapacity) {
            if (homeApps.isEmpty()) {
                listOf(emptyList())
            } else {
                val result = mutableListOf<List<InstalledApp>>()
                result.add(homeApps.take(firstPageCapacity))
                var index = firstPageCapacity
                while (index < homeApps.size) {
                    val end = (index + regularPageCapacity).coerceAtMost(homeApps.size)
                    result.add(homeApps.subList(index, end))
                    index = end
                }
                result
            }
        }

        val totalPages = pages.size + if (settings.showAppLibraryPage) 1 else 0
        val pagerState = rememberPagerState(pageCount = { totalPages })

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("home_horizontal_pager")
            ) { pageIndex ->
                if (settings.showAppLibraryPage && pageIndex == pages.size) {
                    AppLibraryPage(
                        appsByCategory = appsByCategory,
                        settings = settings,
                        onLaunchApp = onLaunchApp,
                        onLongPressApp = onLongPressApp,
                        onOpenDrawer = onOpenDrawer
                    )
                } else {
                    HomeGridPage(
                        pageIndex = pageIndex,
                        apps = pages.getOrElse(pageIndex) { emptyList() },
                        columns = adaptiveColumns,
                        settings = settings,
                        weather = weather,
                        battery = battery,
                        isLoadingApps = isLoadingApps && pageIndex == 0,
                        isEditMode = isEditMode,
                        onLaunchApp = onLaunchApp,
                        onLongPressApp = onLongPressApp,
                        onLongPressEmpty = onLongPressEmpty,
                        onCycleClockStyle = onCycleClockStyle,
                        onRefreshWeather = onRefreshWeather,
                        onRequestLocationWeather = onRequestLocationWeather,
                        onUpdateCity = onUpdateCity
                    )
                }
            }

            // Page indicator dots + Quick Controls bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                repeat(totalPages) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 7.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dot_w"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) TextPrimary else Color.White.copy(alpha = 0.35f),
                        label = "dot_c"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Search Pill & Floating Dock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                ) {
                    LiquidGlassBox(
                        cornerRadius = 50.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(onClick = onOpenSearch)
                            .testTag("home_search_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Spotlight Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spotlight Search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    if (settings.showAppDrawer) {
                        LiquidGlassBox(
                            cornerRadius = 50.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(onClick = onOpenDrawer)
                                .testTag("home_drawer_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = "Open App Drawer",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    LiquidGlassBox(
                        cornerRadius = 50.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onOpenControlCenter)
                            .testTag("home_control_center_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Open Control Center",
                                tint = HyperBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frosted Dock
                LiquidGlassBox(
                    cornerRadius = 30.dp,
                    elevated = true,
                    modifier = Modifier
                        .widthIn(max = 580.dp)
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("home_dock")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (dockApps.isEmpty()) {
                            Text(
                                text = "Long-press any app to pin to Dock",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 14.dp)
                            )
                        } else {
                            dockApps.forEach { app ->
                                AppIconItem(
                                    app = app,
                                    iconShape = settings.iconShape,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                    iconSize = 54.dp,
                                    showLabel = false,
                                    isEditMode = isEditMode,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeGridPage(
    pageIndex: Int,
    apps: List<InstalledApp>,
    columns: Int,
    settings: LauncherSettings,
    weather: WeatherState,
    battery: BatteryStorageState,
    isLoadingApps: Boolean,
    isEditMode: Boolean,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onLongPressEmpty: () -> Unit,
    onCycleClockStyle: (ClockStyle) -> Unit,
    onRefreshWeather: () -> Unit,
    onRequestLocationWeather: () -> Unit,
    onUpdateCity: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .combinedClickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongPressEmpty
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (pageIndex == 0) {
            if (settings.showClockWidget) {
                SpatialDepthClockWidget(
                    style = settings.clockStyle,
                    is24Hour = settings.is24HourFormat,
                    onCycleStyle = {
                        val all = ClockStyle.entries
                        val next = all[(all.indexOf(settings.clockStyle) + 1) % all.size]
                        onCycleClockStyle(next)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (settings.showWeatherWidget || settings.showBatteryWidget) {
                WeatherAndDeviceWidgetRow(
                    weather = weather,
                    battery = battery,
                    showWeather = settings.showWeatherWidget,
                    showBattery = settings.showBatteryWidget,
                    onRefreshWeather = onRefreshWeather,
                    onRequestLocationWeather = onRequestLocationWeather,
                    onUpdateCity = onUpdateCity
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        if (isLoadingApps) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CircularProgressIndicator(color = HyperBlue)
            }
        } else if (apps.isEmpty() && pageIndex == 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LiquidGlassBox(cornerRadius = 24.dp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = "Empty home grid",
                            tint = HyperBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Clean Minimal Home",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Swipe up or use App Library to pin your favorite apps.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            val rows = ceil(apps.size.toFloat() / columns.toFloat()).toInt()
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (c in 0 until columns) {
                            val idx = r * columns + c
                            if (idx < apps.size) {
                                val app = apps[idx]
                                AppIconItem(
                                    app = app,
                                    iconShape = settings.iconShape,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                    isEditMode = isEditMode,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLibraryPage(
    appsByCategory: Map<AppCategory, List<InstalledApp>>,
    settings: LauncherSettings,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onOpenDrawer: () -> Unit
) {
    var expandedCategory by remember { mutableStateOf<AppCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "App Library",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Surface(
                onClick = onOpenDrawer,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.testTag("library_all_apps_chip")
            ) {
                Text(
                    text = "A–Z List",
                    style = MaterialTheme.typography.labelMedium,
                    color = HyperBlue,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(appsByCategory.entries.toList(), key = { it.key.name }) { (category, catApps) ->
                LiquidGlassBox(
                    cornerRadius = 26.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCategory = category }
                        .testTag("library_folder_${category.name}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val previewApps = catApps.take(4)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0 until 2) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (col in 0 until 2) {
                                        val item = previewApps.getOrNull(row * 2 + col)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(54.dp)
                                                .clip(iconShapeForStyle(settings.iconShape))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .clickable(enabled = item != null) {
                                                    if (row == 1 && col == 1 && catApps.size > 4) {
                                                        expandedCategory = category
                                                    } else {
                                                        item?.let(onLaunchApp)
                                                    }
                                                }
                                        ) {
                                            if (item?.iconBitmap != null) {
                                                Image(
                                                    bitmap = item.iconBitmap,
                                                    contentDescription = item.label,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${catApps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    expandedCategory?.let { category ->
        val folderApps = appsByCategory[category].orEmpty()
        Dialog(onDismissRequest = { expandedCategory = null }) {
            LiquidGlassBox(
                cornerRadius = 30.dp,
                elevated = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        IconButton(onClick = { expandedCategory = null }) {
                            Icon(
                                imageVector = Icons.Rounded.Apps,
                                contentDescription = "Close folder",
                                tint = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(320.dp)
                    ) {
                        items(folderApps, key = { it.componentKey }) { app ->
                            AppIconItem(
                                app = app,
                                iconShape = settings.iconShape,
                                onClick = {
                                    expandedCategory = null
                                    onLaunchApp(app)
                                },
                                onLongClick = { onLongPressApp(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

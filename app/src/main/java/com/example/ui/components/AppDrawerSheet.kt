package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AppDrawerSheet(
    apps: List<InstalledApp>,
    gridColumns: Int,
    iconShape: IconShapeStyle,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterQuery by remember { mutableStateOf("") }
    var activeLetter by remember { mutableStateOf<String?>(null) }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val filteredApps = remember(apps, filterQuery) {
        if (filterQuery.isBlank()) apps
        else apps.filter { it.label.contains(filterQuery.trim(), ignoreCase = true) }
    }

    val indexLetters = remember(filteredApps) {
        filteredApps.mapNotNull { it.label.firstOrNull()?.uppercaseChar()?.toString() }
            .distinct()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCanvas.copy(alpha = 0.84f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_drawer_sheet")
    ) {
        val adaptiveCols = if (maxWidth >= 600.dp) gridColumns + 2 else gridColumns

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Filter ${apps.size} apps…", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Filter apps",
                            tint = HyperBlue
                        )
                    },
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(onClick = { filterQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear filter",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HyperBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedContainerColor = Color.White.copy(alpha = 0.07f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drawer_search_input")
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("drawer_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close App Drawer",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (filteredApps.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No apps match \"$filterQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(adaptiveCols),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 26.dp, bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps, key = { it.componentKey }) { app ->
                            AppIconItem(
                                app = app,
                                iconShape = iconShape,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onLongPressApp(app) }
                            )
                        }
                    }
                }

                // A-Z Fast Scrubber Rail
                if (indexLetters.size > 3) {
                    var railHeight by remember { mutableStateOf(1) }
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(0.82f)
                            .width(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .onSizeChanged { railHeight = it.height.coerceAtLeast(1) }
                            .pointerInput(indexLetters) {
                                detectVerticalDragGestures(
                                    onDragEnd = { activeLetter = null },
                                    onDragCancel = { activeLetter = null }
                                ) { change, _ ->
                                    change.consume()
                                    val fraction = (change.position.y / railHeight.toFloat()).coerceIn(0f, 0.999f)
                                    val letterIdx = (fraction * indexLetters.size).toInt()
                                        .coerceIn(0, indexLetters.lastIndex)
                                    val letter = indexLetters[letterIdx]
                                    if (activeLetter != letter) {
                                        activeLetter = letter
                                        val targetAppIndex = filteredApps.indexOfFirst {
                                            it.label.startsWith(letter, ignoreCase = true)
                                        }
                                        if (targetAppIndex >= 0) {
                                            scope.launch { gridState.scrollToItem(targetAppIndex) }
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 6.dp)
                            .testTag("drawer_az_rail")
                    ) {
                        indexLetters.forEach { letter ->
                            Text(
                                text = letter,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = if (activeLetter == letter) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeLetter == letter) HyperBlue else TextSecondary
                            )
                        }
                    }
                }

                activeLetter?.let { letter ->
                    LiquidGlassBox(
                        cornerRadius = 20.dp,
                        elevated = true,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.displayMedium,
                                color = HyperBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

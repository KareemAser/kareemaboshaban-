package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryStorageState
import com.example.model.ClockStyle
import com.example.model.WeatherState
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.CyanFrost
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.HyperBlue
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpatialViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpatialDepthClockWidget(
    style: ClockStyle,
    is24Hour: Boolean,
    onCycleStyle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val dateObj = remember(nowMs) { Date(nowMs) }
    val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
    val timeText = remember(nowMs, is24Hour) {
        SimpleDateFormat(timePattern, Locale.ENGLISH).format(dateObj)
    }
    val amPmText = remember(nowMs, is24Hour) {
        if (is24Hour) "" else SimpleDateFormat("a", Locale.ENGLISH).format(dateObj)
    }
    val dateText = remember(nowMs) {
        SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(dateObj)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCycleStyle)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("clock_widget")
    ) {
        when (style) {
            ClockStyle.SPATIAL_DEPTH -> {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = dateText.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = HyperBlue,
                        letterSpacing = 1.4.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeText,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 62.sp,
                            lineHeight = 64.sp,
                            letterSpacing = (-2).sp,
                            style = MaterialTheme.typography.displayLarge.copy(
                                brush = Brush.verticalGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0.72f))
                                )
                            )
                        )
                        if (amPmText.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = amPmText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }

            ClockStyle.HYPER_BOLD -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 54.sp,
                        color = TextPrimary,
                        letterSpacing = (-1.5).sp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        if (amPmText.isNotEmpty()) {
                            Text(
                                text = amPmText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = SpatialViolet
                            )
                        }
                    }
                }
            }

            ClockStyle.MINIMAL_GLASS -> {
                LiquidGlassBox(
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Tap to change style",
                                style = MaterialTheme.typography.labelSmall,
                                color = HyperBlue
                            )
                        }
                        Text(
                            text = if (amPmText.isNotEmpty()) "$timeText $amPmText" else timeText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherAndDeviceWidgetRow(
    weather: WeatherState,
    battery: BatteryStorageState,
    showWeather: Boolean,
    showBattery: Boolean,
    onRefreshWeather: () -> Unit,
    onRequestLocationWeather: () -> Unit,
    onUpdateCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showWeather && !showBattery) return

    var showCityDialog by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        if (showWeather) {
            WeatherGlassWidget(
                weather = weather,
                onClick = { showCityDialog = true },
                onRefresh = onRefreshWeather,
                modifier = Modifier.weight(1f)
            )
        }
        if (showBattery) {
            BatteryStorageWidget(
                battery = battery,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showCityDialog) {
        var cityInput by remember(weather.city) { mutableStateOf(weather.city) }
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("Weather Location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("City Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weather_city_input")
                    )
                    TextButton(
                        onClick = {
                            showCityDialog = false
                            onRequestLocationWeather()
                        },
                        modifier = Modifier.testTag("weather_use_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Use location",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Current Location")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCityDialog = false
                        if (cityInput.isNotBlank()) onUpdateCity(cityInput.trim())
                    },
                    modifier = Modifier.testTag("weather_save_city_button")
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WeatherGlassWidget(
    weather: WeatherState,
    onClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassBox(
        cornerRadius = 24.dp,
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("weather_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = weatherIconForCode(weather.weatherCode),
                        contentDescription = weather.conditionLabel,
                        tint = if (weather.weatherCode <= 1) AmberWarm else CyanFrost,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = weather.city,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("weather_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh weather",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = weather.tempCelsius?.let { "${it}°" } ?: "--°",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = weather.conditionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val hiLo = if (weather.highTemp != null && weather.lowTemp != null) {
                        "H:${weather.highTemp}° L:${weather.lowTemp}°"
                    } else {
                        "Tap to set city"
                    }
                    Text(
                        text = hiLo,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = HyperBlue
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryStorageWidget(
    battery: BatteryStorageState,
    modifier: Modifier = Modifier
) {
    LiquidGlassBox(
        cornerRadius = 24.dp,
        modifier = modifier.testTag("battery_storage_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull
                        else Icons.Rounded.BatteryStd,
                        contentDescription = "Battery",
                        tint = if (battery.isCharging) EmeraldLive else HyperBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Device",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${battery.temperatureC.toInt()}°C",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${battery.batteryPercent}%",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = if (battery.isCharging) EmeraldLive else TextPrimary
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = battery.chargingLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = "Storage",
                            tint = SpatialViolet,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${battery.usedStorageGb.toInt()}/${battery.totalStorageGb.toInt()} GB",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = SpatialViolet
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { battery.batteryPercent / 100f },
                color = if (battery.isCharging) EmeraldLive else HyperBlue,
                trackColor = Color.White.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
            )
        }
    }
}

private fun weatherIconForCode(code: Int): ImageVector = when {
    code <= 1 -> Icons.Rounded.WbSunny
    code in 2..48 -> Icons.Rounded.Cloud
    code in 51..67 || code in 80..82 -> Icons.Rounded.WaterDrop
    code in 71..77 || code in 85..86 -> Icons.Rounded.AcUnit
    code >= 95 -> Icons.Rounded.Thunderstorm
    else -> Icons.Rounded.Cloud
}

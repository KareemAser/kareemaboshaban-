package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AetherDarkColorScheme = darkColorScheme(
    primary = HyperBlue,
    onPrimary = ObsidianCanvas,
    primaryContainer = HyperBlueGlow,
    onPrimaryContainer = TextPrimary,
    secondary = SpatialViolet,
    onSecondary = ObsidianCanvas,
    tertiary = EmeraldLive,
    onTertiary = ObsidianCanvas,
    background = ObsidianCanvas,
    onBackground = TextPrimary,
    surface = DeepSlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedGlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = SpecularRimTop,
    error = RoseAlert
)

@Composable
fun AetherLauncherTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        AetherDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AetherTypography,
        content = content
    )
}

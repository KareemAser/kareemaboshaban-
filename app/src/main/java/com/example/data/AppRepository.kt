package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppCategory
import com.example.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppRepository(private val context: Context) {
    private val pm: PackageManager = context.packageManager
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackage = context.packageName
        resolveInfos
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != selfPackage }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val key = "$pkg/$act"
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty { pkg }
                val appInfo = info.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val cachedIcon = iconCache[key] ?: runCatching {
                    drawableToImageBitmap(info.loadIcon(pm))
                }.getOrNull()?.also { iconCache[key] = it }

                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    category = categorizeApp(appInfo, pkg, label),
                    isSystemApp = isSystem,
                    iconBitmap = cachedIcon
                )
            }
            .distinctBy { it.componentKey }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launchApp(app: InstalledApp): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            val fallback = pm.getLaunchIntentForPackage(app.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (fallback != null) {
                context.startActivity(fallback)
                true
            } else {
                false
            }
        }
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun requestUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun clearIconCacheForPackage(packageName: String) {
        iconCache.keys.filter { it.startsWith("$packageName/") }.forEach { iconCache.remove(it) }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        val sizePx = 128
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width <= sizePx && bmp.height <= sizePx) {
                return bmp.asImageBitmap()
            }
            val scaled = Bitmap.createScaledBitmap(bmp, sizePx, sizePx, true)
            return scaled.asImageBitmap()
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }

    private fun categorizeApp(
        appInfo: ApplicationInfo,
        packageName: String,
        label: String
    ): AppCategory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.PRODUCTIVITY
                else -> Unit
            }
        }
        val combined = "$packageName $label".lowercase()
        return when {
            combined.contains("game") || combined.contains("play.games") -> AppCategory.GAMES
            combined.contains("whatsapp") || combined.contains("telegram") ||
                combined.contains("instagram") || combined.contains("message") ||
                combined.contains("dialer") || combined.contains("phone") ||
                combined.contains("contacts") || combined.contains("chat") -> AppCategory.SOCIAL
            combined.contains("camera") || combined.contains("gallery") ||
                combined.contains("photos") || combined.contains("music") ||
                combined.contains("youtube") || combined.contains("spotify") ||
                combined.contains("video") -> AppCategory.MEDIA
            combined.contains("docs") || combined.contains("calendar") ||
                combined.contains("mail") || combined.contains("gmail") ||
                combined.contains("notes") || combined.contains("drive") ||
                combined.contains("chrome") || combined.contains("browser") -> AppCategory.PRODUCTIVITY
            combined.contains("settings") || combined.contains("system") -> AppCategory.SYSTEM
            else -> AppCategory.UTILITIES
        }
    }
}

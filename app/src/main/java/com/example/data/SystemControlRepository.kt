package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.ContactsContract
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.example.model.BatteryStorageState
import com.example.model.ContactResult
import com.example.model.ControlCenterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class SystemControlRepository(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var torchCameraId: String? = findFlashCameraId()
    private var torchEnabled: Boolean = false

    private fun findFlashCameraId(): String? {
        return try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    fun readControlCenterState(): ControlCenterState {
        val currentBrightness = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
        } catch (_: Exception) {
            0.7f
        }.coerceIn(0.05f, 1f)

        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        val volumeFraction = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)

        var wifiOn = false
        var cellularOn = false
        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
            if (caps != null) {
                wifiOn = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                cellularOn = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
        } catch (_: Exception) {
        }

        val btOn = try {
            bluetoothManager?.adapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }

        val ringer = try {
            audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        } catch (_: Exception) {
            AudioManager.RINGER_MODE_NORMAL
        }

        return ControlCenterState(
            brightness = currentBrightness,
            volume = volumeFraction,
            isTorchOn = torchEnabled,
            hasTorch = torchCameraId != null,
            isWifiConnected = wifiOn,
            wifiSsidLabel = if (wifiOn) "Connected" else "Wi-Fi",
            isCellularConnected = cellularOn,
            isBluetoothOn = btOn,
            ringerMode = ringer
        )
    }

    fun toggleTorch(): Boolean {
        val camId = torchCameraId ?: findFlashCameraId()?.also { torchCameraId = it } ?: return false
        return try {
            val next = !torchEnabled
            cameraManager?.setTorchMode(camId, next)
            torchEnabled = next
            next
        } catch (_: Exception) {
            torchEnabled
        }
    }

    fun setBrightness(fraction: Float, activity: Activity?) {
        val clamped = fraction.coerceIn(0.05f, 1f)
        activity?.window?.let { window ->
            val attrs = window.attributes
            attrs.screenBrightness = clamped
            window.attributes = attrs
        }
        if (Settings.System.canWrite(context)) {
            runCatching {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    (clamped * 255).toInt().coerceIn(12, 255)
                )
            }
        }
    }

    fun setMediaVolume(fraction: Float) {
        val am = audioManager ?: return
        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = (fraction.coerceIn(0f, 1f) * maxVol).toInt()
        runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0) }
    }

    fun cycleRingerMode(): Int {
        val am = audioManager ?: return AudioManager.RINGER_MODE_NORMAL
        val next = when (am.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        return try {
            am.ringerMode = next
            next
        } catch (_: SecurityException) {
            am.ringerMode
        }
    }

    fun dispatchMediaKey(keyCode: Int) {
        val am = audioManager ?: return
        runCatching {
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    fun isMediaPlaying(): Boolean {
        return runCatching { audioManager?.isMusicActive == true }.getOrDefault(false)
    }

    fun openConnectivityPanel(isWifi: Boolean) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(
                if (isWifi) Settings.Panel.ACTION_WIFI else Settings.Panel.ACTION_INTERNET_CONNECTIVITY
            )
        } else {
            Intent(if (isWifi) Settings.ACTION_WIFI_SETTINGS else Settings.ACTION_WIRELESS_SETTINGS)
        }.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        runCatching { context.startActivity(intent) }
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun openSystemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun openDefaultHomeSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val res = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return res?.activityInfo?.packageName == context.packageName
    }

    fun readBatteryAndStorage(batteryIntent: Intent? = null): BatteryStorageState {
        val intent = batteryIntent ?: context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 85
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            plugged > 0
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
        val tempC = tempTenths / 10f

        val chargingLabel = when {
            status == BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged"
            plugged == BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charge"
            plugged == BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
            plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charge"
            isCharging -> "Charging"
            else -> "On Battery"
        }

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = stat.totalBytes.toDouble()
        val availBytes = stat.availableBytes.toDouble()
        val gb = 1024.0 * 1024.0 * 1024.0
        val totalGb = (totalBytes / gb).toFloat()
        val usedGb = ((totalBytes - availBytes) / gb).toFloat().coerceAtLeast(0f)

        return BatteryStorageState(
            batteryPercent = percent.coerceIn(0, 100),
            isCharging = isCharging,
            chargingLabel = chargingLabel,
            temperatureC = tempC,
            usedStorageGb = usedGb,
            totalStorageGb = totalGb
        )
    }

    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun searchContacts(query: String): List<ContactResult> = withContext(Dispatchers.IO) {
        if (!hasContactsPermission() || query.trim().length < 2) return@withContext emptyList()
        val results = mutableListOf<ContactResult>()
        val resolver: ContentResolver = context.contentResolver
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val args = arrayOf("%${query.trim()}%")
        runCatching {
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                selection,
                args,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext() && results.size < 6) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                    val num = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                    if (name.isNotBlank() && num.isNotBlank() && results.none { it.name == name }) {
                        results.add(ContactResult(id = id, name = name, phoneNumber = num))
                    }
                }
            }
        }
        results
    }

    fun dialNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun smsNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun searchWeb(query: String) {
        val encoded = Uri.encode(query.trim())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    @SuppressLint("MissingPermission")
    suspend fun getApproximateCoordinates(): Triple<Double, Double, String>? =
        withContext(Dispatchers.IO) {
            if (!hasCoarseLocationPermission()) return@withContext null
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext null
            val location = runCatching {
                lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }.getOrNull() ?: return@withContext null

            val cityName = runCatching {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale.ENGLISH)
                    .getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.locality
                    ?: addresses?.firstOrNull()?.adminArea
                    ?: "Current Area"
            }.getOrDefault("Current Area")

            Triple(location.latitude, location.longitude, cityName)
        }
}

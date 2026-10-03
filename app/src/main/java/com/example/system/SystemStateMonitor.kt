package com.example.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import com.example.model.SystemStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SystemStateMonitor(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _statusFlow = MutableStateFlow(SystemStatus())
    val statusFlow: StateFlow<SystemStatus> = _statusFlow.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private var torchCameraId: String? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateBatteryState(it) }
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                _statusFlow.value = _statusFlow.value.copy(isFlashlightOn = enabled)
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCell = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            _statusFlow.value = _statusFlow.value.copy(
                wifiConnected = hasWifi,
                cellularConnected = hasCell
            )
        }

        override fun onLost(network: Network) {
            checkNetworkState()
        }
    }

    fun start() {
        // Battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialBattery = context.registerReceiver(batteryReceiver, filter)
        initialBattery?.let { updateBatteryState(it) }

        // Camera / Flashlight setup
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        torchCameraId = id
                        break
                    }
                }
                cm.registerTorchCallback(torchCallback, null)
            }
        } catch (e: Exception) {
            // Ignored if camera permission or hardware unavailable
        }

        // Network monitoring
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fallback
        }

        // Time ticker & audio/brightness periodic update
        coroutineScope.launch {
            while (isActive) {
                updateTimeAndSystemValues()
                delay(1000)
            }
        }
    }

    fun stop() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}

        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {}

        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {}

        coroutineScope.cancel()
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            85
        }

        _statusFlow.value = _statusFlow.value.copy(
            batteryLevel = batteryPct,
            isCharging = isCharging
        )
    }

    private fun checkNetworkState() {
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCell = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        _statusFlow.value = _statusFlow.value.copy(
            wifiConnected = isWifi,
            cellularConnected = isCell
        )
    }

    private fun updateTimeAndSystemValues() {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        val currentVol = audioManager?.let { am ->
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat().coerceAtLeast(1f)
            cur / max
        } ?: 0.7f

        val ringer = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

        val brightness = try {
            val curB = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            curB / 255f
        } catch (e: Exception) {
            _statusFlow.value.currentBrightness
        }

        _statusFlow.value = _statusFlow.value.copy(
            timeFormatted = timeFormat.format(now),
            dateFormatted = dateFormat.format(now),
            currentVolume = currentVol,
            currentBrightness = brightness.coerceIn(0f, 1f),
            ringerMode = ringer,
            dndEnabled = ringer == AudioManager.RINGER_MODE_SILENT
        )
    }

    fun updateNotifications(notifications: List<com.example.model.AppNotification>) {
        _statusFlow.value = _statusFlow.value.copy(notifications = notifications)
    }

    fun updateFlashlightDirect(on: Boolean) {
        _statusFlow.value = _statusFlow.value.copy(isFlashlightOn = on)
    }

    fun updateVolumeDirect(volume: Float) {
        _statusFlow.value = _statusFlow.value.copy(currentVolume = volume.coerceIn(0f, 1f))
    }

    fun updateBrightnessDirect(brightness: Float) {
        _statusFlow.value = _statusFlow.value.copy(currentBrightness = brightness.coerceIn(0f, 1f))
    }

    fun updateDndDirect(dnd: Boolean) {
        _statusFlow.value = _statusFlow.value.copy(dndEnabled = dnd)
    }

    fun updateAutoRotateDirect(rotate: Boolean) {
        _statusFlow.value = _statusFlow.value.copy(autoRotateOn = rotate)
    }

    fun updateBluetoothDirect(bt: Boolean) {
        _statusFlow.value = _statusFlow.value.copy(bluetoothEnabled = bt)
    }

    fun updateWifiDirect(wifi: Boolean) {
        _statusFlow.value = _statusFlow.value.copy(wifiConnected = wifi)
    }
}

package com.example.system

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.example.service.PixelStatusBarAccessibilityService

class SystemActionController(
    private val context: Context,
    private val stateMonitor: SystemStateMonitor
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    fun performHaptic(strong: Boolean = false) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val effect = if (strong) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (strong) 40L else 15L)
            }
        } catch (e: Exception) {}
    }

    fun toggleFlashlight() {
        performHaptic()
        val current = stateMonitor.statusFlow.value.isFlashlightOn
        val target = !current
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cm.setTorchMode(id, target)
                        stateMonitor.updateFlashlightDirect(target)
                        return
                    }
                }
            }
        } catch (e: Exception) {
            stateMonitor.updateFlashlightDirect(target)
        }
    }

    fun setVolume(progress: Float) {
        try {
            audioManager?.let { am ->
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = (progress.coerceIn(0f, 1f) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                stateMonitor.updateVolumeDirect(progress)
            }
        } catch (e: Exception) {
            stateMonitor.updateVolumeDirect(progress)
        }
    }

    fun setBrightness(progress: Float) {
        val clamped = progress.coerceIn(0.05f, 1f)
        stateMonitor.updateBrightnessDirect(clamped)
        try {
            if (Settings.System.canWrite(context)) {
                val value = (clamped * 255).toInt()
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    value
                )
            }
        } catch (e: Exception) {}
    }

    fun toggleRingerMode() {
        performHaptic()
        try {
            audioManager?.let { am ->
                val nextMode = when (am.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                    AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                    else -> AudioManager.RINGER_MODE_NORMAL
                }
                am.ringerMode = nextMode
                stateMonitor.updateDndDirect(nextMode == AudioManager.RINGER_MODE_SILENT)
            }
        } catch (e: Exception) {}
    }

    fun toggleDnd() {
        performHaptic()
        val current = stateMonitor.statusFlow.value.dndEnabled
        val target = !current
        stateMonitor.updateDndDirect(target)
        try {
            audioManager?.let { am ->
                am.ringerMode = if (target) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
            }
        } catch (e: Exception) {}
    }

    fun toggleAutoRotate() {
        performHaptic()
        val current = stateMonitor.statusFlow.value.autoRotateOn
        val target = !current
        stateMonitor.updateAutoRotateDirect(target)
        try {
            if (Settings.System.canWrite(context)) {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION,
                    if (target) 1 else 0
                )
            }
        } catch (e: Exception) {}
    }

    fun toggleBluetooth() {
        performHaptic()
        val current = stateMonitor.statusFlow.value.bluetoothEnabled
        stateMonitor.updateBluetoothDirect(!current)
        openBluetoothSettings()
    }

    fun toggleWifi() {
        performHaptic()
        val current = stateMonitor.statusFlow.value.wifiConnected
        stateMonitor.updateWifiDirect(!current)
        openWifiSettings()
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openDisplaySettings() {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openSoundSettings() {
        val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    fun openWriteSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            android.net.Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try { context.startActivity(intent) } catch (e: Exception) {}
    }

    // Accessibility Global Actions
    fun lockScreen(): Boolean {
        performHaptic(strong = true)
        val service = PixelStatusBarAccessibilityService.instance
        return if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    fun takeScreenshot(): Boolean {
        performHaptic(strong = true)
        val service = PixelStatusBarAccessibilityService.instance
        return if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    fun openSystemNotifications(): Boolean {
        val service = PixelStatusBarAccessibilityService.instance
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
    }

    fun openSystemQuickSettings(): Boolean {
        val service = PixelStatusBarAccessibilityService.instance
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) ?: false
    }

    fun openPowerDialog(): Boolean {
        performHaptic(strong = true)
        val service = PixelStatusBarAccessibilityService.instance
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) ?: false
    }
}

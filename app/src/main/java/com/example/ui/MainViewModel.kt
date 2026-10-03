package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesRepository
import com.example.model.*
import com.example.service.PixelNotificationListener
import com.example.service.PixelStatusBarAccessibilityService
import com.example.service.PixelStatusBarOverlayService
import com.example.system.SystemActionController
import com.example.system.SystemStateMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionsState(
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasWriteSettingsPermission: Boolean = false,
    val isBatteryOptimizationsIgnored: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PreferencesRepository.getInstance(application)
    val stateMonitor = SystemStateMonitor(application).apply { start() }
    val actionController = SystemActionController(application, stateMonitor)

    val configFlow: StateFlow<StatusBarConfig> = repository.configFlow
    val systemStatusFlow: StateFlow<SystemStatus> = stateMonitor.statusFlow

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    private val _isControlCenterPreviewOpen = MutableStateFlow(false)
    val isControlCenterPreviewOpen: StateFlow<Boolean> = _isControlCenterPreviewOpen.asStateFlow()

    private val _currentTab = MutableStateFlow(0) // 0: Status Bar, 1: Control Center, 2: Gestures & Notch, 3: Permissions
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    init {
        checkPermissions()
        viewModelScope.launch {
            PixelNotificationListener.notificationsFlow.collect { notifs ->
                if (notifs.isNotEmpty()) {
                    stateMonitor.updateNotifications(notifs)
                }
            }
        }
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val hasAccessibility = isAccessibilityServiceEnabled()
        val hasNotification = isNotificationListenerEnabled()
        val hasWriteSettings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isBatteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }

        _permissionsState.value = PermissionsState(
            hasOverlayPermission = hasOverlay,
            hasAccessibilityPermission = hasAccessibility,
            hasNotificationPermission = hasNotification,
            hasWriteSettingsPermission = hasWriteSettings,
            isBatteryOptimizationsIgnored = isBatteryIgnored
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val context = getApplication<Application>()
        val expectedServiceName = "${context.packageName}/${PixelStatusBarAccessibilityService::class.java.canonicalName}"
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(context.packageName) || PixelStatusBarAccessibilityService.instance != null
        } catch (e: Exception) {
            return PixelStatusBarAccessibilityService.instance != null
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val context = getApplication<Application>()
        val packages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packages.contains(context.packageName) || PixelNotificationListener.instance != null
    }

    fun setOverrideEnabled(enabled: Boolean) {
        actionController.performHaptic()
        repository.setOverrideEnabled(enabled)
        val context = getApplication<Application>()
        if (enabled) {
            PixelStatusBarOverlayService.start(context)
        } else {
            PixelStatusBarOverlayService.stop(context)
        }
    }

    fun applyPreset(presetName: String) {
        actionController.performHaptic()
        when (presetName) {
            "PIXEL_16" -> {
                repository.updateConfig {
                    it.copy(
                        colorMode = ColorMode.ELEGANT_DARK,
                        batteryStyle = BatteryStyle.PIXEL_16_PILL,
                        batteryPercentMode = BatteryPercentMode.INSIDE_PILL,
                        clockPosition = ClockPosition.LEFT,
                        clockFormat = ClockFormat.FORMAT_12H,
                        cellularBadgeText = "5G",
                        showWifi = true,
                        showCellular = true,
                        showMediaPill = true,
                        showNotificationIcons = true,
                        showDateInChip = false,
                        cutoutType = CutoutType.CENTER_PUNCH
                    )
                }
            }
            "MATERIAL_YOU" -> {
                repository.updateConfig {
                    it.copy(
                        colorMode = ColorMode.DYNAMIC_MONET,
                        batteryStyle = BatteryStyle.CIRCLE_M3,
                        batteryPercentMode = BatteryPercentMode.NEXT_TO_PILL,
                        clockPosition = ClockPosition.LEFT,
                        showDateInChip = true,
                        showMediaPill = true,
                        showWifi = true,
                        showCellular = true
                    )
                }
            }
            "AMOLED_MIDNIGHT" -> {
                repository.updateConfig {
                    it.copy(
                        colorMode = ColorMode.AMOLED_BLACK,
                        batteryStyle = BatteryStyle.PIXEL_16_PILL,
                        batteryPercentMode = BatteryPercentMode.INSIDE_PILL,
                        showSeconds = true,
                        showNetworkSpeed = true
                    )
                }
            }
            "MINIMALIST" -> {
                repository.updateConfig {
                    it.copy(
                        colorMode = ColorMode.ELEGANT_DARK,
                        batteryStyle = BatteryStyle.HORIZONTAL_BAR,
                        batteryPercentMode = BatteryPercentMode.HIDDEN,
                        showWifi = true,
                        showCellular = false,
                        showBluetooth = false,
                        showDnd = false,
                        showAlarm = false,
                        showMediaPill = false,
                        showNotificationIcons = false
                    )
                }
            }
        }
    }

    fun updateConfig(update: (StatusBarConfig) -> StatusBarConfig) {
        repository.updateConfig(update)
    }

    fun openControlCenterPreview() {
        actionController.performHaptic()
        _isControlCenterPreviewOpen.value = true
    }

    fun closeControlCenterPreview() {
        actionController.performHaptic()
        _isControlCenterPreviewOpen.value = false
    }

    fun requestIgnoreBatteryOptimizations() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stateMonitor.stop()
    }
}

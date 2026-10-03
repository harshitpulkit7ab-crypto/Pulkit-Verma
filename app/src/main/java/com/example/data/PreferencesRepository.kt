package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pixel_statusbar_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<StatusBarConfig> = _configFlow.asStateFlow()

    fun getConfig(): StatusBarConfig = _configFlow.value

    fun updateConfig(update: (StatusBarConfig) -> StatusBarConfig) {
        val newConfig = update(_configFlow.value)
        saveConfig(newConfig)
        _configFlow.value = newConfig
    }

    fun setOverrideEnabled(enabled: Boolean) {
        updateConfig { it.copy(isOverrideEnabled = enabled) }
    }

    private fun loadConfig(): StatusBarConfig {
        return StatusBarConfig(
            isOverrideEnabled = prefs.getBoolean("isOverrideEnabled", false),
            clockPosition = try {
                ClockPosition.valueOf(prefs.getString("clockPosition", ClockPosition.LEFT.name) ?: ClockPosition.LEFT.name)
            } catch (e: Exception) { ClockPosition.LEFT },
            clockFormat = try {
                ClockFormat.valueOf(prefs.getString("clockFormat", ClockFormat.FORMAT_12H.name) ?: ClockFormat.FORMAT_12H.name)
            } catch (e: Exception) { ClockFormat.FORMAT_12H },
            showSeconds = prefs.getBoolean("showSeconds", false),
            showDateInChip = prefs.getBoolean("showDateInChip", false),
            showAmPm = prefs.getBoolean("showAmPm", true),
            batteryStyle = try {
                BatteryStyle.valueOf(prefs.getString("batteryStyle", BatteryStyle.PIXEL_16_PILL.name) ?: BatteryStyle.PIXEL_16_PILL.name)
            } catch (e: Exception) { BatteryStyle.PIXEL_16_PILL },
            batteryPercentMode = try {
                BatteryPercentMode.valueOf(prefs.getString("batteryPercentMode", BatteryPercentMode.INSIDE_PILL.name) ?: BatteryPercentMode.INSIDE_PILL.name)
            } catch (e: Exception) { BatteryPercentMode.INSIDE_PILL },
            showWifi = prefs.getBoolean("showWifi", true),
            showCellular = prefs.getBoolean("showCellular", true),
            cellularBadgeText = prefs.getString("cellularBadgeText", "5G") ?: "5G",
            showNetworkSpeed = prefs.getBoolean("showNetworkSpeed", false),
            showBluetooth = prefs.getBoolean("showBluetooth", true),
            showDnd = prefs.getBoolean("showDnd", true),
            showAlarm = prefs.getBoolean("showAlarm", true),
            showVibrate = prefs.getBoolean("showVibrate", true),
            showPrivacyDot = prefs.getBoolean("showPrivacyDot", false),
            showMediaPill = prefs.getBoolean("showMediaPill", true),
            showNotificationIcons = prefs.getBoolean("showNotificationIcons", true),
            statusBarHeightDp = prefs.getInt("statusBarHeightDp", 32),
            cutoutType = try {
                CutoutType.valueOf(prefs.getString("cutoutType", CutoutType.CENTER_PUNCH.name) ?: CutoutType.CENTER_PUNCH.name)
            } catch (e: Exception) { CutoutType.CENTER_PUNCH },
            cutoutWidthDp = prefs.getInt("cutoutWidthDp", 24),
            paddingStartDp = prefs.getInt("paddingStartDp", 16),
            paddingEndDp = prefs.getInt("paddingEndDp", 16),
            colorMode = try {
                ColorMode.valueOf(prefs.getString("colorMode", ColorMode.ELEGANT_DARK.name) ?: ColorMode.ELEGANT_DARK.name)
            } catch (e: Exception) { ColorMode.ELEGANT_DARK },
            customBackgroundColorHex = prefs.getString("customBackgroundColorHex", "#1A1C1E") ?: "#1A1C1E",
            customIconColorHex = prefs.getString("customIconColorHex", "#E2E2E6") ?: "#E2E2E6",
            gestureSwipeDown = try {
                GestureAction.valueOf(prefs.getString("gestureSwipeDown", GestureAction.OPEN_PIXEL_CONTROL_CENTER.name) ?: GestureAction.OPEN_PIXEL_CONTROL_CENTER.name)
            } catch (e: Exception) { GestureAction.OPEN_PIXEL_CONTROL_CENTER },
            gestureDoubleTap = try {
                GestureAction.valueOf(prefs.getString("gestureDoubleTap", GestureAction.LOCK_SCREEN.name) ?: GestureAction.LOCK_SCREEN.name)
            } catch (e: Exception) { GestureAction.LOCK_SCREEN },
            gestureLongPress = try {
                GestureAction.valueOf(prefs.getString("gestureLongPress", GestureAction.TAKE_SCREENSHOT.name) ?: GestureAction.TAKE_SCREENSHOT.name)
            } catch (e: Exception) { GestureAction.TAKE_SCREENSHOT },
            brightnessSwipeEnabled = prefs.getBoolean("brightnessSwipeEnabled", true),
            hapticFeedbackEnabled = prefs.getBoolean("hapticFeedbackEnabled", true),
            controlCenterBlur = prefs.getBoolean("controlCenterBlur", true),
            controlCenterDualSlider = prefs.getBoolean("controlCenterDualSlider", true)
        )
    }

    private fun saveConfig(config: StatusBarConfig) {
        prefs.edit().apply {
            putBoolean("isOverrideEnabled", config.isOverrideEnabled)
            putString("clockPosition", config.clockPosition.name)
            putString("clockFormat", config.clockFormat.name)
            putBoolean("showSeconds", config.showSeconds)
            putBoolean("showDateInChip", config.showDateInChip)
            putBoolean("showAmPm", config.showAmPm)
            putString("batteryStyle", config.batteryStyle.name)
            putString("batteryPercentMode", config.batteryPercentMode.name)
            putBoolean("showWifi", config.showWifi)
            putBoolean("showCellular", config.showCellular)
            putString("cellularBadgeText", config.cellularBadgeText)
            putBoolean("showNetworkSpeed", config.showNetworkSpeed)
            putBoolean("showBluetooth", config.showBluetooth)
            putBoolean("showDnd", config.showDnd)
            putBoolean("showAlarm", config.showAlarm)
            putBoolean("showVibrate", config.showVibrate)
            putBoolean("showPrivacyDot", config.showPrivacyDot)
            putBoolean("showMediaPill", config.showMediaPill)
            putBoolean("showNotificationIcons", config.showNotificationIcons)
            putInt("statusBarHeightDp", config.statusBarHeightDp)
            putString("cutoutType", config.cutoutType.name)
            putInt("cutoutWidthDp", config.cutoutWidthDp)
            putInt("paddingStartDp", config.paddingStartDp)
            putInt("paddingEndDp", config.paddingEndDp)
            putString("colorMode", config.colorMode.name)
            putString("customBackgroundColorHex", config.customBackgroundColorHex)
            putString("customIconColorHex", config.customIconColorHex)
            putString("gestureSwipeDown", config.gestureSwipeDown.name)
            putString("gestureDoubleTap", config.gestureDoubleTap.name)
            putString("gestureLongPress", config.gestureLongPress.name)
            putBoolean("brightnessSwipeEnabled", config.brightnessSwipeEnabled)
            putBoolean("hapticFeedbackEnabled", config.hapticFeedbackEnabled)
            putBoolean("controlCenterBlur", config.controlCenterBlur)
            putBoolean("controlCenterDualSlider", config.controlCenterDualSlider)
            apply()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

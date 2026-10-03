package com.example.model

enum class ClockPosition {
    LEFT,
    CENTER,
    RIGHT
}

enum class ClockFormat {
    FORMAT_12H,
    FORMAT_24H
}

enum class BatteryStyle {
    PIXEL_16_PILL,
    CIRCLE_M3,
    HORIZONTAL_BAR,
    HIDDEN
}

enum class BatteryPercentMode {
    INSIDE_PILL,
    NEXT_TO_PILL,
    HIDDEN
}

enum class CutoutType {
    NONE,
    CENTER_PUNCH,
    LEFT_PUNCH,
    TEARDROP
}

enum class ColorMode {
    ELEGANT_DARK,
    AMOLED_BLACK,
    DYNAMIC_MONET,
    TRANSLUCENT,
    CUSTOM
}

enum class GestureAction {
    OPEN_PIXEL_CONTROL_CENTER,
    OPEN_SYSTEM_NOTIFICATIONS,
    OPEN_SYSTEM_QUICK_SETTINGS,
    LOCK_SCREEN,
    TAKE_SCREENSHOT,
    NONE
}

data class StatusBarConfig(
    val isOverrideEnabled: Boolean = false,
    val clockPosition: ClockPosition = ClockPosition.LEFT,
    val clockFormat: ClockFormat = ClockFormat.FORMAT_12H,
    val showSeconds: Boolean = false,
    val showDateInChip: Boolean = false,
    val showAmPm: Boolean = true,
    val batteryStyle: BatteryStyle = BatteryStyle.PIXEL_16_PILL,
    val batteryPercentMode: BatteryPercentMode = BatteryPercentMode.INSIDE_PILL,
    val showWifi: Boolean = true,
    val showCellular: Boolean = true,
    val cellularBadgeText: String = "5G",
    val showNetworkSpeed: Boolean = false,
    val showBluetooth: Boolean = true,
    val showDnd: Boolean = true,
    val showAlarm: Boolean = true,
    val showVibrate: Boolean = true,
    val showPrivacyDot: Boolean = false,
    val showMediaPill: Boolean = true,
    val showNotificationIcons: Boolean = true,
    val statusBarHeightDp: Int = 32,
    val cutoutType: CutoutType = CutoutType.CENTER_PUNCH,
    val cutoutWidthDp: Int = 24,
    val paddingStartDp: Int = 16,
    val paddingEndDp: Int = 16,
    val colorMode: ColorMode = ColorMode.ELEGANT_DARK,
    val customBackgroundColorHex: String = "#1A1C1E",
    val customIconColorHex: String = "#E2E2E6",
    val gestureSwipeDown: GestureAction = GestureAction.OPEN_PIXEL_CONTROL_CENTER,
    val gestureDoubleTap: GestureAction = GestureAction.LOCK_SCREEN,
    val gestureLongPress: GestureAction = GestureAction.TAKE_SCREENSHOT,
    val brightnessSwipeEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val controlCenterBlur: Boolean = true,
    val controlCenterDualSlider: Boolean = true
)

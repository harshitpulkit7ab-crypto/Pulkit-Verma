package com.example.model

enum class TileType {
    INTERNET,
    BLUETOOTH,
    DO_NOT_DISTURB,
    FLASHLIGHT,
    AUTO_ROTATE,
    HOTSPOT,
    BATTERY_SAVER,
    DARK_THEME,
    AIRPLANE_MODE,
    NIGHT_LIGHT,
    SCREEN_RECORD,
    CALCULATOR,
    ALARM,
    MUTE_VOLUME,
    LOCK_SCREEN,
    SCREENSHOT
}

data class QuickTileItem(
    val id: TileType,
    val title: String,
    val subtitle: String,
    val isActive: Boolean,
    val isAvailable: Boolean = true
)

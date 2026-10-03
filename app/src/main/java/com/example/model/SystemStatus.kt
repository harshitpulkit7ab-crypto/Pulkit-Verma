package com.example.model

data class AppNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val postTime: Long,
    val iconRes: Int? = null
)

data class SystemStatus(
    val timeFormatted: String = "9:41",
    val dateFormatted: String = "Sat, Aug 15",
    val batteryLevel: Int = 84,
    val isCharging: Boolean = false,
    val wifiConnected: Boolean = true,
    val wifiSsid: String = "Pixel_Fiber_6G",
    val wifiSignalLevel: Int = 4, // 0 to 4
    val cellularConnected: Boolean = true,
    val cellularType: String = "5G",
    val cellularSignalLevel: Int = 4, // 0 to 4
    val bluetoothEnabled: Boolean = true,
    val dndEnabled: Boolean = false,
    val ringerMode: Int = 2, // 2 = Normal, 1 = Vibrate, 0 = Silent
    val isFlashlightOn: Boolean = false,
    val autoRotateOn: Boolean = true,
    val airplaneModeOn: Boolean = false,
    val currentBrightness: Float = 0.65f, // 0.0 to 1.0
    val currentVolume: Float = 0.70f, // 0.0 to 1.0
    val networkSpeedFormatted: String = "2.4 MB/s",
    val activeMediaTitle: String? = "Starboy - The Weeknd",
    val activeMediaArtist: String? = "Spotify",
    val isMediaPlaying: Boolean = true,
    val notifications: List<AppNotification> = listOf(
        AppNotification(
            id = "1",
            packageName = "com.google.android.gm",
            appName = "Gmail",
            title = "Google Security Alert",
            content = "New sign-in detected on Pixel 9 Pro",
            postTime = System.currentTimeMillis() - 1000 * 60 * 5
        ),
        AppNotification(
            id = "2",
            packageName = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "Alex Carter",
            content = "Hey! Let's check out the Android 16 beta update!",
            postTime = System.currentTimeMillis() - 1000 * 60 * 18
        ),
        AppNotification(
            id = "3",
            packageName = "com.spotify.music",
            appName = "Spotify",
            title = "Now Playing",
            content = "Starboy (feat. Daft Punk)",
            postTime = System.currentTimeMillis() - 1000 * 60 * 30
        )
    )
)

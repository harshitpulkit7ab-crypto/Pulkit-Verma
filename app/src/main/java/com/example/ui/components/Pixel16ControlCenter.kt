package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.system.SystemActionController
import com.example.ui.theme.*

@Composable
fun Pixel16ControlCenter(
    config: StatusBarConfig,
    status: SystemStatus,
    actionController: SystemActionController,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    var mediaPlaying by remember { mutableStateOf(status.isMediaPlaying) }
    var notificationsList by remember(status.notifications) { mutableStateOf(status.notifications) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF01A1C1E),
                        Color(0xF81A1C1E),
                        Color(0xFF141517)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -20) {
                        onDismiss()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // HEADER BAR: Big Clock, Date, and Utility Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = status.timeFormatted,
                        color = PixelTextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = status.dateFormatted,
                        color = PixelPrimaryLilac,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Gear Button
                    IconButton(
                        onClick = {
                            actionController.performHaptic()
                            actionController.openDisplaySettings()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PixelDarkSurfaceContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = PixelTextPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Power Menu Pill Button
                    Button(
                        onClick = {
                            actionController.openPowerDialog()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelDarkSurfaceContainer,
                            contentColor = PixelTextPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Power",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // DUAL EXPRESSIVE SLIDERS: BRIGHTNESS & VOLUME
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Brightness Slider Pill
                ExpressiveSliderTile(
                    modifier = Modifier.weight(1f),
                    value = status.currentBrightness,
                    icon = Icons.Rounded.BrightnessHigh,
                    label = "Brightness",
                    onValueChange = { actionController.setBrightness(it) }
                )

                // Volume Slider Pill
                ExpressiveSliderTile(
                    modifier = Modifier.weight(1f),
                    value = status.currentVolume,
                    icon = when {
                        status.currentVolume == 0f -> Icons.Rounded.VolumeMute
                        status.currentVolume < 0.5f -> Icons.Rounded.VolumeDown
                        else -> Icons.Rounded.VolumeUp
                    },
                    label = "Media Vol",
                    onValueChange = { actionController.setVolume(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SCROLLABLE AREA: QUICK TILES, MEDIA CARD & NOTIFICATIONS
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 2-COLUMN EXPRESSIVE QUICK TILES (Android 16 style)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Row 1: Internet & Bluetooth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Internet",
                                subtitle = if (status.wifiConnected) status.wifiSsid else if (status.cellularConnected) "5G Cellular" else "Offline",
                                icon = if (status.wifiConnected) Icons.Rounded.Wifi else Icons.Rounded.SignalCellular4Bar,
                                isActive = status.wifiConnected || status.cellularConnected,
                                onClick = { actionController.toggleWifi() }
                            )

                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Bluetooth",
                                subtitle = if (status.bluetoothEnabled) "On" else "Off",
                                icon = Icons.Rounded.Bluetooth,
                                isActive = status.bluetoothEnabled,
                                onClick = { actionController.toggleBluetooth() }
                            )
                        }

                        // Row 2: Flashlight & Do Not Disturb
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Flashlight",
                                subtitle = if (status.isFlashlightOn) "On" else "Off",
                                icon = if (status.isFlashlightOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff,
                                isActive = status.isFlashlightOn,
                                onClick = { actionController.toggleFlashlight() }
                            )

                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Do Not Disturb",
                                subtitle = if (status.dndEnabled) "Priority only" else "Off",
                                icon = Icons.Rounded.DoNotDisturbOn,
                                isActive = status.dndEnabled,
                                onClick = { actionController.toggleDnd() }
                            )
                        }

                        // Row 3: Auto-rotate & Lock Screen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Auto-rotate",
                                subtitle = if (status.autoRotateOn) "Auto" else "Locked",
                                icon = Icons.Rounded.ScreenRotation,
                                isActive = status.autoRotateOn,
                                onClick = { actionController.toggleAutoRotate() }
                            )

                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Lock Screen",
                                subtitle = "Instant Lock",
                                icon = Icons.Rounded.Lock,
                                isActive = false,
                                onClick = { actionController.lockScreen() }
                            )
                        }

                        // Row 4: Screenshot & Sound Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Screenshot",
                                subtitle = "Capture display",
                                icon = Icons.Rounded.Screenshot,
                                isActive = false,
                                onClick = { actionController.takeScreenshot() }
                            )

                            Pixel16QuickTile(
                                modifier = Modifier.weight(1f),
                                title = "Sound Mode",
                                subtitle = when (status.ringerMode) {
                                    1 -> "Vibrate"
                                    0 -> "Silent"
                                    else -> "Normal"
                                },
                                icon = when (status.ringerMode) {
                                    1 -> Icons.Rounded.Vibration
                                    0 -> Icons.Rounded.VolumeOff
                                    else -> Icons.Rounded.VolumeUp
                                },
                                isActive = status.ringerMode == 2,
                                onClick = { actionController.toggleRingerMode() }
                            )
                        }
                    }
                }

                // PIXEL 16 MEDIA PLAYER CARD
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1DB954)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = "Media",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = status.activeMediaArtist ?: "Spotify",
                                        color = PixelTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PixelPrimaryLilac.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Pixel Phone Speaker",
                                        color = PixelPrimaryLilac,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = status.activeMediaTitle ?: "Starboy (feat. Daft Punk)",
                                color = PixelTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Wavy progress bar
                            LinearProgressIndicator(
                                progress = { 0.42f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = PixelPrimaryLilac,
                                trackColor = PixelDarkOutline
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Playback controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { actionController.performHaptic() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = PixelTextPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        actionController.performHaptic()
                                        mediaPlaying = !mediaPlaying
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(PixelPrimaryLilac)
                                ) {
                                    Icon(
                                        imageVector = if (mediaPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = PixelOnPrimaryLilac,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                IconButton(onClick = { actionController.performHaptic() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Next",
                                        tint = PixelTextPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // NOTIFICATION SHADE HEADER & LIST
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications (${notificationsList.size})",
                            color = PixelPrimaryLilac,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        if (notificationsList.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    actionController.performHaptic()
                                    notificationsList = emptyList()
                                }
                            ) {
                                Text(
                                    text = "Clear all",
                                    color = PixelTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (notificationsList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No new notifications",
                                color = PixelTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(notificationsList, key = { it.id }) { notif ->
                        NotificationCard(
                            notification = notif,
                            onDismiss = {
                                actionController.performHaptic()
                                notificationsList = notificationsList.filter { it.id != notif.id }
                            }
                        )
                    }
                }
            }

            // BOTTOM DISMISS HANDLE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PixelTextSecondary.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun Pixel16QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) PixelPrimaryLilac else PixelDarkSurfaceContainer
    val contentColor = if (isActive) PixelOnPrimaryLilac else PixelTextPrimary
    val subColor = if (isActive) PixelOnPrimaryLilac.copy(alpha = 0.8f) else PixelTextSecondary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = if (!isActive) androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline) else null,
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) PixelOnPrimaryLilac.copy(alpha = 0.15f)
                        else PixelDarkOutline.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = subColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ExpressiveSliderTile(
    value: Float,
    icon: ImageVector,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(PixelDarkSurfaceContainer)
            .border(1.dp, PixelDarkOutline, RoundedCornerShape(26.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    val newProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(newProgress)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Active fill bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = value.coerceIn(0.08f, 1f))
                .clip(RoundedCornerShape(26.dp))
                .background(PixelPrimaryLilac)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (value > 0.25f) PixelOnPrimaryLilac else PixelTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    color = if (value > 0.45f) PixelOnPrimaryLilac else PixelTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "${(value * 100).toInt()}%",
                color = if (value > 0.85f) PixelOnPrimaryLilac else PixelTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PixelPrimaryLilac.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = notification.appName,
                    tint = PixelPrimaryLilac,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.appName,
                        color = PixelTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = PixelTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = notification.title,
                    color = PixelTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (notification.content.isNotEmpty()) {
                    Text(
                        text = notification.content,
                        color = PixelTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

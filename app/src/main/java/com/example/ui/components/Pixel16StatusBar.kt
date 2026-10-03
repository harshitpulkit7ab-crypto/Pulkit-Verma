package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun Pixel16StatusBar(
    config: StatusBarConfig,
    status: SystemStatus,
    modifier: Modifier = Modifier,
    onBarClick: () -> Unit = {},
    onBarLongClick: () -> Unit = {}
) {
    val bgColor = when (config.colorMode) {
        ColorMode.ELEGANT_DARK -> PixelDarkBackground
        ColorMode.AMOLED_BLACK -> Color.Black
        ColorMode.DYNAMIC_MONET -> Color(0xFF1F222A)
        ColorMode.TRANSLUCENT -> Color(0xCC1A1C1E)
        ColorMode.CUSTOM -> try {
            Color(android.graphics.Color.parseColor(config.customBackgroundColorHex))
        } catch (e: Exception) {
            PixelDarkBackground
        }
    }

    val iconColor = when (config.colorMode) {
        ColorMode.ELEGANT_DARK -> PixelTextPrimary
        ColorMode.AMOLED_BLACK -> Color.White
        ColorMode.DYNAMIC_MONET -> PixelPrimaryLilac
        ColorMode.TRANSLUCENT -> PixelTextPrimary
        ColorMode.CUSTOM -> try {
            Color(android.graphics.Color.parseColor(config.customIconColorHex))
        } catch (e: Exception) {
            PixelTextPrimary
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(config.statusBarHeightDp.dp)
            .background(bgColor)
            .padding(start = config.paddingStartDp.dp, end = config.paddingEndDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (config.clockPosition == ClockPosition.LEFT) {
                    ClockChip(config = config, status = status, textColor = iconColor)
                }

                // Notification icons ticker
                if (config.showNotificationIcons && status.notifications.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        status.notifications.take(3).forEach { notif ->
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when {
                                    notif.packageName.contains("gm") || notif.packageName.contains("mail") -> Icons.Rounded.Mail
                                    notif.packageName.contains("msg") || notif.packageName.contains("chat") -> Icons.Rounded.ChatBubble
                                    notif.packageName.contains("spotify") || notif.packageName.contains("music") -> Icons.Rounded.MusicNote
                                    else -> Icons.Rounded.Notifications
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = notif.appName,
                                    tint = iconColor,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }

                // Media Pill Chip (if active)
                if (config.showMediaPill && status.isMediaPlaying) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PixelPrimaryLilac.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Playing",
                            tint = PixelPrimaryLilac,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Music",
                            color = PixelPrimaryLilac,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // CENTER SECTION (Cutout Spacer or Center Clock)
            if (config.cutoutType == CutoutType.CENTER_PUNCH) {
                Spacer(modifier = Modifier.width(config.cutoutWidthDp.dp))
            } else if (config.clockPosition == ClockPosition.CENTER) {
                ClockChip(config = config, status = status, textColor = iconColor)
            }

            // RIGHT SECTION (Status Glyphs & Battery)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Network speed badge
                if (config.showNetworkSpeed) {
                    Text(
                        text = status.networkSpeedFormatted,
                        color = iconColor.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Privacy Indicator Dot (Camera / Mic active green indicator)
                if (config.showPrivacyDot) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }

                // Status Icons
                if (config.showDnd && status.dndEnabled) {
                    Icon(
                        imageVector = Icons.Rounded.DoNotDisturbOn,
                        contentDescription = "DND",
                        tint = iconColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (config.showVibrate && status.ringerMode == 1) {
                    Icon(
                        imageVector = Icons.Rounded.Vibration,
                        contentDescription = "Vibrate",
                        tint = iconColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (config.showAlarm) {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = "Alarm",
                        tint = iconColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (config.showBluetooth && status.bluetoothEnabled) {
                    Icon(
                        imageVector = Icons.Rounded.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = iconColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Wi-Fi 7 Expressive Glyph
                if (config.showWifi && status.wifiConnected) {
                    WifiGlyph(level = status.wifiSignalLevel, tint = iconColor)
                }

                // 5G SA Cellular Glyph
                if (config.showCellular && status.cellularConnected) {
                    CellularGlyph(
                        badge = config.cellularBadgeText,
                        level = status.cellularSignalLevel,
                        tint = iconColor
                    )
                }

                // Pixel 16 Dual-Pill Battery
                if (config.batteryStyle != BatteryStyle.HIDDEN) {
                    Pixel16BatteryIndicator(
                        config = config,
                        level = status.batteryLevel,
                        isCharging = status.isCharging,
                        tint = iconColor
                    )
                }

                if (config.clockPosition == ClockPosition.RIGHT) {
                    ClockChip(config = config, status = status, textColor = iconColor)
                }
            }
        }
    }
}

@Composable
fun ClockChip(
    config: StatusBarConfig,
    status: SystemStatus,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (config.showDateInChip) {
            Text(
                text = status.dateFormatted,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "•",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
        Text(
            text = status.timeFormatted,
            color = textColor,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        )
    }
}

@Composable
fun WifiGlyph(level: Int, tint: Color) {
    Icon(
        imageVector = when (level) {
            0, 1 -> Icons.Rounded.Wifi1Bar
            2 -> Icons.Rounded.Wifi2Bar
            else -> Icons.Rounded.Wifi
        },
        contentDescription = "Wi-Fi",
        tint = tint,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun CellularGlyph(badge: String, level: Int, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        if (badge.isNotEmpty()) {
            Text(
                text = badge,
                color = tint,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
        // Signal bars indicator
        Row(
            modifier = Modifier.height(11.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            for (i in 1..4) {
                val isActive = i <= (level.coerceIn(1, 4))
                val barHeight = (3 + i * 2).dp
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isActive) tint else tint.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun Pixel16BatteryIndicator(
    config: StatusBarConfig,
    level: Int,
    isCharging: Boolean,
    tint: Color
) {
    val batteryColor = when {
        isCharging -> PixelBatteryCharging
        level <= 15 -> Color(0xFFEF5350)
        level <= 30 -> Color(0xFFFFB74D)
        else -> tint
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (config.batteryPercentMode == BatteryPercentMode.NEXT_TO_PILL) {
            Text(
                text = "$level%",
                color = tint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Pill shape container
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(1.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Fill level
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (level / 100f).coerceIn(0.05f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(batteryColor)
            )

            if (isCharging) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Charging",
                        tint = Color.Black,
                        modifier = Modifier.size(9.dp)
                    )
                }
            } else if (config.batteryPercentMode == BatteryPercentMode.INSIDE_PILL) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$level",
                        color = if (level > 40) Color.Black else tint,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

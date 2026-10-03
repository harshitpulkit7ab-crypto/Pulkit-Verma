package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.Pixel16ControlCenter
import com.example.ui.components.Pixel16StatusBar
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PixelStatusBarApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}

@Composable
fun PixelStatusBarApp(viewModel: MainViewModel) {
    val config by viewModel.configFlow.collectAsStateWithLifecycle()
    val status by viewModel.systemStatusFlow.collectAsStateWithLifecycle()
    val permissions by viewModel.permissionsState.collectAsStateWithLifecycle()
    val isControlCenterOpen by viewModel.isControlCenterPreviewOpen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP LIVE STATUS BAR PREVIEW CONTAINER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141517))
                    .padding(bottom = 6.dp)
            ) {
                // Hint label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE STATUS BAR PREVIEW",
                        color = PixelPrimaryLilac,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tap to open Control Center",
                        color = PixelTextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Status Bar Component
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.openControlCenterPreview() }
                        .border(1.dp, PixelDarkOutline, RoundedCornerShape(8.dp))
                ) {
                    Pixel16StatusBar(
                        config = config,
                        status = status
                    )
                }
            }

            // APP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status Pixel",
                        color = PixelTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Pixel 16 Status Bar & Control Center",
                        color = PixelTextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.openControlCenterPreview() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PixelDarkSurfaceContainer)
                        .testTag("preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Test Control Center",
                        tint = PixelPrimaryLilac,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // NAVIGATION PILL TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabPill(
                    text = "Status Bar",
                    icon = Icons.Rounded.Smartphone,
                    isSelected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    text = "Control Center",
                    icon = Icons.Rounded.Dashboard,
                    isSelected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    text = "Gestures",
                    icon = Icons.Rounded.TouchApp,
                    isSelected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    text = "Setup",
                    icon = Icons.Rounded.Security,
                    isSelected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    modifier = Modifier.weight(1f)
                )
            }

            // MAIN SCROLLABLE CONTENT
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
            ) {
                // OVERRIDE MASTER SWITCH CARD
                item {
                    MasterOverrideCard(
                        isEnabled = config.isOverrideEnabled,
                        hasAccessibility = permissions.hasAccessibilityPermission,
                        onToggle = { viewModel.setOverrideEnabled(it) },
                        onSetupClick = { viewModel.setTab(3) }
                    )
                }

                // STYLE PRESETS
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "STYLE PRESETS",
                                color = PixelPrimaryLilac,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetButton(
                                    title = "Pixel 16 Beta",
                                    isActive = config.colorMode == ColorMode.ELEGANT_DARK && config.batteryStyle == BatteryStyle.PIXEL_16_PILL,
                                    onClick = { viewModel.applyPreset("PIXEL_16") },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetButton(
                                    title = "Material You",
                                    isActive = config.colorMode == ColorMode.DYNAMIC_MONET,
                                    onClick = { viewModel.applyPreset("MATERIAL_YOU") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetButton(
                                    title = "AMOLED Black",
                                    isActive = config.colorMode == ColorMode.AMOLED_BLACK,
                                    onClick = { viewModel.applyPreset("AMOLED_MIDNIGHT") },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetButton(
                                    title = "Minimalist",
                                    isActive = config.batteryStyle == BatteryStyle.HORIZONTAL_BAR,
                                    onClick = { viewModel.applyPreset("MINIMALIST") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // TAB SPECIFIC CONTENT
                when (currentTab) {
                    0 -> {
                        // STATUS BAR CUSTOMIZATION
                        item {
                            StatusBarCustomizationSection(
                                config = config,
                                onUpdate = { viewModel.updateConfig(it) }
                            )
                        }
                    }
                    1 -> {
                        // CONTROL CENTER CUSTOMIZATION
                        item {
                            ControlCenterCustomizationSection(
                                config = config,
                                status = status,
                                onOpenPreview = { viewModel.openControlCenterPreview() },
                                onUpdate = { viewModel.updateConfig(it) }
                            )
                        }
                    }
                    2 -> {
                        // GESTURES & NOTCH CUTOUT
                        item {
                            GesturesAndNotchSection(
                                config = config,
                                onUpdate = { viewModel.updateConfig(it) }
                            )
                        }
                    }
                    3 -> {
                        // SETUP & PERMISSIONS
                        item {
                            PermissionsSetupSection(
                                permissions = permissions,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // BOTTOM ACTION BAR
            Surface(
                color = PixelDarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.actionController.performHaptic()
                            viewModel.setOverrideEnabled(true)
                            viewModel.openControlCenterPreview()
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelPrimaryLilac,
                            contentColor = PixelOnPrimaryLilac
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("apply_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply Pixel 16 System UI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // FULLSCREEN CONTROL CENTER PREVIEW DIALOG / SIMULATOR
        if (isControlCenterOpen) {
            Dialog(
                onDismissRequest = { viewModel.closeControlCenterPreview() },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Pixel16ControlCenter(
                    config = config,
                    status = status,
                    actionController = viewModel.actionController,
                    onDismiss = { viewModel.closeControlCenterPreview() }
                )
            }
        }
    }
}

@Composable
fun TabPill(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PixelPrimaryLilac else PixelDarkSurfaceContainer,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline) else null,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) PixelOnPrimaryLilac else PixelTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = if (isSelected) PixelOnPrimaryLilac else PixelTextPrimary,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun MasterOverrideCard(
    isEnabled: Boolean,
    hasAccessibility: Boolean,
    onToggle: (Boolean) -> Unit,
    onSetupClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) PixelPrimaryLilac else PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Override System UI",
                            color = PixelTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isEnabled) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PixelPrimaryLilac.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = PixelPrimaryLilac,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Disable native status bar & apply Pixel 16 layout",
                        color = PixelTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PixelOnPrimaryLilac,
                        checkedTrackColor = PixelPrimaryLilac,
                        uncheckedThumbColor = PixelTextSecondary,
                        uncheckedTrackColor = PixelDarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("override_switch")
                )
            }

            if (!hasAccessibility && isEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33FFB74D),
                    onClick = onSetupClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Enable Accessibility Service in Setup tab for full overlay & gestures.",
                            color = Color(0xFFFFE0B2),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) PixelPrimaryLilac else PixelDarkSurfaceVariant,
            contentColor = if (isActive) PixelOnPrimaryLilac else PixelTextPrimary
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusBarCustomizationSection(
    config: StatusBarConfig,
    onUpdate: ((StatusBarConfig) -> StatusBarConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "STATUS BAR ELEMENTS",
                color = PixelPrimaryLilac,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Clock Alignment
            Column {
                Text(
                    text = "Clock Alignment",
                    color = PixelTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        label = "Left",
                        isSelected = config.clockPosition == ClockPosition.LEFT,
                        onClick = { onUpdate { it.copy(clockPosition = ClockPosition.LEFT) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "Center",
                        isSelected = config.clockPosition == ClockPosition.CENTER,
                        onClick = { onUpdate { it.copy(clockPosition = ClockPosition.CENTER) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "Right",
                        isSelected = config.clockPosition == ClockPosition.RIGHT,
                        onClick = { onUpdate { it.copy(clockPosition = ClockPosition.RIGHT) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Battery Style
            Column {
                Text(
                    text = "Battery Indicator Style",
                    color = PixelTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        label = "Pixel 16 Pill",
                        isSelected = config.batteryStyle == BatteryStyle.PIXEL_16_PILL,
                        onClick = { onUpdate { it.copy(batteryStyle = BatteryStyle.PIXEL_16_PILL) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "Inside %",
                        isSelected = config.batteryPercentMode == BatteryPercentMode.INSIDE_PILL,
                        onClick = { onUpdate { it.copy(batteryPercentMode = BatteryPercentMode.INSIDE_PILL) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "Next to %",
                        isSelected = config.batteryPercentMode == BatteryPercentMode.NEXT_TO_PILL,
                        onClick = { onUpdate { it.copy(batteryPercentMode = BatteryPercentMode.NEXT_TO_PILL) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Toggle Switches
            SettingToggleRow(
                title = "Wi-Fi 7 Expressive Glyph",
                subtitle = "Modern curved signal icon",
                isChecked = config.showWifi,
                onCheckedChange = { chk -> onUpdate { it.copy(showWifi = chk) } }
            )

            SettingToggleRow(
                title = "Cellular 5G SA Badge",
                subtitle = "Displays 5G badge beside signal bars",
                isChecked = config.showCellular,
                onCheckedChange = { chk -> onUpdate { it.copy(showCellular = chk) } }
            )

            SettingToggleRow(
                title = "Live Notification Icons",
                subtitle = "App icons ticker in status bar",
                isChecked = config.showNotificationIcons,
                onCheckedChange = { chk -> onUpdate { it.copy(showNotificationIcons = chk) } }
            )

            SettingToggleRow(
                title = "Now Playing Media Pill",
                subtitle = "Dynamic music pill chip when audio is playing",
                isChecked = config.showMediaPill,
                onCheckedChange = { chk -> onUpdate { it.copy(showMediaPill = chk) } }
            )

            SettingToggleRow(
                title = "Network Speed Meter",
                subtitle = "Real-time transfer speed in KB/s - MB/s",
                isChecked = config.showNetworkSpeed,
                onCheckedChange = { chk -> onUpdate { it.copy(showNetworkSpeed = chk) } }
            )

            SettingToggleRow(
                title = "Privacy Camera/Mic Dot",
                subtitle = "Green dot indicator for sensor privacy",
                isChecked = config.showPrivacyDot,
                onCheckedChange = { chk -> onUpdate { it.copy(showPrivacyDot = chk) } }
            )
        }
    }
}

@Composable
fun ControlCenterCustomizationSection(
    config: StatusBarConfig,
    status: SystemStatus,
    onOpenPreview: () -> Unit,
    onUpdate: ((StatusBarConfig) -> StatusBarConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIXEL 16 CONTROL CENTER",
                    color = PixelPrimaryLilac,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                TextButton(onClick = onOpenPreview) {
                    Text(
                        text = "Open Simulator",
                        color = PixelPrimaryLilac,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            SettingToggleRow(
                title = "Dual Expressive Sliders",
                subtitle = "Wide pill sliders for Brightness and Media Volume",
                isChecked = config.controlCenterDualSlider,
                onCheckedChange = { chk -> onUpdate { it.copy(controlCenterDualSlider = chk) } }
            )

            SettingToggleRow(
                title = "Glassmorphism & Blur Effect",
                subtitle = "Translucent blurred backdrop behind tiles",
                isChecked = config.controlCenterBlur,
                onCheckedChange = { chk -> onUpdate { it.copy(controlCenterBlur = chk) } }
            )

            SettingToggleRow(
                title = "Haptic Vibration Feedback",
                subtitle = "Tactile response on toggles and sliders",
                isChecked = config.hapticFeedbackEnabled,
                onCheckedChange = { chk -> onUpdate { it.copy(hapticFeedbackEnabled = chk) } }
            )
        }
    }
}

@Composable
fun GesturesAndNotchSection(
    config: StatusBarConfig,
    onUpdate: ((StatusBarConfig) -> StatusBarConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "GESTURES & DISPLAY NOTCH",
                color = PixelPrimaryLilac,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Cutout selection
            Column {
                Text(
                    text = "Camera Cutout / Punch Hole",
                    color = PixelTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        label = "Center Punch",
                        isSelected = config.cutoutType == CutoutType.CENTER_PUNCH,
                        onClick = { onUpdate { it.copy(cutoutType = CutoutType.CENTER_PUNCH) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "Left Punch",
                        isSelected = config.cutoutType == CutoutType.LEFT_PUNCH,
                        onClick = { onUpdate { it.copy(cutoutType = CutoutType.LEFT_PUNCH) } },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        label = "None",
                        isSelected = config.cutoutType == CutoutType.NONE,
                        onClick = { onUpdate { it.copy(cutoutType = CutoutType.NONE) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Height Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status Bar Height",
                        color = PixelTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${config.statusBarHeightDp} dp",
                        color = PixelPrimaryLilac,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = config.statusBarHeightDp.toFloat(),
                    onValueChange = { onUpdate { prev -> prev.copy(statusBarHeightDp = it.toInt()) } },
                    valueRange = 24f..48f,
                    colors = SliderDefaults.colors(
                        thumbColor = PixelPrimaryLilac,
                        activeTrackColor = PixelPrimaryLilac,
                        inactiveTrackColor = PixelDarkOutline
                    )
                )
            }

            // Gesture actions
            SettingToggleRow(
                title = "Swipe Down to Open Control Center",
                subtitle = "Trigger Pixel 16 shade by swiping down status bar",
                isChecked = config.gestureSwipeDown == GestureAction.OPEN_PIXEL_CONTROL_CENTER,
                onCheckedChange = { chk ->
                    onUpdate {
                        it.copy(gestureSwipeDown = if (chk) GestureAction.OPEN_PIXEL_CONTROL_CENTER else GestureAction.NONE)
                    }
                }
            )

            SettingToggleRow(
                title = "Double Tap to Lock Screen",
                subtitle = "Quickly lock display by double tapping status bar",
                isChecked = config.gestureDoubleTap == GestureAction.LOCK_SCREEN,
                onCheckedChange = { chk ->
                    onUpdate {
                        it.copy(gestureDoubleTap = if (chk) GestureAction.LOCK_SCREEN else GestureAction.NONE)
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionsSetupSection(
    permissions: com.example.ui.PermissionsState,
    viewModel: MainViewModel
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkSurfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, PixelDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "PERMISSIONS & SYSTEM INTEGRATION",
                color = PixelPrimaryLilac,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            PermissionItemRow(
                title = "Accessibility Service",
                description = "Required to overlay system status bar and handle global swipe/lock gestures.",
                isGranted = permissions.hasAccessibilityPermission,
                onClick = { viewModel.actionController.openAccessibilitySettings() }
            )

            PermissionItemRow(
                title = "Display Over Other Apps",
                description = "Allows drawing Pixel 16 status bar and control center over all applications.",
                isGranted = permissions.hasOverlayPermission,
                onClick = { viewModel.actionController.openOverlaySettings() }
            )

            PermissionItemRow(
                title = "Notification Access",
                description = "Reads active notifications to display live icons in the status bar.",
                isGranted = permissions.hasNotificationPermission,
                onClick = { viewModel.actionController.openNotificationListenerSettings() }
            )

            PermissionItemRow(
                title = "Modify System Settings",
                description = "Enables brightness control and auto-rotate toggles in Control Center.",
                isGranted = permissions.hasWriteSettingsPermission,
                onClick = { viewModel.actionController.openWriteSettings() }
            )

            PermissionItemRow(
                title = "Battery Optimization Exemption",
                description = "Prevents Android from stopping the status bar service in the background.",
                isGranted = permissions.isBatteryOptimizationsIgnored,
                onClick = { viewModel.requestIgnoreBatteryOptimizations() }
            )
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = PixelDarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isGranted) Color(0x3378DC77) else PixelDarkOutline),
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
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0x3378DC77) else Color(0x33FFB74D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Rounded.Check else Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF78DC77) else Color(0xFFFFB74D),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = PixelTextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = PixelTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PixelTextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = PixelTextSecondary,
                fontSize = 11.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PixelOnPrimaryLilac,
                checkedTrackColor = PixelPrimaryLilac,
                uncheckedThumbColor = PixelTextSecondary,
                uncheckedTrackColor = PixelDarkSurfaceVariant
            )
        )
    }
}

@Composable
fun SegmentButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PixelPrimaryLilac else PixelDarkSurfaceVariant,
            contentColor = if (isSelected) PixelOnPrimaryLilac else PixelTextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

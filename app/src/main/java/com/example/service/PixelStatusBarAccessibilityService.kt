package com.example.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.PreferencesRepository
import com.example.model.GestureAction
import com.example.model.StatusBarConfig
import com.example.model.SystemStatus
import com.example.system.SystemActionController
import com.example.system.SystemStateMonitor
import com.example.ui.components.Pixel16ControlCenter
import com.example.ui.components.Pixel16StatusBar
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*

class PixelStatusBarAccessibilityService : AccessibilityService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var statusBarOverlayView: FrameLayout? = null
    private var controlCenterOverlayView: FrameLayout? = null
    private var isControlCenterOpen = false

    private lateinit var prefsRepository: PreferencesRepository
    private lateinit var stateMonitor: SystemStateMonitor
    private lateinit var actionController: SystemActionController

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        prefsRepository = PreferencesRepository.getInstance(this)
        stateMonitor = SystemStateMonitor(this).apply { start() }
        actionController = SystemActionController(this, stateMonitor)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        serviceScope.launch {
            prefsRepository.configFlow.collect { config ->
                if (config.isOverrideEnabled) {
                    showStatusBarOverlay(config)
                } else {
                    removeStatusBarOverlay()
                    hideControlCenter()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}

    override fun onInterrupt() {}

    @SuppressLint("ClickableViewAccessibility")
    private fun showStatusBarOverlay(config: StatusBarConfig) {
        val wm = windowManager ?: return

        if (statusBarOverlayView == null) {
            val density = resources.displayMetrics.density
            val heightPx = (config.statusBarHeightDp * density).toInt()

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                heightPx,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            val frame = FrameLayout(this)
            frame.setViewTreeLifecycleOwner(this)
            frame.setViewTreeViewModelStoreOwner(this)
            frame.setViewTreeSavedStateRegistryOwner(this)

            val composeView = ComposeView(this).apply {
                setContent {
                    MyApplicationTheme {
                        val currentConfig by prefsRepository.configFlow.collectAsStateWithLifecycle()
                        val currentStatus by stateMonitor.statusFlow.collectAsStateWithLifecycle()

                        Pixel16StatusBar(
                            config = currentConfig,
                            status = currentStatus
                        )
                    }
                }
            }

            frame.addView(composeView)

            // Touch gesture recognizer on Status Bar
            val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    when (prefsRepository.getConfig().gestureDoubleTap) {
                        GestureAction.LOCK_SCREEN -> actionController.lockScreen()
                        GestureAction.TAKE_SCREENSHOT -> actionController.takeScreenshot()
                        else -> {}
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    when (prefsRepository.getConfig().gestureLongPress) {
                        GestureAction.TAKE_SCREENSHOT -> actionController.takeScreenshot()
                        GestureAction.OPEN_PIXEL_CONTROL_CENTER -> showControlCenter()
                        else -> actionController.openPowerDialog()
                    }
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (velocityY > 200) {
                        when (prefsRepository.getConfig().gestureSwipeDown) {
                            GestureAction.OPEN_PIXEL_CONTROL_CENTER -> showControlCenter()
                            GestureAction.OPEN_SYSTEM_NOTIFICATIONS -> actionController.openSystemNotifications()
                            GestureAction.OPEN_SYSTEM_QUICK_SETTINGS -> actionController.openSystemQuickSettings()
                            else -> showControlCenter()
                        }
                        return true
                    }
                    return false
                }
            })

            frame.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }

            try {
                wm.addView(frame, params)
                statusBarOverlayView = frame
            } catch (e: Exception) {}
        }
    }

    private fun removeStatusBarOverlay() {
        statusBarOverlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {}
            statusBarOverlayView = null
        }
    }

    fun showControlCenter() {
        if (isControlCenterOpen) return
        val wm = windowManager ?: return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val frame = FrameLayout(this)
        frame.setViewTreeLifecycleOwner(this)
        frame.setViewTreeViewModelStoreOwner(this)
        frame.setViewTreeSavedStateRegistryOwner(this)

        val composeView = ComposeView(this).apply {
            setContent {
                MyApplicationTheme {
                    val currentConfig by prefsRepository.configFlow.collectAsStateWithLifecycle()
                    val currentStatus by stateMonitor.statusFlow.collectAsStateWithLifecycle()

                    Pixel16ControlCenter(
                        config = currentConfig,
                        status = currentStatus,
                        actionController = actionController,
                        onDismiss = { hideControlCenter() }
                    )
                }
            }
        }

        frame.addView(composeView)

        try {
            wm.addView(frame, params)
            controlCenterOverlayView = frame
            isControlCenterOpen = true
            actionController.performHaptic()
        } catch (e: Exception) {}
    }

    fun hideControlCenter() {
        if (!isControlCenterOpen) return
        controlCenterOverlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {}
            controlCenterOverlayView = null
            isControlCenterOpen = false
            actionController.performHaptic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeStatusBarOverlay()
        hideControlCenter()
        stateMonitor.stop()
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        instance = null
    }

    companion object {
        var instance: PixelStatusBarAccessibilityService? = null
            private set
    }
}

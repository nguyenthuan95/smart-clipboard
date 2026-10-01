package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.SmartClipboardApp
import com.example.ui.overlay.FloatingClipboardOverlay
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate900
import com.example.ui.theme.SmartClipboardTheme
import kotlin.math.abs

class FloatingClipboardService : Service() {

    private lateinit var windowManager: WindowManager
    private val preferences = SmartClipboardApp.instance.preferences

    private val bubbleLifecycleOwner = OverlayLifecycleOwner()
    private val panelLifecycleOwner = OverlayLifecycleOwner()

    private var bubbleView: ComposeView? = null
    private var panelView: ComposeView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var panelParams: WindowManager.LayoutParams

    private var isPanelOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        bubbleLifecycleOwner.start()
        panelLifecycleOwner.start()

        initBubbleView()
        initPanelView()
    }

    private fun initBubbleView() {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val sizePx = (50 * density).toInt()

        val initialX = preferences.getBubblePositionX()
        val initialY = preferences.getBubblePositionY()

        bubbleParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        val compose = ComposeView(this).apply {
            setViewTreeLifecycleOwner(bubbleLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(bubbleLifecycleOwner)
            setViewTreeViewModelStoreOwner(bubbleLifecycleOwner)
            setContent {
                SmartClipboardTheme(darkTheme = true) {
                    FloatingBubbleIcon()
                }
            }
        }

        var initialTouchX = 0f
        var initialTouchY = 0f
        var initialParamX = 0
        var initialParamY = 0
        var isDragging = false

        compose.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialParamX = bubbleParams.x
                    initialParamY = bubbleParams.y
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isDragging = true
                        bubbleParams.x = (initialParamX + deltaX).toInt()
                        bubbleParams.y = (initialParamY + deltaY).toInt()
                        try {
                            windowManager.updateViewLayout(compose, bubbleParams)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click detected -> toggle panel!
                        togglePanel()
                    } else {
                        // Save last position
                        preferences.saveBubblePosition(bubbleParams.x, bubbleParams.y)
                    }
                    true
                }
                else -> false
            }
        }

        bubbleView = compose
        try {
            windowManager.addView(bubbleView, bubbleParams)
        } catch (_: Exception) {}
    }

    private fun initPanelView() {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val panelWidth = minOf(width - (24 * displayMetrics.density).toInt(), (450 * displayMetrics.density).toInt())
        val panelHeight = (370 * displayMetrics.density).toInt()

        panelParams = WindowManager.LayoutParams(
            panelWidth,
            panelHeight,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (20 * displayMetrics.density).toInt()
        }

        panelView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(panelLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(panelLifecycleOwner)
            setViewTreeViewModelStoreOwner(panelLifecycleOwner)
            setContent {
                SmartClipboardTheme(darkTheme = true) {
                    FloatingClipboardOverlay(
                        onClose = { setPanelVisibility(false) }
                    )
                }
            }
        }
    }

    fun togglePanel() {
        setPanelVisibility(!isPanelOpen)
    }

    fun setPanelVisibility(visible: Boolean) {
        if (isPanelOpen == visible) return
        isPanelOpen = visible

        val pView = panelView ?: return
        try {
            if (visible) {
                windowManager.addView(pView, panelParams)
            } else {
                windowManager.removeView(pView)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null

        bubbleLifecycleOwner.destroy()
        panelLifecycleOwner.destroy()

        try {
            if (isPanelOpen && panelView != null) {
                windowManager.removeView(panelView)
            }
            if (bubbleView != null) {
                windowManager.removeView(bubbleView)
            }
        } catch (_: Exception) {}
    }

    companion object {
        var instance: FloatingClipboardService? = null
            private set

        val isRunning: Boolean
            get() = instance != null

        fun startService(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, FloatingClipboardService::class.java)
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingClipboardService::class.java)
            try {
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun FloatingBubbleIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Slate900)
            .border(2.dp, CyanAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📋",
            fontSize = 22.sp
        )
    }
}

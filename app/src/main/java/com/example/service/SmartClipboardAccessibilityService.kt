package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.SmartClipboardApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SmartClipboardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    @Volatile
    private var lastCapturedText: String? = null

    @Volatile
    private var lastPastedAdvanceTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isServiceRunning = true

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info

        setupClipboardListener()
    }

    private fun setupClipboardListener() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            checkClipboardCopy(cm)
        }
        cm.addPrimaryClipChangedListener(clipboardListener)
    }

    private fun checkClipboardCopy(cm: ClipboardManager) {
        try {
            val clip = cm.primaryClip ?: return
            if (clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
                if (!text.isNullOrEmpty() && text != lastCapturedText) {
                    val queueRepo = SmartClipboardApp.instance.queueRepository
                    // If this clip is our own advance sync, skip capturing as a new item
                    if (queueRepo.isSuppressedClip(text)) {
                        lastCapturedText = text
                        return
                    }
                    lastCapturedText = text
                    serviceScope.launch {
                        SmartClipboardApp.instance.capturePrimaryClip(cm)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        // 1. Detect when text is pasted into an input field (TYPE_VIEW_TEXT_CHANGED)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            handlePasteEvent(event)
            return
        }

        // 2. Detect when user clicks "Copy" or switches window
        if (cm != null && (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
                    event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
            checkClipboardCopy(cm)
        }
    }

    private fun handlePasteEvent(event: AccessibilityEvent) {
        val now = System.currentTimeMillis()
        // Debounce paste advance (min 400ms between advances)
        if (now - lastPastedAdvanceTime < 400L) return

        val eventText = event.text?.joinToString("") ?: ""
        if (eventText.isEmpty()) return

        val queueRepo = SmartClipboardApp.instance.queueRepository
        val nextItem = runBlocking { queueRepo.getNext() } ?: return

        // If the newly entered text contains the current NEXT item in queue:
        if (eventText.contains(nextItem.text)) {
            lastPastedAdvanceTime = now
            serviceScope.launch {
                val advancedNext = queueRepo.advanceNext()
                withContext(Dispatchers.Main) {
                    if (advancedNext != null) {
                        Toast.makeText(
                            this@SmartClipboardAccessibilityService,
                            "✓ Đã dán: ${nextItem.text}\n➔ Tiếp theo: ${advancedNext.text}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SmartClipboardAccessibilityService,
                            "✓ Đã dán: ${nextItem.text} (Hàng đợi đã hết)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        clipboardListener?.let {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.removePrimaryClipChangedListener(it)
        }
        isServiceRunning = false
        if (instance === this) {
            instance = null
        }
    }

    companion object {
        var instance: SmartClipboardAccessibilityService? = null
            private set
        var isServiceRunning: Boolean = false
            private set
    }
}

package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.SmartClipboardApp
import com.example.util.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmartClipboardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    @Volatile
    private var lastCapturedText: String? = null

    @Volatile
    private var lastPastedAdvanceTime: Long = 0L

    @Volatile
    private var pendingClipRead: Boolean = false

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
        DebugLog.d("SERVICE", "Accessibility Service Connected successfully")
    }

    private fun setupClipboardListener() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            val clip = try { cm.primaryClip } catch (_: Exception) { null }
            val mime = try { cm.primaryClipDescription?.getMimeType(0) } catch (_: Exception) { null }
            DebugLog.d("LISTENER", "fired, readable=${clip != null}, desc=$mime")
            if (clip == null) {
                pendingClipRead = true
            } else {
                checkClipboardCopy(cm, "Listener")
            }
        }
        cm.addPrimaryClipChangedListener(clipboardListener)
    }

    private fun checkClipboardCopy(cm: ClipboardManager, source: String = "Event") {
        try {
            val clip = cm.primaryClip ?: return
            if (clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
                if (!text.isNullOrEmpty() && text != lastCapturedText) {
                    val queueRepo = SmartClipboardApp.instance.queueRepository
                    // If this clip is our own internal sync, skip capturing as a new item
                    if (queueRepo.isSuppressedClip(text)) {
                        lastCapturedText = text
                        DebugLog.d("COPY-SUPPRESSED", "Ignored internal clip sync: '$text'")
                        return
                    }
                    lastCapturedText = text
                    DebugLog.d("COPY-DETECTED", "src=$source, text='$text'")
                    serviceScope.launch {
                        SmartClipboardApp.instance.capturePrimaryClip(cm)
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e("COPY-ERR", "checkClipboardCopy failed", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        // 1. Detect when text is pasted into an input field (TYPE_VIEW_TEXT_CHANGED)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            handlePasteEvent(event)
            return
        }

        // 2. If we had a pending clipboard read that was denied when listener fired, retry on event
        if (pendingClipRead && cm != null) {
            pendingClipRead = false
            checkClipboardCopy(cm, source = "PendingRetry_${event.eventType}")
        }

        // 3. Detect when user clicks "Copy", selects text, or switches window
        if (cm != null && (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
                    event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
            checkClipboardCopy(cm, source = "Event_${event.eventType}")
        }
    }

    private fun norm(s: CharSequence?): String =
        s?.filter { it.isLetterOrDigit() }?.toString().orEmpty()

    private fun handlePasteEvent(event: AccessibilityEvent) {
        // Chỉ xử lý khi có text được thêm vào (bỏ qua backspace, delete, placeholder)
        if (event.addedCount <= 0) return

        if (System.currentTimeMillis() - lastPastedAdvanceTime < 400L) return

        var eventText = event.text?.joinToString("") ?: ""
        if (eventText.isEmpty()) {
            eventText = event.source?.text?.toString() ?: ""
        }

        DebugLog.d(
            "PASTE-EVENT",
            "pkg=${event.packageName} text='$eventText' added=${event.addedCount} removed=${event.removedCount} before='${event.beforeText}'"
        )
        if (eventText.isEmpty()) return

        serviceScope.launch {
            val queueRepo = SmartClipboardApp.instance.queueRepository
            val next = queueRepo.getNext() ?: return@launch
            val n = norm(next.text)
            val e = norm(eventText)
            DebugLog.d("PASTE-CHECK", "expected='$n' received='$e'")

            if (n.isNotEmpty() && e.contains(n)) {
                lastPastedAdvanceTime = System.currentTimeMillis()
                lastCapturedText = null // Reset so subsequent copies are never falsely blocked
                DebugLog.d("PASTE-MATCH", "Matched NEXT! Advancing queue...")
                val advanced = queueRepo.advanceNext()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SmartClipboardAccessibilityService,
                        if (advanced != null) "✓ Đã dán: ${next.text}\n➔ Tiếp theo: ${advanced.text}"
                        else "✓ Đã dán: ${next.text} (Hàng đợi đã hết)",
                        Toast.LENGTH_SHORT
                    ).show()
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

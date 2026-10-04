package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.SmartClipboardApp
import com.example.ui.activity.TransparentClipReaderActivity
import com.example.util.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
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

    private fun clipTs(cm: ClipboardManager): Long =
        if (Build.VERSION.SDK_INT >= 26) cm.primaryClipDescription?.timestamp ?: 0L else 0L

    private fun readClipViaFocus(reason: String) {
        try {
            val intent = Intent(this, TransparentClipReaderActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
            startActivity(intent)
            DebugLog.d("FOCUS-START", "reason=$reason")
        } catch (e: Exception) {
            DebugLog.d("FOCUS-ERR", "reason=$reason: $e")
        }
    }

    private fun checkClipboardCopy(cm: ClipboardManager, source: String = "Event") {
        try {
            val clip = cm.primaryClip
            if (clip == null || clip.itemCount == 0) {
                val mime = try { cm.primaryClipDescription?.getMimeType(0) } catch (_: Exception) { null }
                DebugLog.d("COPY-SKIP", "src=$source ts=${clipTs(cm)} mime=$mime")
                return
            }

            val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
            if (!text.isNullOrEmpty() && text != lastCapturedText) {
                if (text.length > 500 || text.count { it == '\n' } > 5) {
                    DebugLog.d("COPY-REJECT", "Text exceeds limit (len=${text.length})")
                    return
                }

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
            } else if (text != null && text == lastCapturedText) {
                DebugLog.d("COPY-SAME", "src=$source text equals lastCapturedText: '$text'")
            }
        } catch (e: Exception) {
            DebugLog.e("COPY-ERR", "src=$source checkClipboardCopy failed", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. Log mọi sự kiện cửa sổ hoặc notification từ app khác
        if ((event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
             event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) &&
            event.packageName != packageName) {
            DebugLog.d("WIN", "type=${event.eventType} pkg=${event.packageName} cls=${event.className} text=${event.text}")
            if (event.packageName == "com.android.systemui") {
                readClipViaFocus("systemui-win")
            }
        }

        // 2. Log click từ app khác (ít event nên không spam)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED &&
            event.packageName != packageName) {
            DebugLog.d("CLICK", "pkg=${event.packageName} text=${event.text} desc=${event.contentDescription}")
        }

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        // 3. Detect when text is pasted into an input field (TYPE_VIEW_TEXT_CHANGED)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            handlePasteEvent(event)
            return
        }

        // 4. If we had a pending clipboard read that was denied when listener fired, retry on event
        if (pendingClipRead && cm != null) {
            pendingClipRead = false
            checkClipboardCopy(cm, source = "PendingRetry_${event.eventType}")
        }

        // 5. Detect when user clicks "Copy", selects text, or switches window
        if (cm != null && (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                    event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
                    event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
            checkClipboardCopy(cm, source = "Event_${event.eventType}")
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                serviceScope.launch {
                    delay(150)
                    withContext(Dispatchers.Main) { checkClipboardCopy(cm, "Retry150") }
                    delay(350)
                    withContext(Dispatchers.Main) { checkClipboardCopy(cm, "Retry500") }
                }
            }
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

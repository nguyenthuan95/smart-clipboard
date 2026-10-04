package com.example.ui.activity

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.example.SmartClipboardApp
import com.example.util.ClipState
import com.example.util.DebugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransparentClipReaderActivity : Activity() {

    private var hasRead = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLog.d("FOCUS-CREATE", "TransparentClipReaderActivity created")
        window.attributes = window.attributes.apply {
            width = 1
            height = 1
            alpha = 0f
        }

        window.decorView.postDelayed({
            if (!hasRead && !isFinishing) {
                DebugLog.d("FOCUS-TIMEOUT", "Forcing read after delay, hasFocus=${hasWindowFocus()}")
                readAndFinish()
            }
        }, 300)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        DebugLog.d("FOCUS-FOCUS", "hasFocus=$hasFocus")
        if (hasFocus && !hasRead) {
            readAndFinish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasRead && hasWindowFocus()) {
            DebugLog.d("FOCUS-RESUME", "hasWindowFocus=true")
            readAndFinish()
        }
    }

    private fun readAndFinish() {
        hasRead = true
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (cm != null) {
                val clip = cm.primaryClip
                val text = clip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
                val ts = if (Build.VERSION.SDK_INT >= 26) {
                    cm.primaryClipDescription?.timestamp ?: 0L
                } else 0L

                ClipState.knownTs = ts

                if (!text.isNullOrEmpty()) {
                    if (text.length > 500 || text.count { it == '\n' } > 5) {
                        DebugLog.d("FOCUS-READ-REJECT", "Text exceeds limit (len=${text.length})")
                        return
                    }
                    DebugLog.d("FOCUS-READ", "text='$text' ts=$ts")
                    val queueRepo = SmartClipboardApp.instance.queueRepository
                    if (!queueRepo.isSuppressedClip(text)) {
                        CoroutineScope(Dispatchers.Default).launch {
                            SmartClipboardApp.instance.capturePrimaryClip(cm)
                        }
                    } else {
                        DebugLog.d("FOCUS-READ-SUPPRESSED", "Ignored suppressed clip: '$text'")
                    }
                } else {
                    DebugLog.d("FOCUS-READ", "clip is null/empty with focus (ts=$ts)")
                }
            }
        } catch (e: Exception) {
            DebugLog.e("FOCUS-READ-ERR", "Error reading clip with focus", e)
        } finally {
            finish()
            overridePendingTransition(0, 0)
        }
    }
}

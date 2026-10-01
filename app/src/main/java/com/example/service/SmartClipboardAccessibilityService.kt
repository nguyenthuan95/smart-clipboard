package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.SmartClipboardApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmartClipboardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events observation if needed
    }

    override fun onInterrupt() {
        // Interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    /**
     * Pastes an arbitrary text string (e.g. from Clipboard History) into the currently focused editable node.
     * Uses AccessibilityNodeInfo without any X/Y coordinates or OCR.
     */
    fun pasteText(text: String, onCompleted: ((Boolean) -> Unit)? = null) {
        serviceScope.launch {
            val targetNode = findFocusedInputNode()

            if (targetNode == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Không tìm thấy ô nhập liệu.", Toast.LENGTH_SHORT).show()
                }
                onCompleted?.invoke(false)
                return@launch
            }

            val success = tryPerformPaste(targetNode, text)
            targetNode.recycle()

            withContext(Dispatchers.Main) {
                if (success) {
                    onCompleted?.invoke(true)
                } else {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Không thể dán — hãy kiểm tra ô nhập.", Toast.LENGTH_SHORT).show()
                    onCompleted?.invoke(false)
                }
            }
        }
    }

    /**
     * Performs cross-app paste for the NEXT item in the FIFO Queue.
     * Searches for focused and editable input without any X/Y coordinates.
     * Only advances and removes from Queue if paste action succeeded!
     */
    fun pasteNextFromQueue(onCompleted: ((Boolean) -> Unit)? = null) {
        serviceScope.launch {
            val queueRepo = SmartClipboardApp.instance.queueRepository
            val nextItem = queueRepo.getNext()

            if (nextItem == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Hàng đợi rỗng", Toast.LENGTH_SHORT).show()
                }
                onCompleted?.invoke(false)
                return@launch
            }

            // Find target node first
            val targetNode = findFocusedInputNode()
            if (targetNode == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Không tìm thấy ô nhập liệu.", Toast.LENGTH_SHORT).show()
                }
                onCompleted?.invoke(false)
                return@launch
            }

            val success = queueRepo.performPasteAndAdvance { item ->
                tryPerformPaste(targetNode, item.text)
            }
            targetNode.recycle()

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "✓ Đã dán: ${nextItem.text}", Toast.LENGTH_SHORT).show()
                    onCompleted?.invoke(true)
                } else {
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Không thể dán — hãy kiểm tra ô nhập.", Toast.LENGTH_SHORT).show()
                    onCompleted?.invoke(false)
                }
            }
        }
    }

    /**
     * Inserts text into targetNode using standard Android Accessibility actions.
     * Priority:
     * 1. ACTION_SET_TEXT (appending if node already contains text)
     * 2. ACTION_PASTE (via system clipboard)
     */
    private fun tryPerformPaste(targetNode: AccessibilityNodeInfo, textToInsert: String): Boolean {
        if (!targetNode.isEditable) return false

        // Check current text to support continuous sequential pasting without wiping previous pastes
        val existingText = targetNode.text?.toString() ?: ""
        val combinedText = if (existingText.isEmpty()) textToInsert else "$existingText $textToInsert"

        // 1. Try ACTION_SET_TEXT
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, combinedText)
        }
        var performed = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        // 2. Fallback to ACTION_PASTE if ACTION_SET_TEXT was rejected
        if (!performed) {
            try {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                cm?.setPrimaryClip(ClipData.newPlainText("Smart Clipboard", textToInsert))
                performed = targetNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            } catch (_: Exception) {}
        }

        return performed
    }

    /**
     * Finds the currently focused editable input across active interactive windows.
     */
    private fun findFocusedInputNode(): AccessibilityNodeInfo? {
        // 1. Try finding input focus from rootInActiveWindow
        val root = rootInActiveWindow
        if (root != null) {
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focused != null && focused.isEditable) {
                return focused
            }
            focused?.recycle()

            val searched = searchEditableNode(root)
            if (searched != null) {
                return searched
            }
        }

        // 2. Try all interactive windows if root was insufficient
        try {
            val windowList = windows
            for (w in windowList) {
                val wRoot = w.root ?: continue
                val focused = wRoot.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                if (focused != null && focused.isEditable) {
                    return focused
                }
                focused?.recycle()

                val searched = searchEditableNode(wRoot)
                if (searched != null) {
                    return searched
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun searchEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) {
            return AccessibilityNodeInfo.obtain(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = searchEditableNode(child)
            child.recycle()
            if (found != null) {
                return found
            }
        }
        return null
    }

    companion object {
        var instance: SmartClipboardAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }
}

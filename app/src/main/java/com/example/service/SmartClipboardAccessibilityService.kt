package com.example.service

import android.accessibilityservice.AccessibilityService
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
     * Performs cross-app paste using AccessibilityNodeInfo.
     * Searches for focused and editable input without any X/Y coordinates.
     * Only advances the Queue if the paste action succeeded!
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

            val success = queueRepo.performPasteAndAdvance { item ->
                val root = rootInActiveWindow ?: return@performPasteAndAdvance false
                val targetNode = findFocusedEditableNode(root) ?: return@performPasteAndAdvance false

                if (!targetNode.isEditable) {
                    targetNode.recycle()
                    return@performPasteAndAdvance false
                }

                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, item.text)
                }
                var performed = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                if (!performed) {
                    performed = targetNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                }
                targetNode.recycle()
                performed
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    // Slight visual notification
                    Toast.makeText(this@SmartClipboardAccessibilityService, "✓ Đã dán: ${nextItem.text}", Toast.LENGTH_SHORT).show()
                    onCompleted?.invoke(true)
                } else {
                    // As requested in requirement #13
                    Toast.makeText(this@SmartClipboardAccessibilityService, "Không thể dán — hãy kiểm tra ô nhập.", Toast.LENGTH_SHORT).show()
                    onCompleted?.invoke(false)
                }
            }
        }
    }

    private fun findFocusedEditableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 1. Try finding input focus directly
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            return focused
        }
        focused?.recycle()

        // 2. Fallback: Search hierarchy for focused & editable node
        return searchEditableNode(root)
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

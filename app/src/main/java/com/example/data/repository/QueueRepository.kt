package com.example.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.local.QueueDao
import com.example.data.model.ClipboardItem
import com.example.data.model.QueueItem
import com.example.data.pref.PreferencesManager
import com.example.util.DebugLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QueueRepository(
    private val dao: QueueDao,
    private val context: Context,
    private val preferences: PreferencesManager
) {
    private val pasteMutex = Mutex()

    @Volatile
    var lastSuppressedClipText: String? = null
        private set

    val queueItemsFlow: Flow<List<QueueItem>> = dao.getAllFlow()
    val nextItemFlow: Flow<QueueItem?> = dao.getNextFlow()

    suspend fun getQueue(): List<QueueItem> = dao.getAll()

    suspend fun getNext(): QueueItem? = dao.getNext()

    suspend fun getCount(): Int = dao.getCount()

    /**
     * Appends an item to the END of the Queue (Strict FIFO).
     * Never prepends or unshifts.
     * Enforces MAX_QUEUE_SIZE = 50.
     */
    suspend fun enqueue(text: String, clipboardId: Long? = null, isPinned: Boolean = false): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1L

        return pasteMutex.withLock {
            // Do not re-enqueue if the last item in queue has identical text
            val lastItem = dao.getLast()
            if (lastItem != null && lastItem.text == trimmed) {
                DebugLog.d("ENQUEUE-SKIP", "Duplicate with tail: '$trimmed'")
                return@withLock lastItem.id
            }

            val count = dao.getCount()
            if (count >= MAX_QUEUE_SIZE) {
                DebugLog.d("ENQUEUE-FULL", "Queue is full ($count items)")
                return@withLock -1L
            }

            val maxPos = dao.getMaxPosition() ?: 0L
            val newPosition = maxPos + 1L
            val item = QueueItem(
                text = trimmed,
                position = newPosition,
                createdAt = System.currentTimeMillis(),
                clipboardId = clipboardId,
                isPinned = isPinned
            )
            val id = dao.insert(item)
            DebugLog.d("ENQUEUE-SUCCESS", "id=$id, pos=$newPosition, text='$trimmed'")

            // If Queue Mode is active, make sure system clipboard points to the NEXT item
            if (preferences.queueModeEnabled.value) {
                syncSystemClipboardWithNextLocked()
            }
            id
        }
    }

    suspend fun advanceNext(): QueueItem? {
        return pasteMutex.withLock {
            val nextItem = dao.getNext() ?: return@withLock null
            DebugLog.d("ADVANCE", "Removing current NEXT: id=${nextItem.id}, text='${nextItem.text}'")
            dao.deleteById(nextItem.id)

            if (preferences.queueModeEnabled.value) {
                syncSystemClipboardWithNextLocked()
            }
            val newNext = dao.getNext()
            DebugLog.d("ADVANCE-DONE", "New NEXT in DB: '${newNext?.text}'")
            newNext
        }
    }

    /**
     * Serialized paste operation.
     * Only advances and removes item if pasteAction returns TRUE.
     */
    suspend fun performPasteAndAdvance(pasteAction: suspend (QueueItem) -> Boolean): Boolean {
        return pasteMutex.withLock {
            val nextItem = dao.getNext() ?: return@withLock false
            val success = pasteAction(nextItem)

            if (success) {
                dao.deleteById(nextItem.id)
                if (preferences.queueModeEnabled.value) {
                    syncSystemClipboardWithNextLocked()
                }
                true
            } else {
                false
            }
        }
    }

    suspend fun deleteItem(id: Long) {
        pasteMutex.withLock {
            dao.deleteById(id)
            if (preferences.queueModeEnabled.value) {
                syncSystemClipboardWithNextLocked()
            }
        }
    }

    /**
     * Removes all items from the Queue.
     */
    suspend fun clearQueue() {
        pasteMutex.withLock {
            dao.deleteAll()
            DebugLog.d("QUEUE-CLEARED", "All queue items deleted")
        }
    }

    /**
     * Deletes expired unpinned queue items (> 30 minutes).
     */
    suspend fun cleanupExpired(): Int {
        return pasteMutex.withLock {
            val now = System.currentTimeMillis()
            val deleted = dao.deleteExpired(now, ClipboardItem.EXPIRATION_DURATION_MS)
            if (deleted > 0 && preferences.queueModeEnabled.value) {
                syncSystemClipboardWithNextLocked()
            }
            deleted
        }
    }

    /**
     * Called when Queue Mode is toggled ON/OFF.
     */
    suspend fun setQueueModeEnabled(enabled: Boolean) {
        preferences.setQueueModeEnabled(enabled)
        if (enabled) {
            pasteMutex.withLock {
                syncSystemClipboardWithNextLocked()
            }
        }
    }

    /**
     * Synchronizes Android System Clipboard with the current NEXT item in Queue.
     */
    suspend fun syncSystemClipboardWithNext() {
        pasteMutex.withLock {
            syncSystemClipboardWithNextLocked()
        }
    }

    private suspend fun syncSystemClipboardWithNextLocked() {
        val next = dao.getNext() ?: return
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            lastSuppressedClipText = next.text
            val clip = ClipData.newPlainText("Smart Clipboard Queue", next.text)
            clipboardManager.setPrimaryClip(clip)
            DebugLog.d("CLIPBOARD-SYNC", "System clipboard set to NEXT: '${next.text}'")
        } catch (e: Exception) {
            DebugLog.e("CLIPBOARD-SYNC-ERR", "Failed to sync NEXT to system clipboard", e)
        }
    }

    fun isSuppressedClip(text: String?): Boolean {
        if (text == null) return false
        val suppressed = lastSuppressedClipText
        if (suppressed != null && suppressed == text) {
            // Once suppressed, clear it so future real copies of the same text are allowed
            lastSuppressedClipText = null
            return true
        }
        return false
    }

    fun markSuppressed(text: String) {
        lastSuppressedClipText = text
    }

    fun clearSuppressedClip() {
        lastSuppressedClipText = null
    }

    companion object {
        const val MAX_QUEUE_SIZE = 50
    }
}

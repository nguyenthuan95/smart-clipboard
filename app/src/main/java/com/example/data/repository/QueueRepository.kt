package com.example.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.data.local.QueueDao
import com.example.data.model.ClipboardItem
import com.example.data.model.QueueItem
import com.example.data.pref.PreferencesManager
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
     */
    suspend fun enqueue(text: String, clipboardId: Long? = null, isPinned: Boolean = false): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1L

        return pasteMutex.withLock {
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

            // If Queue Mode is active, make sure system clipboard points to the NEXT item
            if (preferences.queueModeEnabled.value) {
                syncSystemClipboardWithNextLocked()
            }
            id
        }
    }

    /**
     * Serialized paste operation.
     * Only advances and removes item if pasteAction returns TRUE.
     * Prevents race conditions during rapid consecutive pastes.
     */
    suspend fun performPasteAndAdvance(pasteAction: suspend (QueueItem) -> Boolean): Boolean {
        return pasteMutex.withLock {
            val nextItem = dao.getNext() ?: return@withLock false
            val success = pasteAction(nextItem)

            if (success) {
                // Remove from queue
                dao.deleteById(nextItem.id)

                // Advance system clipboard to the new NEXT item if queue mode is active
                if (preferences.queueModeEnabled.value) {
                    syncSystemClipboardWithNextLocked()
                }
                true
            } else {
                // Keep nextItem in queue, do not advance
                false
            }
        }
    }

    /**
     * Removes all items from the Queue.
     * Does NOT touch Clipboard History or pinned items.
     */
    suspend fun clearQueue() {
        pasteMutex.withLock {
            dao.deleteAll()
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
        } catch (_: Exception) {
            // Background permission or security exception handling
        }
    }

    fun isSuppressedClip(text: String?): Boolean {
        if (text == null) return false
        val suppressed = lastSuppressedClipText
        return suppressed != null && suppressed == text
    }

    fun clearSuppressedClip() {
        lastSuppressedClipText = null
    }
}

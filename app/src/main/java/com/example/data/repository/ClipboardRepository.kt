package com.example.data.repository

import com.example.data.local.ClipboardDao
import com.example.data.model.ClipboardItem
import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    fun getAllItemsFlow(): Flow<List<ClipboardItem>> = dao.getAllFlow()

    fun getPinnedItemsFlow(): Flow<List<ClipboardItem>> = dao.getPinnedFlow()

    fun getRecentItemsFlow(): Flow<List<ClipboardItem>> {
        return dao.getRecentFlow(System.currentTimeMillis())
    }

    fun searchItemsFlow(query: String): Flow<List<ClipboardItem>> {
        return dao.searchFlow(query, System.currentTimeMillis())
    }

    suspend fun saveCopiedText(text: String, isPinned: Boolean = false): Long {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1L

        val existing = dao.getByExactText(trimmed)
        val now = System.currentTimeMillis()
        val expiresAt = now + ClipboardItem.EXPIRATION_DURATION_MS

        return if (existing != null) {
            // Keep pin status if already pinned, or update expiresAt to move to top
            val updated = existing.copy(
                createdAt = now,
                expiresAt = expiresAt,
                pinned = existing.pinned || isPinned
            )
            dao.update(updated)
            existing.id
        } else {
            val item = ClipboardItem(
                text = trimmed,
                createdAt = now,
                pinned = isPinned,
                expiresAt = expiresAt
            )
            dao.insert(item)
        }
    }

    suspend fun togglePin(id: Long, targetPinState: Boolean? = null) {
        val item = dao.getById(id) ?: return
        val newPinned = targetPinState ?: !item.pinned

        if (!newPinned) {
            // Unpinning: check original expiration time (30 mins from creation)
            val now = System.currentTimeMillis()
            if (now >= item.expiresAt) {
                // If more than 30 minutes have elapsed since it was copied, delete immediately!
                dao.deleteById(id)
                return
            }
        }
        dao.updatePinStatus(id, newPinned)
    }

    suspend fun bulkPin(ids: List<Long>, pin: Boolean) {
        if (ids.isEmpty()) return
        if (pin) {
            dao.updatePinStatusBulk(ids, true)
        } else {
            val now = System.currentTimeMillis()
            for (id in ids) {
                val item = dao.getById(id) ?: continue
                if (now >= item.expiresAt) {
                    dao.deleteById(id)
                } else {
                    dao.updatePinStatus(id, false)
                }
            }
        }
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteByIds(ids: List<Long>) {
        if (ids.isNotEmpty()) {
            dao.deleteByIds(ids)
        }
    }

    suspend fun cleanupExpired(): Int {
        val now = System.currentTimeMillis()
        return dao.deleteExpired(now)
    }

    suspend fun clearAllUnpinned() {
        dao.deleteAllUnpinned()
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}

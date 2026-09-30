package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_items",
    indices = [
        Index(value = ["pinned"]),
        Index(value = ["expiresAt"]),
        Index(value = ["createdAt"])
    ]
)
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val expiresAt: Long = createdAt + EXPIRATION_DURATION_MS
) {
    companion object {
        const val EXPIRATION_DURATION_MINUTES = 30L
        const val EXPIRATION_DURATION_MS = EXPIRATION_DURATION_MINUTES * 60L * 1000L // 30 minutes in milliseconds
    }

    /**
     * Checks if this item has expired based on current time.
     * Note: pinned items are exempt from expiration deletion while pinned.
     */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return now >= expiresAt
    }
}

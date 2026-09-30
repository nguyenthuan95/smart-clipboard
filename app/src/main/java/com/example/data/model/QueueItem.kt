package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "queue_items",
    indices = [
        Index(value = ["position"]),
        Index(value = ["createdAt"])
    ]
)
data class QueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val position: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val clipboardId: Long? = null,
    val isPinned: Boolean = false
)

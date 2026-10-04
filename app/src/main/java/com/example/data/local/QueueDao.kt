package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    fun getAllFlow(): Flow<List<QueueItem>>

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    suspend fun getAll(): List<QueueItem>

    @Query("SELECT * FROM queue_items ORDER BY position ASC LIMIT 1")
    fun getNextFlow(): Flow<QueueItem?>

    @Query("SELECT * FROM queue_items ORDER BY position ASC LIMIT 1")
    suspend fun getNext(): QueueItem?

    @Query("SELECT * FROM queue_items ORDER BY position DESC LIMIT 1")
    suspend fun getLast(): QueueItem?

    @Query("SELECT MAX(position) FROM queue_items")
    suspend fun getMaxPosition(): Long?

    @Query("SELECT COUNT(*) FROM queue_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QueueItem>)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM queue_items")
    suspend fun deleteAll()

    @Query("DELETE FROM queue_items WHERE isPinned = 0 AND (:currentTime - createdAt) >= :expirationDurationMs")
    suspend fun deleteExpired(currentTime: Long, expirationDurationMs: Long): Int

    @Query("DELETE FROM queue_items WHERE clipboardId = :clipboardId")
    suspend fun deleteByClipboardId(clipboardId: Long)
}

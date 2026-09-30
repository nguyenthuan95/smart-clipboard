package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClipboardItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query("SELECT * FROM clipboard_items ORDER BY pinned DESC, createdAt DESC")
    fun getAllFlow(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE pinned = 1 ORDER BY createdAt DESC")
    fun getPinnedFlow(): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE pinned = 0 AND expiresAt > :currentTime ORDER BY createdAt DESC")
    fun getRecentFlow(currentTime: Long): Flow<List<ClipboardItem>>

    @Query("""
        SELECT * FROM clipboard_items 
        WHERE text LIKE '%' || :query || '%' 
        AND (pinned = 1 OR expiresAt > :currentTime)
        ORDER BY pinned DESC, createdAt DESC
    """)
    fun searchFlow(query: String, currentTime: Long): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ClipboardItem?

    @Query("SELECT * FROM clipboard_items WHERE text = :text ORDER BY createdAt DESC LIMIT 1")
    suspend fun getByExactText(text: String): ClipboardItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Update
    suspend fun update(item: ClipboardItem)

    @Query("UPDATE clipboard_items SET pinned = :pinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, pinned: Boolean)

    @Query("UPDATE clipboard_items SET pinned = :pinned WHERE id IN (:ids)")
    suspend fun updatePinStatusBulk(ids: List<Long>, pinned: Boolean)

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clipboard_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM clipboard_items WHERE pinned = 0 AND expiresAt <= :currentTime")
    suspend fun deleteExpired(currentTime: Long): Int

    @Query("DELETE FROM clipboard_items WHERE pinned = 0")
    suspend fun deleteAllUnpinned()

    @Query("DELETE FROM clipboard_items")
    suspend fun deleteAll()
}

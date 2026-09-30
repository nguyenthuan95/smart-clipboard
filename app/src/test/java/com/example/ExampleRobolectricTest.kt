package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ClipboardItem
import com.example.data.pref.PreferencesManager
import com.example.data.repository.ClipboardRepository
import com.example.data.repository.QueueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ClipboardRepository
    private lateinit var queueRepository: QueueRepository
    private lateinit var preferences: PreferencesManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferences = PreferencesManager.getInstance(context)
        repository = ClipboardRepository(database.clipboardDao())
        queueRepository = QueueRepository(database.queueDao(), context, preferences)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Clipboard", appName)
    }

    @Test
    fun `unpinned items expire after 30 minutes and are deleted by cleanup`() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = database.clipboardDao()

        // 1. Fresh item: expires in 30 mins
        val freshItem = ClipboardItem(
            text = "Fresh unpinned item",
            createdAt = now,
            pinned = false,
            expiresAt = now + ClipboardItem.EXPIRATION_DURATION_MS
        )
        val freshId = dao.insert(freshItem)

        // 2. Expired item: expired 1 minute ago
        val expiredItem = ClipboardItem(
            text = "Old unpinned item",
            createdAt = now - (31 * 60 * 1000L),
            pinned = false,
            expiresAt = now - (1 * 60 * 1000L)
        )
        val expiredId = dao.insert(expiredItem)

        // 3. Pinned item: created 2 hours ago, but pinned = true
        val pinnedItem = ClipboardItem(
            text = "Important pinned item",
            createdAt = now - (120 * 60 * 1000L),
            pinned = true,
            expiresAt = now - (90 * 60 * 1000L)
        )
        val pinnedId = dao.insert(pinnedItem)

        // Run cleanup
        val deletedCount = repository.cleanupExpired()
        assertEquals(1, deletedCount)

        // Expired unpinned item should be deleted
        assertNull(dao.getById(expiredId))

        // Fresh unpinned item should still exist
        assertNotNull(dao.getById(freshId))

        // Pinned item should NOT be deleted even after 30 minutes!
        val preservedPinned = dao.getById(pinnedId)
        assertNotNull(preservedPinned)
        assertTrue(preservedPinned!!.pinned)
    }

    @Test
    fun `unpinning an item older than 30 minutes deletes it immediately`() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = database.clipboardDao()

        // Pinned item copied 40 minutes ago
        val item = ClipboardItem(
            text = "Address pinned earlier",
            createdAt = now - (40 * 60 * 1000L),
            pinned = true,
            expiresAt = now - (10 * 60 * 1000L) // expired already according to initial time
        )
        val id = dao.insert(item)

        // Unpin it: requirement states: "Nếu đã quá 30 phút thì xóa ngay"
        repository.togglePin(id, false)

        val result = dao.getById(id)
        assertNull("Item should be deleted immediately because it is already older than 30 minutes", result)
    }

    // --- REQUIREMENT #28: FIFO QUEUE TEST SUITE ---

    @Test
    fun `FIFO test - items are enqueued and dequeued in strict First In First Out order`() = runBlocking {
        val items = listOf("A", "B", "C", "D", "E")
        for (item in items) {
            queueRepository.enqueue(item)
        }

        val queueList = queueRepository.getQueue().map { it.text }
        assertEquals(listOf("A", "B", "C", "D", "E"), queueList)

        // Consume one by one
        val dequeued = mutableListOf<String>()
        while (queueRepository.getNext() != null) {
            queueRepository.performPasteAndAdvance { nextItem ->
                dequeued.add(nextItem.text)
                true
            }
        }
        assertEquals(listOf("A", "B", "C", "D", "E"), dequeued)
        assertEquals(0, queueRepository.getCount())
    }

    @Test
    fun `Failure test - failed paste keeps current item as NEXT and does not advance`() = runBlocking {
        queueRepository.enqueue("A")
        queueRepository.enqueue("B")
        queueRepository.enqueue("C")

        // First paste fails (e.g. input not found or not editable)
        val result = queueRepository.performPasteAndAdvance { nextItem ->
            assertEquals("A", nextItem.text)
            false // Simulate failure
        }

        assertFalse("Paste should report failure", result)

        // Queue must still have A as NEXT, followed by B and C
        val queueTexts = queueRepository.getQueue().map { it.text }
        assertEquals(listOf("A", "B", "C"), queueTexts)

        val currentNext = queueRepository.getNext()
        assertNotNull(currentNext)
        assertEquals("A", currentNext!!.text)
    }

    @Test
    fun `Success test - successful paste removes item from queue and advances to next`() = runBlocking {
        queueRepository.enqueue("A")
        queueRepository.enqueue("B")
        queueRepository.enqueue("C")

        val result = queueRepository.performPasteAndAdvance { nextItem ->
            assertEquals("A", nextItem.text)
            true // Successful paste
        }

        assertTrue("Paste should report success", result)

        val remainingTexts = queueRepository.getQueue().map { it.text }
        assertEquals(listOf("B", "C"), remainingTexts)

        val newNext = queueRepository.getNext()
        assertNotNull(newNext)
        assertEquals("B", newNext!!.text)
    }

    @Test
    fun `Rapid paste test - serialized consecutive pastes consume items without duplicates or skips`() = runBlocking {
        val inputList = listOf("A", "B", "C", "D", "E")
        for (item in inputList) {
            queueRepository.enqueue(item)
        }

        val pastedItems = mutableListOf<String>()

        // Simulate 5 rapid consecutive pastes (e.g. user tapping paste button very quickly)
        val jobs = (1..5).map {
            async {
                queueRepository.performPasteAndAdvance { item ->
                    synchronized(pastedItems) {
                        pastedItems.add(item.text)
                    }
                    true
                }
            }
        }
        jobs.awaitAll()

        assertEquals(listOf("A", "B", "C", "D", "E"), pastedItems)
        assertEquals(0, queueRepository.getCount())
    }

    @Test
    fun `Persistence test - queue items persist in database`() = runBlocking {
        queueRepository.enqueue("A")
        queueRepository.enqueue("B")
        queueRepository.enqueue("C")

        // Re-read directly from database DAO
        val persisted = database.queueDao().getAll().map { it.text }
        assertEquals(listOf("A", "B", "C"), persisted)

        val next = database.queueDao().getNext()
        assertNotNull(next)
        assertEquals("A", next!!.text)
    }

    @Test
    fun `Clear test - clearing queue leaves clipboard history and pinned items intact`() = runBlocking {
        // Add to history
        val histId1 = repository.saveCopiedText("History Clip 1", isPinned = false)
        val histId2 = repository.saveCopiedText("History Clip 2 (Pinned)", isPinned = true)

        // Add to queue
        queueRepository.enqueue("Queue A")
        queueRepository.enqueue("Queue B")
        queueRepository.enqueue("Queue C")

        assertEquals(3, queueRepository.getCount())

        // Clear queue
        queueRepository.clearQueue()

        // Queue must be empty
        assertEquals(0, queueRepository.getCount())
        assertNull(queueRepository.getNext())

        // History items must be completely intact!
        val histItem1 = database.clipboardDao().getById(histId1)
        val histItem2 = database.clipboardDao().getById(histId2)
        assertNotNull("History Clip 1 must still exist", histItem1)
        assertNotNull("Pinned clip must still exist", histItem2)
        assertTrue("Pinned clip must still be pinned", histItem2!!.pinned)
    }
}

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

        val freshItem = ClipboardItem(
            text = "Fresh unpinned item",
            createdAt = now,
            pinned = false,
            expiresAt = now + ClipboardItem.EXPIRATION_DURATION_MS
        )
        val freshId = dao.insert(freshItem)

        val expiredItem = ClipboardItem(
            text = "Old unpinned item",
            createdAt = now - (31 * 60 * 1000L),
            pinned = false,
            expiresAt = now - (1 * 60 * 1000L)
        )
        val expiredId = dao.insert(expiredItem)

        val pinnedItem = ClipboardItem(
            text = "Important pinned item",
            createdAt = now - (120 * 60 * 1000L),
            pinned = true,
            expiresAt = now - (90 * 60 * 1000L)
        )
        val pinnedId = dao.insert(pinnedItem)

        val deletedCount = repository.cleanupExpired()
        assertEquals(1, deletedCount)

        assertNull(dao.getById(expiredId))
        assertNotNull(dao.getById(freshId))
        val preservedPinned = dao.getById(pinnedId)
        assertNotNull(preservedPinned)
        assertTrue(preservedPinned!!.pinned)
    }

    @Test
    fun `unpinning an item older than 30 minutes deletes it immediately`() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = database.clipboardDao()

        val item = ClipboardItem(
            text = "Address pinned earlier",
            createdAt = now - (40 * 60 * 1000L),
            pinned = true,
            expiresAt = now - (10 * 60 * 1000L)
        )
        val id = dao.insert(item)

        repository.togglePin(id, false)

        val result = dao.getById(id)
        assertNull("Item should be deleted immediately because it is already older than 30 minutes", result)
    }

    // --- REQUIREMENT #19: MAXIMUM 50 ITEMS TEST SUITE ---

    @Test
    fun `Test 1 - Add 50 items DH001 to DH050 - Queue has exactly 50 items`() = runBlocking {
        for (i in 1..50) {
            val code = String.format("DH%03d", i)
            val id = queueRepository.enqueue(code)
            assertTrue("Item $code should be inserted successfully", id > 0)
        }

        assertEquals(50, queueRepository.getCount())
        val queueList = queueRepository.getQueue().map { it.text }
        assertEquals(50, queueList.size)
        assertEquals("DH001", queueList.first())
        assertEquals("DH050", queueList.last())
        assertEquals("DH001", queueRepository.getNext()?.text)
    }

    @Test
    fun `Test 2 - Add DH051 when queue has 50 items - DH051 rejected from queue, 50 items kept, history still saves DH051`() = runBlocking {
        // Enqueue 50 items
        for (i in 1..50) {
            val code = String.format("DH%03d", i)
            queueRepository.enqueue(code)
            repository.saveCopiedText(code)
        }
        assertEquals(50, queueRepository.getCount())

        // Try to enqueue 51st item (DH051)
        val enqueueResult = queueRepository.enqueue("DH051")
        assertEquals(-1L, enqueueResult) // Rejected!

        // Queue must still have exactly 50 items (DH001 to DH050)
        assertEquals(50, queueRepository.getCount())
        val queueList = queueRepository.getQueue().map { it.text }
        assertEquals("DH001", queueList.first())
        assertEquals("DH050", queueList.last())
        assertFalse(queueList.contains("DH051"))

        // Clipboard History still records DH051!
        val histId51 = repository.saveCopiedText("DH051")
        assertTrue(histId51 > 0)
        val hist51 = database.clipboardDao().getById(histId51)
        assertNotNull("History must store DH051", hist51)
        assertEquals("DH051", hist51!!.text)
    }

    @Test
    fun `Test 3 - Paste DH001 successfully - DH001 removed, DH002 becomes NEXT`() = runBlocking {
        for (i in 1..50) {
            queueRepository.enqueue(String.format("DH%03d", i))
        }

        val success = queueRepository.performPasteAndAdvance { nextItem ->
            assertEquals("DH001", nextItem.text)
            true // Successful paste
        }

        assertTrue(success)
        assertEquals(49, queueRepository.getCount())

        val next = queueRepository.getNext()
        assertNotNull(next)
        assertEquals("DH002", next!!.text)
    }

    @Test
    fun `Test 4 - Add DH051 after DH001 removed - DH051 appended to end, Queue is DH002 to DH051`() = runBlocking {
        for (i in 1..50) {
            queueRepository.enqueue(String.format("DH%03d", i))
        }

        // Paste DH001
        queueRepository.performPasteAndAdvance { true }
        assertEquals(49, queueRepository.getCount())

        // Now queue has space for 1 item -> Add DH051
        val newId = queueRepository.enqueue("DH051")
        assertTrue("DH051 should be accepted into queue now", newId > 0)
        assertEquals(50, queueRepository.getCount())

        val queueList = queueRepository.getQueue().map { it.text }
        assertEquals("DH002", queueList.first())
        assertEquals("DH051", queueList.last())
        assertEquals("DH002", queueRepository.getNext()?.text)
    }

    @Test
    fun `Test 5 - Paste 50 times continuously - exact sequence DH001 to DH050 without duplicates or skips`() = runBlocking {
        for (i in 1..50) {
            queueRepository.enqueue(String.format("DH%03d", i))
        }

        val pastedList = mutableListOf<String>()

        // Simulate 50 concurrent / rapid paste calls
        val jobs = (1..50).map {
            async {
                queueRepository.performPasteAndAdvance { item ->
                    synchronized(pastedList) {
                        pastedList.add(item.text)
                    }
                    true
                }
            }
        }
        jobs.awaitAll()

        assertEquals(50, pastedList.size)
        for (i in 1..50) {
            val expected = String.format("DH%03d", i)
            assertEquals("Item at index ${i - 1} must match", expected, pastedList[i - 1])
        }
        assertEquals(0, queueRepository.getCount())
    }

    @Test
    fun `Test 6 - Paste failure at DH025 - DH025 remains NEXT, does not advance to DH026`() = runBlocking {
        for (i in 1..50) {
            queueRepository.enqueue(String.format("DH%03d", i))
        }

        // Paste 1..24 successfully
        for (i in 1..24) {
            val res = queueRepository.performPasteAndAdvance { true }
            assertTrue(res)
        }

        assertEquals("DH025", queueRepository.getNext()?.text)

        // Attempt paste at DH025, but it fails (returns false)
        val failResult = queueRepository.performPasteAndAdvance { nextItem ->
            assertEquals("DH025", nextItem.text)
            false // Simulate failure (e.g. non-editable input)
        }

        assertFalse("Paste must report failure", failResult)

        // DH025 must STILL be NEXT!
        val nextAfterFail = queueRepository.getNext()
        assertNotNull(nextAfterFail)
        assertEquals("DH025", nextAfterFail!!.text)
        assertEquals(26, queueRepository.getCount()) // 50 - 24 = 26 items left
    }

    @Test
    fun `Test 7 - Restart app - Queue retains exact order in database`() = runBlocking {
        for (i in 1..10) {
            queueRepository.enqueue(String.format("DH%03d", i))
        }

        // Simulate app restart by creating a new QueueRepository instance pointing to the same DB
        val reloadedQueueRepo = QueueRepository(database.queueDao(), context, preferences)

        val reloadedList = reloadedQueueRepo.getQueue().map { it.text }
        assertEquals(10, reloadedList.size)
        assertEquals("DH001", reloadedList.first())
        assertEquals("DH010", reloadedList.last())
        assertEquals("DH001", reloadedQueueRepo.getNext()?.text)
    }

    @Test
    fun `Clear test - clearing queue leaves clipboard history and pinned items intact`() = runBlocking {
        val histId1 = repository.saveCopiedText("History Clip 1", isPinned = false)
        val histId2 = repository.saveCopiedText("History Clip 2 (Pinned)", isPinned = true)

        queueRepository.enqueue("DH001")
        queueRepository.enqueue("DH002")
        queueRepository.enqueue("DH003")

        assertEquals(3, queueRepository.getCount())

        queueRepository.clearQueue()

        assertEquals(0, queueRepository.getCount())
        assertNull(queueRepository.getNext())

        val histItem1 = database.clipboardDao().getById(histId1)
        val histItem2 = database.clipboardDao().getById(histId2)
        assertNotNull("History Clip 1 must still exist", histItem1)
        assertNotNull("Pinned clip must still exist", histItem2)
        assertTrue("Pinned clip must still be pinned", histItem2!!.pinned)
    }
}

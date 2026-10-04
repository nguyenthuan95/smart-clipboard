package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SmartClipboardApp
import com.example.data.model.ClipboardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClipboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmartClipboardApp.instance.repository
    private val preferences = SmartClipboardApp.instance.preferences
    private val queueRepository = SmartClipboardApp.instance.queueRepository

    val historyEnabled = preferences.historyEnabled
    val vibrateOnPaste = preferences.vibrateOnPaste
    val enableAnimations = preferences.enableAnimations
    val darkMode = preferences.darkModePreference
    val queueModeEnabled = preferences.queueModeEnabled

    val queueItems = queueRepository.queueItemsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val nextQueueItem = queueRepository.nextItemFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _testFieldValue = MutableStateFlow("")
    val testFieldValue: StateFlow<String> = _testFieldValue.asStateFlow()

    private val _systemStatus = MutableStateFlow(checkSystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    val floatingBubbleEnabled = preferences.floatingBubbleEnabled

    val pinnedItems: StateFlow<List<ClipboardItem>> = combine(
        repository.getPinnedItemsFlow(),
        _searchQuery
    ) { items, query ->
        if (query.isBlank()) items
        else items.filter { it.text.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentItems: StateFlow<List<ClipboardItem>> = combine(
        repository.getRecentItemsFlow(),
        _searchQuery
    ) { items, query ->
        if (query.isBlank()) items
        else items.filter { it.text.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initial cleanup
        cleanupExpired()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTestFieldValue(text: String) {
        _testFieldValue.value = text
    }

    fun setMultiSelectMode(enabled: Boolean) {
        _isMultiSelectMode.value = enabled
        if (!enabled) {
            _selectedIds.value = emptySet()
        }
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun selectAll(allIds: List<Long>) {
        _selectedIds.value = allIds.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun addManualClip(text: String) {
        viewModelScope.launch {
            val clipId = repository.saveCopiedText(text)
            if (queueModeEnabled.value) {
                queueRepository.enqueue(text = text, clipboardId = if (clipId > 0) clipId else null)
            }
        }
    }

    fun toggleQueueMode(enabled: Boolean) {
        viewModelScope.launch {
            queueRepository.setQueueModeEnabled(enabled)
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            queueRepository.clearQueue()
        }
    }

    fun enqueueIntoQueue(text: String) {
        viewModelScope.launch {
            queueRepository.enqueue(text)
        }
    }

    fun pasteNextIntoTestField() {
        viewModelScope.launch {
            queueRepository.performPasteAndAdvance { nextItem ->
                pasteIntoTestField(nextItem.text)
                true
            }
        }
    }

    fun togglePin(id: Long) {
        viewModelScope.launch {
            repository.togglePin(id)
        }
    }

    fun bulkPin(pin: Boolean) {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            repository.bulkPin(ids, pin)
            _selectedIds.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun bulkDelete() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            repository.deleteByIds(ids)
            _selectedIds.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }

    fun bulkCopySelected(context: Context) {
        val allItems = pinnedItems.value + recentItems.value
        val selected = _selectedIds.value.mapNotNull { id -> allItems.find { it.id == id } }
        if (selected.isNotEmpty()) {
            val joined = selected.joinToString("\n") { it.text }
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboardManager?.setPrimaryClip(ClipData.newPlainText("Smart Clipboard", joined))
            _isMultiSelectMode.value = false
            _selectedIds.value = emptySet()
        }
    }

    fun pasteIntoTestField(text: String) {
        val current = _testFieldValue.value
        _testFieldValue.value = if (current.isEmpty()) text else "$current $text"
    }

    fun syncFromSystemClipboard() {
        viewModelScope.launch {
            SmartClipboardApp.instance.capturePrimaryClip()
        }
    }

    fun cleanupExpired() {
        viewModelScope.launch {
            repository.cleanupExpired()
        }
    }

    fun clearAllUnpinned() {
        viewModelScope.launch {
            repository.clearAllUnpinned()
        }
    }

    fun refreshSystemStatus() {
        _systemStatus.value = checkSystemStatus()
    }

    fun checkSystemStatus(): SystemStatus {
        return SystemStatus(
            hasOverlayPermission = true,
            isAccessibilityEnabled = true,
            isFloatingBubbleRunning = false
        )
    }

    fun toggleFloatingBubble(enabled: Boolean) {
        preferences.setFloatingBubbleEnabled(enabled)
        refreshSystemStatus()
    }

    fun advanceQueueItem() {
        viewModelScope.launch {
            queueRepository.advanceNext()
        }
    }

    // Settings actions
    fun setHistoryEnabled(enabled: Boolean) = preferences.setHistoryEnabled(enabled)
    fun setVibrateOnPaste(enabled: Boolean) = preferences.setVibrateOnPaste(enabled)
    fun setEnableAnimations(enabled: Boolean) = preferences.setEnableAnimations(enabled)
    fun setDarkMode(mode: String) = preferences.setDarkModePreference(mode)
}

data class SystemStatus(
    val hasOverlayPermission: Boolean,
    val isAccessibilityEnabled: Boolean,
    val isFloatingBubbleRunning: Boolean
)

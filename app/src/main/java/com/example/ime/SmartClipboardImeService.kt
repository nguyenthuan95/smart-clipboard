package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.SmartClipboardApp
import com.example.data.model.ClipboardItem
import com.example.ime.ui.ImeRootView
import com.example.ui.theme.SmartClipboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartClipboardImeService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val store by lazy { ViewModelStore() }
    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val _isClipboardPanelOpen = MutableStateFlow(false)
    val isClipboardPanelOpen: StateFlow<Boolean> = _isClipboardPanelOpen.asStateFlow()

    private val _lastPastedId = MutableStateFlow<Long?>(null)
    val lastPastedId: StateFlow<Long?> = _lastPastedId.asStateFlow()

    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Cleanup expired items on IME launch
        serviceScope.launch(Dispatchers.IO) {
            SmartClipboardApp.instance.repository.cleanupExpired()
        }
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@SmartClipboardImeService)
            setViewTreeViewModelStoreOwner(this@SmartClipboardImeService)
            setViewTreeSavedStateRegistryOwner(this@SmartClipboardImeService)

            setContent {
                SmartClipboardTheme(darkTheme = true) {
                    ImeRootView(
                        service = this@SmartClipboardImeService,
                        onCommitText = { text -> commitText(text) },
                        onDeleteBack = { deleteBackward() },
                        onEnter = { performEnterAction() },
                        onSwitchIme = { switchInputMethod() },
                        onToggleClipboard = { toggleClipboardPanel() },
                        onCloseClipboard = { setClipboardPanelOpen(false) }
                    )
                }
            }
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // Cleanup expired items when input starts
        serviceScope.launch(Dispatchers.IO) {
            SmartClipboardApp.instance.repository.cleanupExpired()
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Capture system clipboard if new text was copied while switching to this field
        serviceScope.launch(Dispatchers.IO) {
            SmartClipboardApp.instance.capturePrimaryClip()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
    }

    fun toggleClipboardPanel() {
        _isClipboardPanelOpen.value = !_isClipboardPanelOpen.value
        if (_isClipboardPanelOpen.value) {
            // Fresh capture and cleanup when panel opens
            serviceScope.launch(Dispatchers.IO) {
                SmartClipboardApp.instance.capturePrimaryClip()
                SmartClipboardApp.instance.repository.cleanupExpired()
            }
        }
    }

    fun setClipboardPanelOpen(open: Boolean) {
        _isClipboardPanelOpen.value = open
    }

    /**
     * Commits text to current input connection.
     * CRITICAL REQUIREMENT: Clipboard panel STAYS OPEN after committing text!
     */
    fun commitClipboardItem(item: ClipboardItem) {
        commitText(item.text)
        _lastPastedId.value = item.id
        triggerHaptic()
    }

    /**
     * Pastes NEXT item in FIFO Queue and advances queue.
     * Panel STAYS OPEN!
     */
    fun pasteNextFromQueue(onResult: ((Boolean) -> Unit)? = null) {
        serviceScope.launch {
            val success = SmartClipboardApp.instance.queueRepository.performPasteAndAdvance { nextItem ->
                val ic = currentInputConnection ?: return@performPasteAndAdvance false
                val committed = ic.commitText(nextItem.text, 1)
                if (committed) {
                    _lastPastedId.value = nextItem.id
                    triggerHaptic()
                }
                committed
            }
            onResult?.invoke(success)
        }
    }

    /**
     * Commits multiple items joined by newline.
     * Clipboard panel STAYS OPEN!
     */
    fun commitMultipleItems(items: List<ClipboardItem>) {
        if (items.isEmpty()) return
        val joined = items.joinToString(separator = "\n") { it.text }
        commitText(joined)
        triggerHaptic()
    }

    fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun deleteBackward() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    fun performEnterAction() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions ?: 0
        val isSendOrDone = (action and EditorInfo.IME_MASK_ACTION) in listOf(
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND
        )
        if (isSendOrDone) {
            ic.performEditorAction(action and EditorInfo.IME_MASK_ACTION)
        } else {
            ic.commitText("\n", 1)
        }
    }

    fun switchInputMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToPreviousInputMethod()
        } else {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            @Suppress("DEPRECATION")
            imm?.showInputMethodPicker()
        }
    }

    fun copyToSystemClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = android.content.ClipData.newPlainText("Smart Clipboard", text)
        clipboardManager.setPrimaryClip(clip)
        triggerHaptic()
    }

    private fun triggerHaptic() {
        // Disabled per user request: no vibration
    }
}

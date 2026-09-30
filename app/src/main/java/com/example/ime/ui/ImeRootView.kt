package com.example.ime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ime.SmartClipboardImeService

@Composable
fun ImeRootView(
    service: SmartClipboardImeService,
    onCommitText: (String) -> Unit,
    onDeleteBack: () -> Unit,
    onEnter: () -> Unit,
    onSwitchIme: () -> Unit,
    onToggleClipboard: () -> Unit,
    onCloseClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClipboardPanelOpen by service.isClipboardPanelOpen.collectAsState()

    AnimatedContent(
        targetState = isClipboardPanelOpen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ime_view_transition",
        modifier = modifier.fillMaxWidth()
    ) { isOpen ->
        if (isOpen) {
            ImeClipboardPanel(
                service = service,
                onClose = onCloseClipboard
            )
        } else {
            ImeKeyboardLayout(
                service = service,
                onCommitText = onCommitText,
                onDeleteBack = onDeleteBack,
                onEnter = onEnter,
                onSwitchIme = onSwitchIme,
                onOpenClipboard = onToggleClipboard
            )
        }
    }
}

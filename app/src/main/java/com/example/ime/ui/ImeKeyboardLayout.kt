package com.example.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SmartClipboardApp
import com.example.ime.SmartClipboardImeService
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentGlow
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun ImeKeyboardLayout(
    service: SmartClipboardImeService,
    onCommitText: (String) -> Unit,
    onDeleteBack: () -> Unit,
    onEnter: () -> Unit,
    onSwitchIme: () -> Unit,
    onOpenClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShifted by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }

    val queueRepo = SmartClipboardApp.instance.queueRepository
    val queueModeEnabled by SmartClipboardApp.instance.preferences.queueModeEnabled.collectAsState()
    val nextQueueItem by queueRepo.nextItemFlow.collectAsState(initial = null)

    val recentClips by SmartClipboardApp.instance.repository.getRecentItemsFlow()
        .collectAsState(initial = emptyList())

    val row1 = if (isSymbolsMode) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
               else listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")

    val row2 = if (isSymbolsMode) listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
               else listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")

    val row3 = if (isSymbolsMode) listOf("*", "\"", "'", ":", ";", "!", "?")
               else listOf("z", "x", "c", "v", "b", "n", "m")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ime_keyboard_layout"),
        color = Slate950
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            // Quick Toolbar: Clipboard button, Queue NEXT chip & Recent chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clipboard Launch Button
                ElevatedButton(
                    onClick = onOpenClipboard,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (queueModeEnabled) AmberPin.copy(alpha = 0.2f) else Slate900,
                        contentColor = if (queueModeEnabled) AmberPin else CyanAccentGlow
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("ime_open_clipboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Clipboard",
                        modifier = Modifier.size(16.dp),
                        tint = if (queueModeEnabled) AmberPin else CyanAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (queueModeEnabled) "FIFO Queue" else "Clipboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // If Queue Mode is ON and has NEXT item: Show prominent ⚡ Dán NEXT button!
                if (queueModeEnabled && nextQueueItem != null) {
                    ElevatedButton(
                        onClick = { service.pasteNextFromQueue() },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = AmberPin,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("ime_paste_next_queue_button")
                    ) {
                        Text(
                            text = "⚡ Dán NEXT: ${nextQueueItem?.text}",
                            maxLines = 1,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Slate950
                        )
                    }
                } else if (recentClips.isNotEmpty()) {
                    // Quick snippet chip from most recent clipboard
                    val topClip = recentClips.first()
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCommitText(topClip.text) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "📋",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = topClip.text,
                                maxLines = 1,
                                fontSize = 11.sp,
                                color = Slate700.copy(alpha = 0.9f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Switch IME Globe
                IconButton(
                    onClick = onSwitchIme,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Đổi bàn phím",
                        tint = Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                row1.forEach { char ->
                    val displayChar = if (isShifted && !isSymbolsMode) char.uppercase() else char
                    KeyButton(
                        text = displayChar,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCommitText(displayChar)
                            if (isShifted) isShifted = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                row2.forEach { char ->
                    val displayChar = if (isShifted && !isSymbolsMode) char.uppercase() else char
                    KeyButton(
                        text = displayChar,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCommitText(displayChar)
                            if (isShifted) isShifted = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3 with Shift & Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shift Key
                KeyButton(
                    text = if (isShifted) "⇧" else "⇪",
                    isSpecial = true,
                    isHighlight = isShifted,
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        if (!isSymbolsMode) {
                            isShifted = !isShifted
                        }
                    }
                )

                row3.forEach { char ->
                    val displayChar = if (isShifted && !isSymbolsMode) char.uppercase() else char
                    KeyButton(
                        text = displayChar,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCommitText(displayChar)
                            if (isShifted) isShifted = false
                        }
                    )
                }

                // Backspace Key
                KeyButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    isSpecial = true,
                    modifier = Modifier.weight(1.3f),
                    onClick = onDeleteBack
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 4: ?123, comma, Spacebar, period, Enter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 123 / ABC mode toggle
                KeyButton(
                    text = if (isSymbolsMode) "ABC" else "?123",
                    isSpecial = true,
                    modifier = Modifier.weight(1.4f),
                    onClick = { isSymbolsMode = !isSymbolsMode }
                )

                // Comma
                KeyButton(
                    text = ",",
                    modifier = Modifier.weight(0.9f),
                    onClick = { onCommitText(",") }
                )

                // Spacebar
                KeyButton(
                    text = "Dấu cách",
                    modifier = Modifier.weight(3.8f),
                    onClick = { onCommitText(" ") }
                )

                // Period
                KeyButton(
                    text = ".",
                    modifier = Modifier.weight(0.9f),
                    onClick = { onCommitText(".") }
                )

                // Enter Key
                KeyButton(
                    icon = Icons.Default.KeyboardReturn,
                    isSpecial = true,
                    isHighlight = true,
                    modifier = Modifier.weight(1.5f),
                    onClick = onEnter
                )
            }
        }
    }
}

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSpecial: Boolean = false,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isHighlight -> CyanAccent.copy(alpha = 0.85f)
        isSpecial -> Slate800
        else -> Slate900
    }

    val contentColor = when {
        isHighlight -> Slate950
        isSpecial -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = contentColor,
                fontSize = if (text.length > 2) 11.sp else 16.sp,
                fontWeight = if (isSpecial || isHighlight) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

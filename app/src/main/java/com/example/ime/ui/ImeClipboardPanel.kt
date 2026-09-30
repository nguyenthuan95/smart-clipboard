package com.example.ime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SmartClipboardApp
import com.example.data.model.ClipboardItem
import com.example.ime.SmartClipboardImeService
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.ClipType
import com.example.util.TextCategoryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImeClipboardPanel(
    service: SmartClipboardImeService,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = SmartClipboardApp.instance.repository
    val queueRepo = SmartClipboardApp.instance.queueRepository
    val scope = rememberCoroutineScope()

    val pinnedItems by repository.getPinnedItemsFlow().collectAsState(initial = emptyList())
    val recentItems by repository.getRecentItemsFlow().collectAsState(initial = emptyList())
    val queueItems by queueRepo.queueItemsFlow.collectAsState(initial = emptyList())
    val nextQueueItem by queueRepo.nextItemFlow.collectAsState(initial = null)
    val queueModeEnabled by SmartClipboardApp.instance.preferences.queueModeEnabled.collectAsState()
    val lastPastedId by service.lastPastedId.collectAsState()

    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<Long>() }

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter items if searching
    val filteredPinned = remember(pinnedItems, searchQuery) {
        if (searchQuery.isBlank()) pinnedItems
        else pinnedItems.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }
    val filteredRecent = remember(recentItems, searchQuery) {
        if (searchQuery.isBlank()) recentItems
        else recentItems.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .testTag("ime_clipboard_panel"),
        color = Slate950
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📋",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Clipboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search toggle
                    IconButton(
                        onClick = {
                            isSearchVisible = !isSearchVisible
                            if (!isSearchVisible) searchQuery = ""
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = if (isSearchVisible) CyanAccent else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Multi-select toggle
                    IconButton(
                        onClick = {
                            isMultiSelectMode = !isMultiSelectMode
                            if (!isMultiSelectMode) selectedItemIds.clear()
                        },
                        modifier = Modifier.size(36.dp).testTag("ime_multi_select_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Chọn nhiều mục",
                            tint = if (isMultiSelectMode) CyanAccent else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Switch back to keyboard
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp).testTag("ime_close_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Bàn phím",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Close (✕)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp).testTag("ime_close_x_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng Clipboard",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar (if opened)
            AnimatedVisibility(visible = isSearchVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = { Text("Tìm trong clipboard...", fontSize = 13.sp, color = Slate400) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // List of Clipboard Items
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredPinned.isEmpty() && filteredRecent.isEmpty() && queueItems.isEmpty() && !queueModeEnabled) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Không tìm thấy nội dung phù hợp" else "Chưa có nội dung sao chép",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section: FIFO QUEUE (Header & Items)
                        if (queueModeEnabled || queueItems.isNotEmpty()) {
                            item(key = "section_fifo_queue") {
                                Surface(
                                    color = Slate900,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (queueModeEnabled) AmberPin else Slate800
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("ime_fifo_queue_section")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "⚡ FIFO QUEUE",
                                                    color = AmberPin,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = AmberPin.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "BẬT",
                                                        color = AmberPin,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            if (queueItems.isNotEmpty()) {
                                                TextButton(
                                                    onClick = {
                                                        scope.launch {
                                                            queueRepo.clearQueue()
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(28.dp).testTag("ime_clear_queue_btn")
                                                ) {
                                                    Text("Clear Queue", color = RedDelete, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (queueItems.isEmpty()) {
                                            Text(
                                                text = "Hàng đợi rỗng. Bật Queue và sao chép để tự động xếp hàng.",
                                                color = Slate400,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                queueItems.forEach { qItem ->
                                                    val isNext = qItem.id == nextQueueItem?.id
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isNext) Slate800 else Slate900)
                                                            .clickable {
                                                                // Tapping paste and advance - panel stays open!
                                                                service.pasteNextFromQueue()
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (isNext) "●" else "○",
                                                            color = if (isNext) AmberPin else Slate400,
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(end = 6.dp)
                                                        )
                                                        Text(
                                                            text = qItem.text,
                                                            color = if (isNext) Color.White else Slate300,
                                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (isNext) {
                                                            Surface(
                                                                color = AmberPin,
                                                                shape = RoundedCornerShape(4.dp),
                                                                modifier = Modifier.padding(start = 6.dp)
                                                            ) {
                                                                Text(
                                                                    text = "NEXT",
                                                                    color = Slate950,
                                                                    fontWeight = FontWeight.Black,
                                                                    fontSize = 9.sp,
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Pinned
                        if (filteredPinned.isNotEmpty()) {
                            item(key = "header_pinned") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = AmberPin,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Đã ghim",
                                        color = AmberPin,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            items(filteredPinned, key = { "pinned_${it.id}" }) { item ->
                                val isSelected = selectedItemIds.contains(item.id)
                                val isRecentlyPasted = lastPastedId == item.id

                                ClipboardItemRow(
                                    item = item,
                                    isMultiSelectMode = isMultiSelectMode,
                                    isSelected = isSelected,
                                    isRecentlyPasted = isRecentlyPasted,
                                    onItemClick = {
                                        if (isMultiSelectMode) {
                                            if (isSelected) selectedItemIds.remove(item.id)
                                            else selectedItemIds.add(item.id)
                                        } else {
                                            // COMMIT TEXT - CLIPBOARD DOES NOT CLOSE!
                                            service.commitClipboardItem(item)
                                        }
                                    },
                                    onTogglePin = {
                                        scope.launch {
                                            repository.togglePin(item.id, false)
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            repository.deleteById(item.id)
                                        }
                                    }
                                )
                            }
                        }

                        // Section: Recent
                        if (filteredRecent.isNotEmpty()) {
                            item(key = "header_recent") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Gần đây",
                                        color = Slate400,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            items(filteredRecent, key = { "recent_${it.id}" }) { item ->
                                val isSelected = selectedItemIds.contains(item.id)
                                val isRecentlyPasted = lastPastedId == item.id

                                ClipboardItemRow(
                                    item = item,
                                    isMultiSelectMode = isMultiSelectMode,
                                    isSelected = isSelected,
                                    isRecentlyPasted = isRecentlyPasted,
                                    onItemClick = {
                                        if (isMultiSelectMode) {
                                            if (isSelected) selectedItemIds.remove(item.id)
                                            else selectedItemIds.add(item.id)
                                        } else {
                                            // COMMIT TEXT - CLIPBOARD DOES NOT CLOSE!
                                            service.commitClipboardItem(item)
                                        }
                                    },
                                    onTogglePin = {
                                        scope.launch {
                                            repository.togglePin(item.id, true)
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            repository.deleteById(item.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Multi-Select Bottom Toolbar
            AnimatedVisibility(visible = isMultiSelectMode) {
                Surface(
                    color = Slate900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pin / Unpin
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val allSelectedArePinned = (pinnedItems.map { it.id }.toSet().containsAll(selectedItemIds))
                                    repository.bulkPin(selectedItemIds.toList(), !allSelectedArePinned)
                                    selectedItemIds.clear()
                                }
                            },
                            enabled = selectedItemIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = if (selectedItemIds.isNotEmpty()) AmberPin else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ghim",
                                color = if (selectedItemIds.isNotEmpty()) AmberPin else Slate700,
                                fontSize = 12.sp
                            )
                        }

                        // Delete
                        TextButton(
                            onClick = {
                                scope.launch {
                                    repository.deleteByIds(selectedItemIds.toList())
                                    selectedItemIds.clear()
                                }
                            },
                            enabled = selectedItemIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (selectedItemIds.isNotEmpty()) RedDelete else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xóa",
                                color = if (selectedItemIds.isNotEmpty()) RedDelete else Slate700,
                                fontSize = 12.sp
                            )
                        }

                        // Copy selected (joined by newline)
                        TextButton(
                            onClick = {
                                val allItems = pinnedItems + recentItems
                                val selectedClips = selectedItemIds.mapNotNull { id -> allItems.find { it.id == id } }
                                val joined = selectedClips.joinToString("\n") { it.text }
                                service.copyToSystemClipboard(joined)
                                selectedItemIds.clear()
                                isMultiSelectMode = false
                            },
                            enabled = selectedItemIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (selectedItemIds.isNotEmpty()) CyanAccent else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sao chép",
                                color = if (selectedItemIds.isNotEmpty()) CyanAccent else Slate700,
                                fontSize = 12.sp
                            )
                        }

                        // Paste All directly into field
                        FilledTonalButton(
                            onClick = {
                                val allItems = pinnedItems + recentItems
                                val selectedClips = selectedItemIds.mapNotNull { id -> allItems.find { it.id == id } }
                                service.commitMultipleItems(selectedClips)
                                // Still stays open!
                            },
                            enabled = selectedItemIds.isNotEmpty(),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Dán tất cả",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClipboardItemRow(
    item: ClipboardItem,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    isRecentlyPasted: Boolean,
    onItemClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipType = remember(item.text) { TextCategoryHelper.detectType(item.text) }
    val timeLabel = remember(item.createdAt) { TextCategoryHelper.formatTimestamp(item.createdAt) }

    // Quick visual state for "Đã dán" pulse
    var showPastedIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(isRecentlyPasted) {
        if (isRecentlyPasted) {
            showPastedIndicator = true
            delay(1200)
            showPastedIndicator = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (showPastedIndicator) 1.5.dp else if (isSelected) 1.dp else 0.5.dp,
                color = when {
                    showPastedIndicator -> GreenSuccess
                    isSelected -> CyanAccent
                    item.pinned -> AmberPin.copy(alpha = 0.5f)
                    else -> Slate800
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onItemClick)
            .testTag("clipboard_item_${item.id}"),
        color = if (showPastedIndicator) Slate800 else Slate900,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox if in multi-select mode
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onItemClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CyanAccent,
                        uncheckedColor = Slate400
                    ),
                    modifier = Modifier.size(24.dp).padding(end = 6.dp)
                )
            } else {
                // Category icon badge
                val (icon, tint) = when (clipType) {
                    ClipType.PHONE -> Icons.Default.Phone to Color(0xFF38BDF8)
                    ClipType.LINK -> Icons.Default.Link to Color(0xFF60A5FA)
                    ClipType.ADDRESS -> Icons.Default.LocationOn to Color(0xFFF472B6)
                    ClipType.TEXT -> Icons.Default.Description to Slate400
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(tint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Text content and time
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = item.text,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeLabel,
                        color = Slate400,
                        fontSize = 11.sp
                    )

                    // Visual badge "✓ Đã dán" if recently tapped
                    AnimatedVisibility(
                        visible = showPastedIndicator,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Đã dán",
                                color = GreenSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Pin / Unpin button
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(32.dp).testTag("pin_button_${item.id}")
            ) {
                Icon(
                    imageVector = if (item.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (item.pinned) "Bỏ ghim" else "Ghim",
                    tint = if (item.pinned) AmberPin else Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Slate400,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

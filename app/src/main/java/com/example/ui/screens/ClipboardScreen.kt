package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipboardItem
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ClipboardViewModel
import com.example.util.ClipType
import com.example.util.TextCategoryHelper

@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pinnedItems by viewModel.pinnedItems.collectAsState()
    val recentItems by viewModel.recentItems.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val queueModeEnabled by viewModel.queueModeEnabled.collectAsState()
    val queueItems by viewModel.queueItems.collectAsState()
    val nextQueueItem by viewModel.nextQueueItem.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Status Bar: Overlay & Accessibility setup banner
            if (!systemStatus.hasOverlayPermission || !systemStatus.isAccessibilityEnabled) {
                Surface(
                    color = Slate900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigateToSetup() }
                        .border(1.dp, AmberPin.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberPin,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kích hoạt Nút nổi Clipboard & Tự động dán",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Chạm để bật Hiển thị trên ứng dụng khác & Hỗ trợ tiếp cận",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }

            // FIFO Queue Mode Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (queueModeEnabled) AmberPin.copy(alpha = 0.8f) else Slate800
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("fifo_queue_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚡ FIFO QUEUE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AmberPin
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AmberPin.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "MẶC ĐỊNH BẬT",
                                    color = AmberPin,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (queueItems.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearQueue() },
                                modifier = Modifier.height(28.dp).testTag("app_clear_queue_button"),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text("Xóa Hàng Đợi", color = RedDelete, fontSize = 11.sp)
                            }
                        }
                    }

                    if (queueItems.isEmpty()) {
                        Text(
                            text = "Hàng đợi luôn sẵn sàng (Tối đa 50 item). Hãy copy các mã đơn hàng/văn bản liên tục để tự động xếp hàng và dán tuần tự.",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            queueItems.take(5).forEach { qItem ->
                                val isNext = qItem.id == nextQueueItem?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isNext) Slate800 else Slate950)
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
                                        color = if (isNext) Color.White else Slate400,
                                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isNext) {
                                        Surface(
                                            color = AmberPin,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "NEXT",
                                                color = Slate950,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.pasteNextIntoTestField() },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberPin),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp).testTag("app_paste_next_button")
                            ) {
                                Text(
                                    text = "⚡ Dán NEXT vào ô thử",
                                    color = Slate950,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.clearQueue() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp).testTag("app_clear_queue_button")
                            ) {
                                Text("Xóa Hàng Đợi", color = RedDelete, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Search Bar & Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Tìm kiếm clipboard...", fontSize = 13.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp).testTag("search_text_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sync from Clipboard
                IconButton(
                    onClick = {
                        viewModel.syncFromSystemClipboard()
                        Toast.makeText(context, "Đã đồng bộ từ bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Slate900, RoundedCornerShape(10.dp))
                        .testTag("sync_clipboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Đồng bộ",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Multi-select toggle
                IconButton(
                    onClick = { viewModel.setMultiSelectMode(!isMultiSelectMode) },
                    modifier = Modifier
                        .size(46.dp)
                        .background(if (isMultiSelectMode) CyanAccent else Slate900, RoundedCornerShape(10.dp))
                        .testTag("multi_select_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Chọn nhiều",
                        tint = if (isMultiSelectMode) Slate950 else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Clipboard Items List
            if (pinnedItems.isEmpty() && recentItems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Không tìm thấy kết quả" else "Chưa có nội dung clipboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sao chép bất kỳ văn bản nào trên máy để tự động lưu, hoặc chạm nút (+) bên dưới để thêm thủ công.",
                            color = Slate400,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pinned Section
                    if (pinnedItems.isNotEmpty()) {
                        item(key = "header_pinned") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = AmberPin,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Đã ghim",
                                    color = AmberPin,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        items(pinnedItems, key = { "pinned_${it.id}" }) { item ->
                            val isSelected = selectedIds.contains(item.id)
                            AppClipboardCard(
                                item = item,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = isSelected,
                                onSelectToggle = { viewModel.toggleSelection(item.id) },
                                onTogglePin = { viewModel.togglePin(item.id) },
                                onDelete = { viewModel.deleteItem(item.id) },
                                onCopyToSystem = {
                                    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("Smart Clipboard", item.text))
                                    Toast.makeText(context, "Đã sao chép vào bộ nhớ", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Recent Section
                    if (recentItems.isNotEmpty()) {
                        item(key = "header_recent") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = "Gần đây",
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        items(recentItems, key = { "recent_${it.id}" }) { item ->
                            val isSelected = selectedIds.contains(item.id)
                            AppClipboardCard(
                                item = item,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = isSelected,
                                onSelectToggle = { viewModel.toggleSelection(item.id) },
                                onTogglePin = { viewModel.togglePin(item.id) },
                                onDelete = { viewModel.deleteItem(item.id) },
                                onCopyToSystem = {
                                    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    cm?.setPrimaryClip(android.content.ClipData.newPlainText("Smart Clipboard", item.text))
                                    Toast.makeText(context, "Đã sao chép vào bộ nhớ", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar when Multi-Select is Active
            AnimatedVisibility(visible = isMultiSelectMode) {
                Surface(
                    color = Slate900,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pin / Unpin
                        TextButton(
                            onClick = {
                                val allItems = pinnedItems + recentItems
                                val selectedClips = selectedIds.mapNotNull { id -> allItems.find { it.id == id } }
                                val allArePinned = selectedClips.all { it.pinned }
                                viewModel.bulkPin(!allArePinned)
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = AmberPin, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ghim", color = AmberPin, fontSize = 12.sp)
                        }

                        // Delete
                        TextButton(
                            onClick = { viewModel.bulkDelete() },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RedDelete, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xóa", color = RedDelete, fontSize = 12.sp)
                        }

                        // Copy multiple items joined by newline
                        Button(
                            onClick = {
                                viewModel.bulkCopySelected(context)
                                Toast.makeText(context, "Đã sao chép các mục đã chọn (ngăn cách bằng dòng mới)", Toast.LENGTH_SHORT).show()
                            },
                            enabled = selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sao chép", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppClipboardCard(
    item: ClipboardItem,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onCopyToSystem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipType = remember(item.text) { TextCategoryHelper.detectType(item.text) }
    val timeLabel = remember(item.createdAt) { TextCategoryHelper.formatTimestamp(item.createdAt) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) CyanAccent else if (item.pinned) AmberPin.copy(alpha = 0.4f) else Slate800,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (isMultiSelectMode) onSelectToggle()
                else onCopyToSystem()
            }
            .testTag("app_clipboard_card_${item.id}"),
        color = Slate900,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMultiSelectMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CyanAccent,
                            uncheckedColor = Slate400
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    // Type badge
                    val (icon, tint) = when (clipType) {
                        ClipType.PHONE -> Icons.Default.Phone to Color(0xFF38BDF8)
                        ClipType.LINK -> Icons.Default.Link to Color(0xFF60A5FA)
                        ClipType.ADDRESS -> Icons.Default.LocationOn to Color(0xFFF472B6)
                        ClipType.TEXT -> Icons.Default.Description to Slate400
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(tint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Time label
                Text(
                    text = timeLabel,
                    color = Slate400,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                // Pin toggle button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (item.pinned) "Bỏ ghim" else "Ghim",
                        tint = if (item.pinned) AmberPin else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy to system
                IconButton(
                    onClick = onCopyToSystem,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Sao chép",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text
            Text(
                text = item.text,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }
    }
}

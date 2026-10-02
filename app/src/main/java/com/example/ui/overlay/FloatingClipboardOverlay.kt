package com.example.ui.overlay

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.SmartClipboardApp
import com.example.data.model.ClipboardItem
import com.example.data.model.QueueItem
import com.example.service.SmartClipboardAccessibilityService
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun FloatingClipboardOverlay(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardRepo = SmartClipboardApp.instance.repository
    val queueRepo = SmartClipboardApp.instance.queueRepository

    val pinnedItems by clipboardRepo.getPinnedItemsFlow().collectAsState(initial = emptyList())
    val recentItems by clipboardRepo.getRecentItemsFlow().collectAsState(initial = emptyList())
    val queueItems by queueRepo.queueItemsFlow.collectAsState(initial = emptyList())
    val nextQueueItem by queueRepo.nextItemFlow.collectAsState(initial = null)

    // Function to handle paste through AccessibilityService
    fun doPasteText(text: String) {
        val service = SmartClipboardAccessibilityService.instance
        if (service != null) {
            service.pasteText(text)
        } else {
            Toast.makeText(context, "Vui lòng bật Hỗ trợ tiếp cận (Accessibility) trong Cài đặt", Toast.LENGTH_LONG).show()
        }
    }

    fun doPasteNext() {
        val service = SmartClipboardAccessibilityService.instance
        if (service != null) {
            service.pasteNextFromQueue()
        } else {
            Toast.makeText(context, "Vui lòng bật Hỗ trợ tiếp cận (Accessibility) trong Cài đặt", Toast.LENGTH_LONG).show()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("floating_clipboard_overlay_panel"),
        shape = RoundedCornerShape(16.dp),
        color = Slate950,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        text = "Smart Clipboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp).testTag("floating_panel_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ==========================================
                // SECTION 1: FIFO QUEUE
                // ==========================================
                item(key = "overlay_fifo_queue_header") {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberPin.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("overlay_fifo_queue_card")
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
                                            text = "MẶC ĐỊNH BẬT",
                                            color = AmberPin,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                if (queueItems.isNotEmpty()) {
                                    TextButton(
                                        onClick = { scope.launch { queueRepo.clearQueue() } },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        modifier = Modifier.height(28.dp).testTag("overlay_clear_queue_btn")
                                    ) {
                                        Text("Clear Queue", color = RedDelete, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (queueItems.isEmpty()) {
                                Text(
                                    text = "Hàng đợi rỗng. Sao chép các mã/văn bản liên tục để tự động xếp hàng và dán tuần tự.",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    queueItems.take(6).forEach { qItem ->
                                        val isNext = qItem.id == nextQueueItem?.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isNext) Slate800 else Slate900)
                                                .clickable { doPasteNext() }
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
                                                    shape = RoundedCornerShape(4.dp)
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

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { doPasteNext() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPin),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(32.dp).testTag("overlay_paste_next_btn")
                                ) {
                                    Text(
                                        text = "⚡ Dán NEXT: ${nextQueueItem?.text ?: ""}",
                                        color = Slate950,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION 2: ĐÃ GHIM (PINNED ITEMS)
                // ==========================================
                if (pinnedItems.isNotEmpty()) {
                    item(key = "overlay_pinned_header") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    items(pinnedItems, key = { "pinned_${it.id}" }) { item ->
                        OverlayClipboardRow(
                            item = item,
                            onItemClick = { doPasteText(item.text) },
                            onTogglePin = { scope.launch { clipboardRepo.togglePin(item.id) } },
                            onDelete = { scope.launch { clipboardRepo.deleteById(item.id) } }
                        )
                    }
                }

                // ==========================================
                // SECTION 3: GẦN ĐÂY (RECENT CLIPS)
                // ==========================================
                if (recentItems.isNotEmpty()) {
                    item(key = "overlay_recent_header") {
                        Text(
                            text = "Gần đây",
                            color = Slate400,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(recentItems, key = { "recent_${it.id}" }) { item ->
                        OverlayClipboardRow(
                            item = item,
                            onItemClick = { doPasteText(item.text) },
                            onTogglePin = { scope.launch { clipboardRepo.togglePin(item.id) } },
                            onDelete = { scope.launch { clipboardRepo.deleteById(item.id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayClipboardRow(
    item: ClipboardItem,
    onItemClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.text,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (item.pinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Ghim",
                    tint = if (item.pinned) AmberPin else Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.QueueItem
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ClipboardViewModel

@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val queueItems by viewModel.queueItems.collectAsState()
    val nextQueueItem by viewModel.nextQueueItem.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("clipboard_screen")
    ) {
        // App Title & Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart Clipboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Surface(
                color = AmberPin.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "FIFO: ${queueItems.size}/50",
                    color = AmberPin,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Action Buttons Row (Nạp NEXT & Xóa Hàng Đợi)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (queueItems.isNotEmpty()) {
                        val currentNext = nextQueueItem?.text
                        viewModel.advanceQueueItem()
                        Toast.makeText(
                            context,
                            "✓ Đã dán: $currentNext → Chuyển sang mục tiếp theo",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(context, "Hàng đợi đang rỗng", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = queueItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPin,
                    disabledContainerColor = Slate800
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("app_paste_next_button")
            ) {
                Text(
                    text = "⚡ Nạp NEXT vào Clipboard",
                    color = if (queueItems.isNotEmpty()) Slate950 else Slate400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (queueItems.isNotEmpty()) {
                OutlinedButton(
                    onClick = { viewModel.clearQueue() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDelete),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedDelete.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("app_clear_queue_button")
                ) {
                    Text("Xóa Hết", color = RedDelete, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Section Title: Danh sách Hàng Đợi
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = AmberPin,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DANH SÁCH CHỜ DÁN (VÀO TRƯỚC - RA TRƯỚC)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberPin,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // List of Queue Items or Empty State
        if (queueItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(AmberPin.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAddCheck,
                            contentDescription = null,
                            tint = AmberPin,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Hàng đợi đang trống",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hãy Sao chép (Copy) các mã đơn hàng, số điện thoại, văn bản liên tục ở bất kỳ ứng dụng nào.\n\nChúng sẽ tự động xếp hàng tại đây để bạn dán tuần tự từng mục cực nhanh!",
                        color = Slate400,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(queueItems, key = { _, item -> item.id }) { index, item ->
                    val isNext = item.id == nextQueueItem?.id
                    QueueItemRow(
                        item = item,
                        index = index + 1,
                        isNext = isNext,
                        onCopy = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("Smart Clipboard", item.text))
                            Toast.makeText(context, "✓ Đã chép: ${item.text}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteQueueItem(item.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(
    item: QueueItem,
    index: Int,
    isNext: Boolean,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isNext) Slate900 else Slate950),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isNext) 1.5.dp else 1.dp,
            color = if (isNext) AmberPin else Slate800
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index or NEXT Badge
            if (isNext) {
                Surface(
                    color = AmberPin,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "NEXT",
                        color = Slate950,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            } else {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "#$index",
                        color = Slate300,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Content
            Text(
                text = item.text,
                color = if (isNext) Color.White else Slate300,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Quick Copy
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Sao chép",
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Delete
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
    }
}

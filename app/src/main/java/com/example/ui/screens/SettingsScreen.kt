package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ClipboardViewModel

@Composable
fun SettingsScreen(
    viewModel: ClipboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyEnabled by viewModel.historyEnabled.collectAsState()
    val vibrateOnPaste by viewModel.vibrateOnPaste.collectAsState()
    val enableAnimations by viewModel.enableAnimations.collectAsState()
    val darkModePref by viewModel.darkMode.collectAsState()
    val queueModeEnabled by viewModel.queueModeEnabled.collectAsState()

    var showClearUnpinnedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cài đặt ứng dụng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Group 1: General Clipboard behavior
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Clipboard history
                SettingSwitchRow(
                    icon = Icons.Default.History,
                    iconTint = CyanAccent,
                    title = "Lưu lịch sử Clipboard",
                    description = "Tự động ghi nhớ văn bản bạn sao chép",
                    checked = historyEnabled,
                    onCheckedChange = { viewModel.setHistoryEnabled(it) },
                    testTag = "setting_history_switch"
                )

                // FIFO Queue Mode
                SettingSwitchRow(
                    icon = Icons.Default.Bolt,
                    iconTint = AmberPin,
                    title = "Hàng đợi FIFO (Queue Mode)",
                    description = "Vào trước - Ra trước. Tự động nạp item tiếp theo vào bộ nhớ và dán tuần tự",
                    checked = queueModeEnabled,
                    onCheckedChange = { viewModel.toggleQueueMode(it) },
                    testTag = "setting_queue_mode_switch"
                )

                // 30-min auto delete explanation
                SettingInfoRow(
                    icon = Icons.Default.Timer,
                    iconTint = AmberPin,
                    title = "Tự động dọn dẹp sau 30 phút",
                    description = "Clipboard chưa ghim sẽ tự động biến mất sau 30 phút. Các mục đã ghim được giữ lại vô thời hạn."
                )

                // Vibration feedback
                SettingSwitchRow(
                    icon = Icons.Default.Vibration,
                    iconTint = CyanAccent,
                    title = "Rung khi dán",
                    description = "Rung phản hồi nhẹ khi chạm chọn clipboard để dán",
                    checked = vibrateOnPaste,
                    onCheckedChange = { viewModel.setVibrateOnPaste(it) },
                    testTag = "setting_vibrate_switch"
                )

                // Animations
                SettingSwitchRow(
                    icon = Icons.Default.Animation,
                    iconTint = CyanAccent,
                    title = "Hiệu ứng hoạt họa",
                    description = "Hiệu ứng mượt mà khi mở bảng và chuyển đổi",
                    checked = enableAnimations,
                    onCheckedChange = { viewModel.setEnableAnimations(it) },
                    testTag = "setting_animation_switch"
                )
            }
        }

        // Group 2: Theme
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Chế độ giao diện", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        "dark" to "Tối",
                        "light" to "Sáng",
                        "system" to "Hệ thống"
                    )

                    themes.forEach { (mode, label) ->
                        val isSelected = darkModePref == mode
                        Button(
                            onClick = { viewModel.setDarkMode(mode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CyanAccent else Slate800,
                                contentColor = if (isSelected) Slate950 else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Group 3: Storage & Maintenance
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Bảo trì & Dữ liệu", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)

                // Cleanup expired now
                OutlinedButton(
                    onClick = {
                        viewModel.cleanupExpired()
                        Toast.makeText(context, "Đã quét và xóa các mục quá hạn 30 phút", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Dọn dẹp mục hết hạn ngay", color = CyanAccent)
                }

                // Clear all unpinned
                OutlinedButton(
                    onClick = { showClearUnpinnedDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = RedDelete, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Xóa tất cả mục chưa ghim", color = RedDelete)
                }
            }
        }
    }

    // Confirmation dialog
    if (showClearUnpinnedDialog) {
        AlertDialog(
            onDismissRequest = { showClearUnpinnedDialog = false },
            title = { Text("Xác nhận xóa?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "Thao tác này sẽ xóa tất cả clipboard chưa được ghim. Các mục đã ghim sẽ được giữ nguyên.",
                    color = Slate400,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllUnpinned()
                        showClearUnpinnedDialog = false
                        Toast.makeText(context, "Đã xóa toàn bộ mục chưa ghim", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDelete)
                ) {
                    Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearUnpinnedDialog = false }) {
                    Text("Hủy", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            Text(text = description, fontSize = 12.sp, color = Slate400, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Slate950,
                checkedTrackColor = CyanAccent,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate800
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun SettingInfoRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
            Text(text = description, fontSize = 12.sp, color = Slate400, lineHeight = 16.sp)
        }
    }
}

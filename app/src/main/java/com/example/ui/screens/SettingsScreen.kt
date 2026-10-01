package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedDelete
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
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
    val systemStatus by viewModel.systemStatus.collectAsState()
    val floatingBubbleEnabled by viewModel.floatingBubbleEnabled.collectAsState()
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Text(
            text = "Cài đặt & Kích hoạt",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Banner: Independent Manager Explanation
        Surface(
            color = Slate900,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Smart Clipboard Manager độc lập",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hoạt động song song cùng bàn phím hiện tại của bạn (Gboard, Samsung Keyboard...). Không cần đặt app làm bàn phím mặc định.",
                    fontSize = 13.sp,
                    color = Slate300,
                    lineHeight = 18.sp
                )
            }
        }

        // ==========================================
        // SECTION 1: KÍCH HOẠT QUYỀN HỆ THỐNG
        // ==========================================
        Text(
            text = "KÍCH HOẠT HỆ THỐNG",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 1.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step 1: Cho phép hiển thị trên ứng dụng khác
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (systemStatus.hasOverlayPermission) GreenSuccess.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            fontWeight = FontWeight.Bold,
                            color = if (systemStatus.hasOverlayPermission) GreenSuccess else CyanAccent,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hiển thị trên ứng dụng khác",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (systemStatus.hasOverlayPermission) "✓ Đã cấp quyền hiển thị" else "Cần cấp quyền để hiện nút nổi 📋",
                            fontSize = 12.sp,
                            color = if (systemStatus.hasOverlayPermission) GreenSuccess else Slate400
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (systemStatus.hasOverlayPermission) Slate800 else CyanAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("open_overlay_settings_btn")
                    ) {
                        Text(
                            text = if (systemStatus.hasOverlayPermission) "Đã cấp" else "Cấp quyền",
                            color = if (systemStatus.hasOverlayPermission) Color.White else Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Step 2: Bật Accessibility Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (systemStatus.isAccessibilityEnabled) GreenSuccess.copy(alpha = 0.2f) else AmberPin.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2",
                            fontWeight = FontWeight.Bold,
                            color = if (systemStatus.isAccessibilityEnabled) GreenSuccess else AmberPin,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dịch vụ Hỗ trợ tiếp cận",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (systemStatus.isAccessibilityEnabled) "✓ Đã bật Hỗ trợ tiếp cận" else "Cần bật để tự động dán vào ô nhập",
                            fontSize = 12.sp,
                            color = if (systemStatus.isAccessibilityEnabled) GreenSuccess else Slate400
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (systemStatus.isAccessibilityEnabled) Slate800 else AmberPin
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("open_accessibility_settings_btn")
                    ) {
                        Text(
                            text = if (systemStatus.isAccessibilityEnabled) "Đã bật" else "Bật ngay",
                            color = if (systemStatus.isAccessibilityEnabled) Color.White else Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Step 3: Nút nổi Clipboard Switch
                SettingSwitchRow(
                    icon = Icons.Default.Bolt,
                    iconTint = CyanAccent,
                    title = "Nút nổi Clipboard 📋 trên màn hình",
                    description = "Hiển thị bong bóng nổi để mở bảng Clipboard trên bất kỳ app nào (Zalo, Chrome, Messenger...)",
                    checked = floatingBubbleEnabled,
                    onCheckedChange = { viewModel.toggleFloatingBubble(it) },
                    testTag = "setting_floating_bubble_switch"
                )
            }
        }

        // ==========================================
        // SECTION 2: HƯỚNG DẪN 5 BƯỚC SỬ DỤNG
        // ==========================================
        Text(
            text = "HƯỚNG DẪN SỬ DỤNG",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 1.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GuideStepRow(step = "1", text = "Cấp quyền 'Hiển thị trên ứng dụng khác' ở bước 1.")
                GuideStepRow(step = "2", text = "Bật 'Hỗ trợ tiếp cận (Accessibility)' cho Smart Clipboard ở bước 2.")
                GuideStepRow(step = "3", text = "Quay lại ứng dụng Smart Clipboard.")
                GuideStepRow(step = "4", text = "Mở một ứng dụng bất kỳ có ô nhập liệu (Zalo, Chrome, Messenger...).")
                GuideStepRow(step = "5", text = "Nhấn nút nổi 📋 để mở bảng Clipboard và chạm nội dung để dán liên tục mà không cần đóng bảng!")
            }
        }

        // ==========================================
        // SECTION 2: CÀI ĐẶT CLIPBOARD & HÀNG ĐỢI FIFO
        // ==========================================
        Text(
            text = "CÀI ĐẶT CLIPBOARD & HÀNG ĐỢI",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 1.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // FIFO Queue Mode
                SettingSwitchRow(
                    icon = Icons.Default.Bolt,
                    iconTint = AmberPin,
                    title = "Hàng đợi FIFO (Mặc định BẬT)",
                    description = "Vào trước - Ra trước (Tối đa 50 item). Tự động nạp item tiếp theo vào bộ nhớ và dán tuần tự.",
                    checked = queueModeEnabled,
                    onCheckedChange = { viewModel.toggleQueueMode(it) },
                    testTag = "setting_queue_mode_switch"
                )

                // Clipboard history
                SettingSwitchRow(
                    icon = Icons.Default.History,
                    iconTint = CyanAccent,
                    title = "Lưu lịch sử Clipboard",
                    description = "Tự động ghi nhớ các văn bản bạn sao chép",
                    checked = historyEnabled,
                    onCheckedChange = { viewModel.setHistoryEnabled(it) },
                    testTag = "setting_history_switch"
                )

                // 30-min auto delete explanation
                SettingInfoRow(
                    icon = Icons.Default.Timer,
                    iconTint = AmberPin,
                    title = "Tự động dọn dẹp sau 30 phút",
                    description = "Clipboard chưa ghim sẽ tự động dọn sạch sau 30 phút. Các mục đã ghim được giữ lại vĩnh viễn."
                )

                // Vibration feedback
                SettingSwitchRow(
                    icon = Icons.Default.Vibration,
                    iconTint = CyanAccent,
                    title = "Rung khi dán",
                    description = "Rung phản hồi nhẹ khi chạm dán clipboard",
                    checked = vibrateOnPaste,
                    onCheckedChange = { viewModel.setVibrateOnPaste(it) },
                    testTag = "setting_vibrate_switch"
                )

                // Animations
                SettingSwitchRow(
                    icon = Icons.Default.Animation,
                    iconTint = CyanAccent,
                    title = "Hiệu ứng hoạt họa",
                    description = "Hiệu ứng mượt mà khi mở bảng và thao tác",
                    checked = enableAnimations,
                    onCheckedChange = { viewModel.setEnableAnimations(it) },
                    testTag = "setting_animation_switch"
                )
            }
        }

        // ==========================================
        // SECTION 3: BẢO MẬT & GIỚI THIỆU ỨNG DỤNG
        // ==========================================
        Text(
            text = "GIỚI THIỆU & BẢO MẬT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 1.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Privacy Guarantee
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GreenSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bảo mật & Quyền riêng tư 100% Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Smart Clipboard hoạt động hoàn toàn cục bộ trên thiết bị của bạn. Không sử dụng quyền Internet, không thu thập và không gửi bất kỳ dữ liệu nào ra máy chủ bên ngoài.",
                            fontSize = 12.sp,
                            color = Slate400,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Features summary
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyanAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Clipboard Manager v1.0.0",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hỗ trợ bàn phím IME giữ nguyên bảng sau khi dán nhiều lần, chế độ FIFO Queue tối đa 50 item dán tuần tự liên tục, tự động dọn dẹp sau 30 phút và lưu trữ cục bộ với Room Database.",
                            fontSize = 12.sp,
                            color = Slate400,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 4: THAO TÁC DỮ LIỆU
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Manual Cleanup Expired
                Button(
                    onClick = {
                        viewModel.cleanupExpired()
                        Toast.makeText(context, "Đã dọn dẹp các clipboard hết hạn", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("cleanup_now_button")
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp), tint = CyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Dọn dẹp clipboard hết hạn ngay", color = Color.White, fontSize = 13.sp)
                }

                // Clear All Unpinned
                OutlinedButton(
                    onClick = { showClearUnpinnedDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("clear_unpinned_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp), tint = RedDelete)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Xóa toàn bộ clipboard chưa ghim", color = RedDelete, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showClearUnpinnedDialog) {
        AlertDialog(
            onDismissRequest = { showClearUnpinnedDialog = false },
            title = { Text("Xác nhận xóa", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa tất cả các mục clipboard chưa ghim? Các mục đã ghim sẽ được giữ nguyên.", color = Slate400) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllUnpinned()
                        showClearUnpinnedDialog = false
                        Toast.makeText(context, "Đã xóa toàn bộ mục chưa ghim", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Xóa tất cả", color = RedDelete, fontWeight = FontWeight.Bold)
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
private fun SettingSwitchRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Slate800, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Slate400,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Slate950,
                checkedTrackColor = iconTint,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate800
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingInfoRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Slate800, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Slate400,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GuideStepRow(step: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(CyanAccent.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = CyanAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Slate300,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

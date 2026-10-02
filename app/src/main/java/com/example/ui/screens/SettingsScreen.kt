package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.Slate300
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
    val systemStatus by viewModel.systemStatus.collectAsState()
    val floatingBubbleEnabled by viewModel.floatingBubbleEnabled.collectAsState()
    val historyEnabled by viewModel.historyEnabled.collectAsState()

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
        // SECTION 3: CÀI ĐẶT CLIPBOARD
        // ==========================================
        Text(
            text = "CÀI ĐẶT CLIPBOARD",
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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

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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
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
    val historyEnabled by viewModel.historyEnabled.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()

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
            text = "Cài đặt & Hướng dẫn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Banner: Background Copy Interception Explanation
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (systemStatus.isAccessibilityEnabled) GreenSuccess.copy(alpha = 0.6f) else AmberPin.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (systemStatus.isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Accessibility,
                        contentDescription = null,
                        tint = if (systemStatus.isAccessibilityEnabled) GreenSuccess else AmberPin,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (systemStatus.isAccessibilityEnabled) "✓ Đang tự động lưu ngầm 100%" else "Tự động lưu khi Copy ở app khác (Android 10+)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = if (systemStatus.isAccessibilityEnabled)
                        "Dịch vụ Hỗ trợ tiếp cận đang bật. Mọi văn bản bạn Sao chép ở Zalo, Chrome, Facebook... sẽ tự động lưu ngầm vào ứng dụng ngay lập tức."
                    else
                        "Theo quy định bảo mật của Android (từ Android 10 trở lên), các app chạy ngầm không được tự ý đọc nội dung bạn copy ở app khác trừ khi bạn bật Hỗ trợ tiếp cận. Nếu bật, app sẽ tự động ghi nhớ mọi nội dung copy mà không cần mở app.",
                    fontSize = 12.sp,
                    color = Slate300,
                    lineHeight = 17.sp
                )

                if (!systemStatus.isAccessibilityEnabled) {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPin),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("enable_accessibility_btn")
                    ) {
                        Text(
                            text = "Bật Hỗ trợ tiếp cận để tự động lưu ngầm",
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 1: CÁC CÁCH SỬ DỤNG TIỆN LỢI
        // ==========================================
        Text(
            text = "CÁCH DÙNG TRONG MỌI ỨNG DỤNG",
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
                FeatureGuideRow(
                    step = "1",
                    title = "Ô Cài đặt nhanh (Quick Settings Tile)",
                    desc = "Vuốt từ trên cùng màn hình xuống, chạm ô '⚡ Dán FIFO': Ứng dụng sẽ nạp ngay mã tiếp theo vào bộ nhớ tạm và mở app để bạn dán ngay lập tức!"
                )

                FeatureGuideRow(
                    step = "2",
                    title = "Menu văn bản hệ thống (Android Process Text)",
                    desc = "Khi ở Zalo, Messenger, Chrome... chọn văn bản hoặc chạm giữ ô nhập. Menu hệ thống sẽ xuất hiện mục: '⚡ Dán FIFO tiếp theo', '📋 Chọn từ Clipboard' và '📥 Lưu vào Smart Clipboard'."
                )

                FeatureGuideRow(
                    step = "3",
                    title = "Tự động đồng bộ khi mở ứng dụng",
                    desc = "Bất cứ khi nào bạn vừa Copy và chuyển sang app Smart Clipboard, nội dung sao chép mới nhất sẽ được tự động nhận diện và đưa vào danh sách."
                )
            }
        }

        // ==========================================
        // SECTION 2: CÀI ĐẶT CLIPBOARD
        // ==========================================
        Text(
            text = "CÀI ĐẶT BỘ NHỚ TẠM",
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
private fun FeatureGuideRow(step: String, title: String, desc: String) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = Slate300,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
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

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("about_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📋", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Smart Clipboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "Phiên bản 1.0 (Native Android IME)",
                    fontSize = 12.sp,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Giải pháp Clipboard thông minh giúp tăng năng suất sao chép - dán trên thiết bị di động.",
                    fontSize = 13.sp,
                    color = Slate400,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Key Features Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Điểm nổi bật",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                FeaturePoint(
                    icon = Icons.Default.Bolt,
                    color = CyanAccent,
                    title = "Không đóng khi dán (Continuous Paste)",
                    desc = "Khi chạm dán một nội dung, bảng Clipboard vẫn mở để bạn có thể dán tiếp mục 2, mục 3 liên tục."
                )

                FeaturePoint(
                    icon = Icons.Default.Timer,
                    color = AmberPin,
                    title = "Tự động xóa sau 30 phút",
                    desc = "Clipboard chưa ghim sẽ tự động dọn dẹp sau 30 phút mà không làm rác bộ nhớ thiết bị."
                )

                FeaturePoint(
                    icon = Icons.Default.PushPin,
                    color = AmberPin,
                    title = "Ghim nội dung quan trọng",
                    desc = "Các mục đã ghim được lưu trữ lâu dài và không bị xóa bởi bộ đếm 30 phút."
                )

                FeaturePoint(
                    icon = Icons.Default.Check,
                    color = GreenSuccess,
                    title = "Chọn nhiều mục cùng lúc",
                    desc = "Hỗ trợ chọn hàng loạt để Ghim, Xóa hoặc Ghép nội dung bằng dòng mới."
                )
            }
        }

        // Security & Privacy Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bảo mật & Quyền riêng tư",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "• 100% Offline: Ứng dụng hoàn toàn KHÔNG sử dụng quyền INTERNET.",
                    fontSize = 13.sp,
                    color = Slate400
                )
                Text(
                    text = "• Không phân tích / không telemetry: Nội dung clipboard chỉ được lưu trong database SQLite mã nguồn mở Room cục bộ trên máy.",
                    fontSize = 13.sp,
                    color = Slate400
                )
                Text(
                    text = "• Lọc dữ liệu nhạy cảm: Tự động phát hiện cờ EXTRA_IS_SENSITIVE của Android (mật khẩu, mã OTP) để không lưu trữ.",
                    fontSize = 13.sp,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun FeaturePoint(
    icon: ImageVector,
    color: Color,
    title: String,
    desc: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
            Text(text = desc, fontSize = 12.sp, color = Slate400, lineHeight = 16.sp)
        }
    }
}

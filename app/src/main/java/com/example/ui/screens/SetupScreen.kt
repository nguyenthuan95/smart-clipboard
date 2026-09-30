package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ClipboardViewModel

@Composable
fun SetupScreen(
    viewModel: ClipboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imeStatus by viewModel.imeStatus.collectAsState()
    var testInputText by remember { mutableStateOf("") }

    // Auto refresh status when screen displays
    LaunchedEffect(Unit) {
        viewModel.refreshImeStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("setup_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bật bàn phím Smart Clipboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Để dán văn bản liên tục từ bất kỳ ứng dụng nào mà không bị đóng bảng clipboard, hãy kích hoạt bàn phím theo 2 bước bên dưới.",
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )
            }
        }

        // Step 1: Enable in Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (imeStatus.isEnabled) GreenSuccess.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            fontWeight = FontWeight.Bold,
                            color = if (imeStatus.isEnabled) GreenSuccess else CyanAccent,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Bật trong Cài đặt hệ thống",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (imeStatus.isEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (imeStatus.isEnabled) GreenSuccess else Slate700,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Vào Cài đặt bàn phím của thiết bị và gạt công tắc kích hoạt cho 'Bàn phím Smart Clipboard'.",
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (imeStatus.isEnabled) Slate800 else CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_ime_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (imeStatus.isEnabled) Color.White else Slate950,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (imeStatus.isEnabled) "Cài đặt bàn phím (Đã bật)" else "Mở cài đặt bàn phím",
                        color = if (imeStatus.isEnabled) Color.White else Slate950,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Step 2: Switch to Smart Clipboard
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (imeStatus.isSelected) GreenSuccess.copy(alpha = 0.2f) else AmberPin.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2",
                            fontWeight = FontWeight.Bold,
                            color = if (imeStatus.isSelected) GreenSuccess else AmberPin,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chọn làm bàn phím hoạt động",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (imeStatus.isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (imeStatus.isSelected) GreenSuccess else Slate700,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Mở bảng chọn bàn phím và chọn 'Bàn phím Smart Clipboard' để sử dụng.",
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        @Suppress("DEPRECATION")
                        imm?.showInputMethodPicker()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (imeStatus.isSelected) Slate800 else AmberPin),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("select_ime_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = if (imeStatus.isSelected) Color.White else Slate950,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (imeStatus.isSelected) "Đang chọn Smart Clipboard ✓" else "Chọn bàn phím nhập liệu",
                        color = if (imeStatus.isSelected) Color.White else Slate950,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Step 3: Accessibility Service for cross-app queue pasting
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(CyanAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hỗ trợ tiếp cận (Tùy chọn cho Hàng đợi)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Cho phép Smart Clipboard tự động tìm ô nhập văn bản và dán item tiếp theo trong Hàng đợi FIFO vào bất kỳ app nào (Zalo, Chrome, app giao hàng...).",
                    fontSize = 13.sp,
                    color = Slate400,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_accessibility_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cài đặt Hỗ trợ tiếp cận (Accessibility)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Test Field
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kiểm tra bàn phím tại đây:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Chạm vào ô nhập bên dưới để bàn phím hiện lên. Nhấn nút [ Clipboard ] trên bàn phím để mở bảng dán liên tục!",
                    fontSize = 12.sp,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    placeholder = { Text("Chạm để mở bàn phím Smart Clipboard...", fontSize = 13.sp, color = Slate400) },
                    modifier = Modifier.fillMaxWidth().testTag("setup_test_field"),
                    minLines = 2,
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
    }
}

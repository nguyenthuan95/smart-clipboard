package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ClipboardScreen
import com.example.ui.screens.LogScreen
import com.example.ui.theme.AmberPin
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate950
import com.example.ui.theme.SmartClipboardTheme
import com.example.ui.viewmodel.ClipboardViewModel

enum class AppTab {
    QUEUE,
    LOGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: ClipboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModePref by viewModel.darkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (darkModePref) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            SmartClipboardTheme(darkTheme = isDark) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemStatus()
        viewModel.syncFromSystemClipboard()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            viewModel.syncFromSystemClipboard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: ClipboardViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.QUEUE) }

    // If on logs tab, pressing back returns to Queue tab
    BackHandler(enabled = currentTab != AppTab.QUEUE) {
        currentTab = AppTab.QUEUE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.QUEUE -> "Smart Clipboard (Hàng Đợi FIFO)"
                            AppTab.LOGS -> "Nhật Ký Lỗi & Chẩn Đoán"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate950,
                tonalElevation = 4.dp
            ) {
                // Tab 1: FIFO Queue (Danh sách chờ dán)
                NavigationBarItem(
                    selected = currentTab == AppTab.QUEUE,
                    onClick = { currentTab = AppTab.QUEUE },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.QUEUE) Icons.Filled.FormatListNumbered else Icons.Outlined.FormatListNumbered,
                            contentDescription = "Hàng đợi",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Hàng đợi FIFO", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = AmberPin,
                        indicatorColor = AmberPin,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_queue")
                )

                // Tab 2: Logs (Nhật ký lỗi)
                NavigationBarItem(
                    selected = currentTab == AppTab.LOGS,
                    onClick = { currentTab = AppTab.LOGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.LOGS) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                            contentDescription = "Nhật ký lỗi",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Nhật ký lỗi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_logs")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.QUEUE -> ClipboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.LOGS -> LogScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

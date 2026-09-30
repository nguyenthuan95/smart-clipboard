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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ClipboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SmartClipboardTheme
import com.example.ui.viewmodel.ClipboardViewModel

enum class AppTab {
    CLIPBOARD,
    SETUP,
    SETTINGS,
    ABOUT
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
        viewModel.refreshImeStatus()
        viewModel.syncFromSystemClipboard()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: ClipboardViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.CLIPBOARD) }

    // If on a secondary tab, pressing back returns to Clipboard tab
    BackHandler(enabled = currentTab != AppTab.CLIPBOARD) {
        currentTab = AppTab.CLIPBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.CLIPBOARD -> stringResource(R.string.app_name)
                            AppTab.SETUP -> stringResource(R.string.tab_setup)
                            AppTab.SETTINGS -> stringResource(R.string.tab_settings)
                            AppTab.ABOUT -> stringResource(R.string.tab_about)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
                // Clipboard Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.CLIPBOARD,
                    onClick = { currentTab = AppTab.CLIPBOARD },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CLIPBOARD) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                            contentDescription = stringResource(R.string.tab_clipboard),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_clipboard), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_clipboard")
                )

                // Setup / Enable Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SETUP,
                    onClick = { currentTab = AppTab.SETUP },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SETUP) Icons.Filled.Keyboard else Icons.Outlined.Keyboard,
                            contentDescription = stringResource(R.string.tab_setup),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_setup), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_setup")
                )

                // Settings Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { currentTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.tab_settings),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )

                // About Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.ABOUT,
                    onClick = { currentTab = AppTab.ABOUT },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.tab_about),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.tab_about), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_about")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.CLIPBOARD -> ClipboardScreen(
                viewModel = viewModel,
                onNavigateToSetup = { currentTab = AppTab.SETUP },
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.SETUP -> SetupScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.ABOUT -> AboutScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

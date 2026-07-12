package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.MainViewModel

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "downloads"
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        text = when (currentRoute) {
                            "downloads" -> "Aria Manager"
                            "browser" -> "Web Browser"
                            "settings" -> "Settings"
                            else -> "Aria"
                        },
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ) 
                },
                actions = {
                    if (currentRoute == "downloads") {
                        IconButton(onClick = { /* TODO: Sort */ }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        IconButton(onClick = {
                            val text = clipboardManager.getText()?.text ?: ""
                            viewModel.openAddDialog(text)
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Add from Clipboard")
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloads") },
                    label = { Text("Downloads") },
                    selected = currentRoute == "downloads",
                    alwaysShowLabel = false,
                    onClick = {
                        navController.navigate("downloads") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Language, contentDescription = "Browser") },
                    label = { Text("Browser") },
                    selected = currentRoute == "browser",
                    alwaysShowLabel = false,
                    onClick = {
                        navController.navigate("browser") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    alwaysShowLabel = false,
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "downloads",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("downloads") { DownloadsScreen(viewModel) }
            composable("browser") { BrowserScreen() }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }
}

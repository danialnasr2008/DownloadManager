package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val themeMode by viewModel.themeMode.collectAsState()
    var multiPart by remember { mutableStateOf(true) }
    var speedLimit by remember { mutableStateOf(0f) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelection(themeMode = themeMode, onThemeChange = { viewModel.setThemeMode(it) })
        }

        item {
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Downloads & Network",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingToggleItem(
                icon = Icons.Default.ViewAgenda,
                title = "Multi-part Downloading",
                subtitle = "Use intelligent segmentation (Aria engine)",
                checked = multiPart,
                onCheckedChange = { multiPart = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Speed Limit: ${if (speedLimit == 0f) "Unlimited" else "${speedLimit.toInt()} MB/s"}")
                    Slider(
                        value = speedLimit,
                        onValueChange = { speedLimit = it },
                        valueRange = 0f..50f
                    )
                }
            }
        }

        item {
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Advanced Download Features",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            var wifiOnly by remember { mutableStateOf(false) }
            SettingToggleItem(
                icon = Icons.Default.Wifi,
                title = "Wi-Fi Only",
                subtitle = "Only download when connected to Wi-Fi",
                checked = wifiOnly,
                onCheckedChange = { wifiOnly = it }
            )
            
            var autoResume by remember { mutableStateOf(true) }
            SettingToggleItem(
                icon = Icons.Default.Autorenew,
                title = "Auto-Resume",
                subtitle = "Automatically resume interrupted downloads",
                checked = autoResume,
                onCheckedChange = { autoResume = it }
            )
            
            var autoCategorize by remember { mutableStateOf(true) }
            SettingToggleItem(
                icon = Icons.Default.Category,
                title = "Auto-Categorize",
                subtitle = "Organize files by type (Video, Audio, Docs)",
                checked = autoCategorize,
                onCheckedChange = { autoCategorize = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            var concurrentDownloads by remember { mutableStateOf(3f) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Concurrent Downloads: ${concurrentDownloads.toInt()}")
                    Slider(
                        value = concurrentDownloads,
                        onValueChange = { concurrentDownloads = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            var threadsPerDownload by remember { mutableStateOf(16f) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CallSplit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Threads per Download: ${threadsPerDownload.toInt()}")
                    Slider(
                        value = threadsPerDownload,
                        onValueChange = { threadsPerDownload = it },
                        valueRange = 1f..32f,
                        steps = 30
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSelection(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Column {
        ThemeOption(text = "System Default", selected = themeMode == ThemeMode.SYSTEM, onClick = { onThemeChange(ThemeMode.SYSTEM) })
        ThemeOption(text = "Light Theme", selected = themeMode == ThemeMode.LIGHT, onClick = { onThemeChange(ThemeMode.LIGHT) })
        ThemeOption(text = "Dark Theme", selected = themeMode == ThemeMode.DARK, onClick = { onThemeChange(ThemeMode.DARK) })
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

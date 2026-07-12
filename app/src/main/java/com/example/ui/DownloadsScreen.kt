package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DownloadStatus
import com.example.model.DownloadTask
import com.example.viewmodel.MainViewModel

@Composable
fun DownloadsScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val tasks by viewModel.tasks.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val clipboardUrl by viewModel.clipboardUrl.collectAsState()
    val tabs = listOf("All", "Active", "Completed")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            divider = {},
            indicator = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedTabIndex == index
                FilterChip(
                    selected = selected,
                    onClick = { selectedTabIndex = index },
                    label = { 
                        Text(
                            text = title, 
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal 
                        ) 
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    border = null
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val filteredTasks = when (selectedTabIndex) {
                1 -> tasks.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
                2 -> tasks.filter { it.status == DownloadStatus.COMPLETED }
                else -> tasks
            }

            if (filteredTasks.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        DownloadCard(
                            task = task,
                            onToggleStatus = { viewModel.toggleStatus(task) },
                            onTogglePriority = { viewModel.togglePriority(task.id) },
                            onDelete = { viewModel.removeTask(task) }
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog("") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Download") },
                text = { Text("New Download", fontWeight = FontWeight.Bold) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
        }
    }

    if (showAddDialog) {
        AddDownloadDialog(
            viewModel = viewModel,
            initialUrl = clipboardUrl,
            onDismiss = { viewModel.closeAddDialog() },
            onAdd = { url, name, size, schedule ->
                viewModel.addDownload(url, name, size, schedule)
                viewModel.closeAddDialog()
            }
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active downloads",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Tap + to add a new task",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun DownloadCard(
    task: DownloadTask,
    onToggleStatus: () -> Unit,
    onTogglePriority: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = if (task.status == DownloadStatus.COMPLETED) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer, 
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = if (task.status == DownloadStatus.COMPLETED) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.fileName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (task.status == DownloadStatus.DOWNLOADING) {
                            "Downloading • ${task.speed} • ETA 2m"
                        } else {
                            task.status.name
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (task.status) {
                            DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                            DownloadStatus.ERROR -> MaterialTheme.colorScheme.error
                            DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                IconButton(onClick = onTogglePriority) {
                    Icon(
                        imageVector = if (task.isPriority) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Priority",
                        tint = if (task.isPriority) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulating multipart progress bars
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 0 until 8) {
                    val partProgress = if (task.progress > (i / 8f)) 1f else if (task.progress > ((i-1)/8f)) (task.progress * 8) % 1f else 0f
                    LinearProgressIndicator(
                        progress = { partProgress },
                        modifier = Modifier.weight(1f).height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatSize(task.downloadedSize)} / ${formatSize(task.totalSize)} • ${(task.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (task.status != DownloadStatus.COMPLETED) {
                        FilledIconButton(
                            onClick = onToggleStatus,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (task.status == DownloadStatus.DOWNLOADING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    FilledIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddDownloadDialog(viewModel: MainViewModel, initialUrl: String, onDismiss: () -> Unit, onAdd: (String, String, Long, Long?) -> Unit) {
    var url by remember { mutableStateOf(initialUrl) }
    var name by remember { mutableStateOf("") }
    var totalSize by remember { mutableStateOf(0L) }
    var isFetching by remember { mutableStateOf(false) }
    var scheduledTimeMs by remember { mutableStateOf<Long?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    val isBatch = url.lines().count { it.isNotBlank() } > 1

    LaunchedEffect(url) {
        val firstUrl = url.lines().firstOrNull { it.isNotBlank() }
        if (firstUrl != null && firstUrl.startsWith("http") && !isBatch) {
            isFetching = true
            val details = viewModel.fetchUrlDetails(firstUrl)
            if (name.isEmpty() || name == "downloaded_file") {
                name = details.first
            }
            totalSize = details.second
            isFetching = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBatch) "Batch Download" else "New Download") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (One per line for batch)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    minLines = 3,
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!isBatch) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("File Name") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (isFetching) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (totalSize > 0) {
                        Text("Size: ${formatSize(totalSize)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text("File names will be automatically detected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (scheduledTimeMs == null) "Start immediately" else "Scheduled to start later")
                    Switch(
                        checked = scheduledTimeMs != null,
                        onCheckedChange = { if (it) showTimePicker = true else scheduledTimeMs = null }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isBatch) {
                        url.lines().filter { it.isNotBlank() }.forEach { singleUrl ->
                            onAdd(singleUrl, "", 0L, scheduledTimeMs)
                        }
                    } else {
                        onAdd(url, name, totalSize, scheduledTimeMs)
                    }
                },
                enabled = url.isNotBlank() && (isBatch || name.isNotBlank()) && !isFetching
            ) {
                Text(if (isBatch) "Start All" else "Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showTimePicker) {
        // Mock time picker logic for scheduling
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Schedule Download") },
            text = { Text("Scheduling feature is in development. Selecting a time 1 hour from now.") },
            confirmButton = {
                Button(onClick = { 
                    scheduledTimeMs = System.currentTimeMillis() + 3600000 
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            }
        )
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

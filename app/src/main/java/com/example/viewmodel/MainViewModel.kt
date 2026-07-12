package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DownloadDatabase
import com.example.data.DownloadRepository
import com.example.model.DownloadStatus
import com.example.model.DownloadTask
import com.example.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DownloadRepository
    private val prefs = application.getSharedPreferences("aria_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries[prefs.getInt("theme_mode", ThemeMode.SYSTEM.ordinal)]
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _clipboardUrl = MutableStateFlow("")
    val clipboardUrl: StateFlow<String> = _clipboardUrl.asStateFlow()

    fun openAddDialog(url: String = "") {
        _clipboardUrl.value = url
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
        _clipboardUrl.value = ""
    }

    init {
        val dao = DownloadDatabase.getDatabase(application).downloadDao()
        repository = DownloadRepository(dao)
    }

    val tasks: StateFlow<List<DownloadTask>> = repository.allTasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putInt("theme_mode", mode.ordinal).apply()
        _themeMode.value = mode
    }

    suspend fun fetchUrlDetails(urlString: String): Pair<String, Long> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            val contentLength = connection.contentLengthLong
            val disposition = connection.getHeaderField("Content-Disposition")
            
            var fileName = ""
            if (disposition != null && disposition.indexOf("filename=") > 0) {
                val idx = disposition.indexOf("filename=") + 10
                fileName = disposition.substring(idx, disposition.length - 1).replace("\"", "")
            }
            if (fileName.isBlank()) {
                fileName = urlString.substringAfterLast("/")
                if (fileName.contains("?")) fileName = fileName.substringBefore("?")
            }
            if (fileName.isBlank()) fileName = "downloaded_file"

            connection.disconnect()
            Pair(fileName, contentLength)
        } catch (e: Exception) {
            Pair("", 0L)
        }
    }

    fun addDownload(url: String, fileName: String, totalSize: Long, scheduledTimeMs: Long?, isPriority: Boolean = false) {
        viewModelScope.launch {
            val finalName = if (fileName.isBlank()) {
                val segment = url.substringAfterLast("/")
                if (segment.isNotBlank() && !segment.contains("?")) segment else "download_${System.currentTimeMillis()}"
            } else fileName
            repository.addDownload(url, finalName, isPriority, totalSize, scheduledTimeMs)
        }
    }

    fun togglePriority(taskId: Long) {
        viewModelScope.launch {
            repository.togglePriority(taskId)
        }
    }

    fun removeTask(task: DownloadTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
    
    fun toggleStatus(task: DownloadTask) {
        viewModelScope.launch {
            val newStatus = when (task.status) {
                DownloadStatus.DOWNLOADING -> DownloadStatus.PAUSED
                DownloadStatus.PAUSED, DownloadStatus.QUEUED, DownloadStatus.ERROR -> DownloadStatus.DOWNLOADING
                else -> task.status
            }
            repository.updateTaskStatus(task.id, newStatus)
        }
    }
}

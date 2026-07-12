package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val fileName: String,
    val progress: Float = 0f,
    val totalSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val speed: String = "0 KB/s",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val isPriority: Boolean = false,
    val scheduledTimeMs: Long? = null
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, ERROR
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

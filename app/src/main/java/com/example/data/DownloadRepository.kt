package com.example.data

import com.example.model.DownloadStatus
import com.example.model.DownloadTask
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {

    val allTasks: Flow<List<DownloadTask>> = downloadDao.getAllTasks()

    suspend fun addDownload(url: String, fileName: String, isPriority: Boolean, totalSize: Long = 0L, scheduledTimeMs: Long? = null) {
        val task = DownloadTask(
            url = url,
            fileName = fileName,
            isPriority = isPriority,
            totalSize = totalSize,
            scheduledTimeMs = scheduledTimeMs
        )
        downloadDao.insertTask(task)
    }

    suspend fun updateTaskStatus(taskId: Long, status: DownloadStatus) {
        val task = downloadDao.getTaskById(taskId)
        task?.let {
            downloadDao.updateTask(it.copy(status = status))
        }
    }

    suspend fun deleteTask(task: DownloadTask) {
        downloadDao.deleteTask(task)
    }

    suspend fun togglePriority(taskId: Long) {
        val task = downloadDao.getTaskById(taskId)
        task?.let {
            downloadDao.updateTask(it.copy(isPriority = !it.isPriority))
        }
    }
}

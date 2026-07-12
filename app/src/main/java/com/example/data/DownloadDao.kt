package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download_tasks ORDER BY isPriority DESC, id DESC")
    fun getAllTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): DownloadTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTask): Long

    @Update
    suspend fun updateTask(task: DownloadTask)

    @Delete
    suspend fun deleteTask(task: DownloadTask)
}

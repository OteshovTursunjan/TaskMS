package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.SprintConfigEntity
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SprintDao {

    @Query("SELECT * FROM sprint_tasks ORDER BY updatedAtMillis DESC")
    fun observeAllTasks(): Flow<List<SprintTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: SprintTaskEntity): Long

    @Update
    suspend fun updateTask(task: SprintTaskEntity)

    @Query("UPDATE sprint_tasks SET status = :status, updatedAtMillis = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAt: Long)

    @Query("UPDATE sprint_tasks SET pomodorosCompleted = pomodorosCompleted + 1, updatedAtMillis = :updatedAt WHERE id = :taskId")
    suspend fun incrementTaskPomodoro(taskId: Long, updatedAt: Long)

    @Query("DELETE FROM sprint_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("SELECT * FROM sprint_config WHERE id = 1")
    fun observeSprintConfig(): Flow<SprintConfigEntity?>

    @Query("SELECT * FROM sprint_config WHERE id = 1")
    suspend fun getSprintConfigOnce(): SprintConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSprintConfig(config: SprintConfigEntity)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAtMillis DESC LIMIT 30")
    fun observeRecentSessions(): Flow<List<PomodoroSessionEntity>>

    @Insert
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity)
}

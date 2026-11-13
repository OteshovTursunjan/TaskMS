package com.example.data.repository

import com.example.data.local.SprintDao
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.SprintConfigEntity
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskStatus
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow

class SprintRepository(private val dao: SprintDao) {

    val allTasks: Flow<List<SprintTaskEntity>> = dao.observeAllTasks()
    val sprintConfig: Flow<SprintConfigEntity?> = dao.observeSprintConfig()
    val recentSessions: Flow<List<PomodoroSessionEntity>> = dao.observeRecentSessions()

    suspend fun ensureDefaultSprintInitialized(defaultName: String, defaultGoal: String) {
        if (dao.getSprintConfigOnce() == null) {
            val start = DateUtils.startOfLocalDayMillis()
            val end = DateUtils.addDays(start, 13) // 2-week sprint inclusive (14 days)
            dao.upsertSprintConfig(
                SprintConfigEntity(
                    id = 1,
                    sprintName = defaultName,
                    sprintGoal = defaultGoal,
                    durationWeeks = 2,
                    startDateMillis = start,
                    endDateMillis = end
                )
            )
        }
    }

    suspend fun saveSprintConfig(
        name: String,
        goal: String,
        durationWeeks: Int,
        startDateMillis: Long
    ) {
        val normalizedStart = DateUtils.startOfLocalDayMillis(startDateMillis)
        val totalDays = (durationWeeks.coerceIn(1, 2) * 7) - 1
        val end = DateUtils.addDays(normalizedStart, totalDays)
        dao.upsertSprintConfig(
            SprintConfigEntity(
                id = 1,
                sprintName = name.trim().ifEmpty { "Sprint #1" },
                sprintGoal = goal.trim(),
                durationWeeks = durationWeeks.coerceIn(1, 2),
                startDateMillis = normalizedStart,
                endDateMillis = end
            )
        )
    }

    suspend fun saveTask(task: SprintTaskEntity): Long {
        val now = System.currentTimeMillis()
        return if (task.id == 0L) {
            dao.insertTask(task.copy(createdAtMillis = now, updatedAtMillis = now))
        } else {
            dao.updateTask(task.copy(updatedAtMillis = now))
            task.id
        }
    }

    suspend fun moveTaskToStatus(taskId: Long, status: TaskStatus) {
        dao.updateTaskStatus(taskId = taskId, status = status, updatedAt = System.currentTimeMillis())
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskById(taskId)
    }

    suspend fun recordCompletedPomodoro(taskId: Long?, taskTitle: String, durationMinutes: Int = 25) {
        val now = System.currentTimeMillis()
        if (taskId != null) {
            dao.incrementTaskPomodoro(taskId = taskId, updatedAt = now)
        }
        dao.insertPomodoroSession(
            PomodoroSessionEntity(
                taskId = taskId,
                taskTitle = taskTitle,
                durationMinutes = durationMinutes,
                completedAtMillis = now
            )
        )
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sprint_tasks")
data class SprintTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.FEATURE,
    val storyPoints: Int = 2,
    val deadlineMillis: Long? = null,
    val pomodorosCompleted: Int = 0,
    val estimatedPomodoros: Int = 2,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "sprint_config")
data class SprintConfigEntity(
    @PrimaryKey val id: Int = 1,
    val sprintName: String,
    val sprintGoal: String,
    val durationWeeks: Int,
    val startDateMillis: Long,
    val endDateMillis: Long
)

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val taskId: Long?,
    val taskTitle: String,
    val durationMinutes: Int = 25,
    val completedAtMillis: Long = System.currentTimeMillis()
)

package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus

class SprintConverters {
    @TypeConverter
    fun fromStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus =
        runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.TODO)

    @TypeConverter
    fun fromPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority =
        runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.MEDIUM)

    @TypeConverter
    fun fromCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory =
        runCatching { TaskCategory.valueOf(value) }.getOrDefault(TaskCategory.FEATURE)
}

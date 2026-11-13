package com.example.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.PomodoroSessionEntity
import com.example.data.model.SprintConfigEntity
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.repository.SprintRepository
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppDestination {
    KANBAN,
    FOCUS
}

enum class ColumnFilter {
    ALL,
    TODO,
    IN_PROGRESS,
    DONE;

    fun matches(status: TaskStatus): Boolean = when (this) {
        ALL -> true
        TODO -> status == TaskStatus.TODO
        IN_PROGRESS -> status == TaskStatus.IN_PROGRESS
        DONE -> status == TaskStatus.DONE
    }
}

enum class BoardViewMode {
    COLUMNS,
    LIST
}

enum class PomodoroPhase(val durationSeconds: Int, val durationMinutes: Int) {
    FOCUS(25 * 60, 25),
    SHORT_BREAK(5 * 60, 5)
}

data class PomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingSeconds: Int = PomodoroPhase.FOCUS.durationSeconds,
    val totalSeconds: Int = PomodoroPhase.FOCUS.durationSeconds,
    val isRunning: Boolean = false,
    val selectedTaskId: Long? = null,
    val completionMessagePhase: PomodoroPhase? = null,
    val completionNonce: Long = 0L
) {
    val progress: Float
        get() = if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
}

data class SprintUiState(
    val destination: AppDestination = AppDestination.KANBAN,
    val sprintConfig: SprintConfigEntity? = null,
    val allTasks: List<SprintTaskEntity> = emptyList(),
    val filteredTasks: List<SprintTaskEntity> = emptyList(),
    val columnFilter: ColumnFilter = ColumnFilter.ALL,
    val searchQuery: String = "",
    val boardViewMode: BoardViewMode = BoardViewMode.COLUMNS,
    val pomodoro: PomodoroState = PomodoroState(),
    val recentSessions: List<PomodoroSessionEntity> = emptyList(),
    val todayFocusCycles: Int = 0
) {
    val todoCount: Int get() = allTasks.count { it.status == TaskStatus.TODO }
    val inProgressCount: Int get() = allTasks.count { it.status == TaskStatus.IN_PROGRESS }
    val doneCount: Int get() = allTasks.count { it.status == TaskStatus.DONE }
    val totalPomodoros: Int get() = allTasks.sumOf { it.pomodorosCompleted }
    val selectedFocusTask: SprintTaskEntity?
        get() = pomodoro.selectedTaskId?.let { id -> allTasks.firstOrNull { it.id == id } }
}

private data class FilterAndNavState(
    val destination: AppDestination = AppDestination.KANBAN,
    val columnFilter: ColumnFilter = ColumnFilter.ALL,
    val searchQuery: String = "",
    val boardViewMode: BoardViewMode = BoardViewMode.COLUMNS
)

class SprintViewModel(
    private val repository: SprintRepository,
    defaultSprintName: String,
    defaultSprintGoal: String
) : ViewModel() {

    private val filterNavState = MutableStateFlow(FilterAndNavState())
    private val pomodoroState = MutableStateFlow(PomodoroState())
    private var timerJob: Job? = null

    val uiState: StateFlow<SprintUiState> = combine(
        repository.sprintConfig,
        repository.allTasks,
        repository.recentSessions,
        filterNavState,
        pomodoroState
    ) { config, tasks, sessions, nav, pomo ->
        val query = nav.searchQuery.trim().lowercase()
        val filtered = tasks.filter { task ->
            val matchesCol = nav.columnFilter.matches(task.status)
            val matchesQuery = query.isEmpty() ||
                task.title.lowercase().contains(query) ||
                task.description.lowercase().contains(query) ||
                task.category.name.lowercase().contains(query)
            matchesCol && matchesQuery
        }.sortedWith(
            compareBy<SprintTaskEntity> { it.status.order }
                .thenByDescending { it.priority.weight }
                .thenBy { it.deadlineMillis ?: Long.MAX_VALUE }
        )

        val validSelectedTaskId = pomo.selectedTaskId?.takeIf { id -> tasks.any { it.id == id } }
        val normalizedPomo = if (validSelectedTaskId != pomo.selectedTaskId) {
            pomo.copy(selectedTaskId = null)
        } else {
            pomo
        }

        val todayCount = sessions.count { DateUtils.isToday(it.completedAtMillis) }

        SprintUiState(
            destination = nav.destination,
            sprintConfig = config,
            allTasks = tasks,
            filteredTasks = filtered,
            columnFilter = nav.columnFilter,
            searchQuery = nav.searchQuery,
            boardViewMode = nav.boardViewMode,
            pomodoro = normalizedPomo,
            recentSessions = sessions,
            todayFocusCycles = todayCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SprintUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultSprintInitialized(defaultSprintName, defaultSprintGoal)
        }
    }

    fun selectDestination(destination: AppDestination) {
        filterNavState.update { it.copy(destination = destination) }
    }

    fun selectColumnFilter(filter: ColumnFilter) {
        filterNavState.update { it.copy(columnFilter = filter) }
    }

    fun updateSearchQuery(query: String) {
        filterNavState.update { it.copy(searchQuery = query) }
    }

    fun toggleBoardViewMode() {
        filterNavState.update {
            it.copy(
                boardViewMode = if (it.boardViewMode == BoardViewMode.COLUMNS) {
                    BoardViewMode.LIST
                } else {
                    BoardViewMode.COLUMNS
                }
            )
        }
    }

    fun saveTask(
        existingTask: SprintTaskEntity?,
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        category: TaskCategory,
        storyPoints: Int,
        deadlineMillis: Long?,
        estimatedPomodoros: Int
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            val target = existingTask?.copy(
                title = cleanTitle,
                description = description.trim(),
                status = status,
                priority = priority,
                category = category,
                storyPoints = storyPoints.coerceIn(1, 13),
                deadlineMillis = deadlineMillis,
                estimatedPomodoros = estimatedPomodoros.coerceIn(1, 12)
            ) ?: SprintTaskEntity(
                title = cleanTitle,
                description = description.trim(),
                status = status,
                priority = priority,
                category = category,
                storyPoints = storyPoints.coerceIn(1, 13),
                deadlineMillis = deadlineMillis,
                estimatedPomodoros = estimatedPomodoros.coerceIn(1, 12)
            )
            repository.saveTask(target)
        }
    }

    fun moveTask(taskId: Long, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.moveTaskToStatus(taskId, newStatus)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            if (pomodoroState.value.selectedTaskId == taskId) {
                pauseTimer()
                pomodoroState.update { it.copy(selectedTaskId = null) }
            }
            repository.deleteTask(taskId)
        }
    }

    fun saveSprintConfig(name: String, goal: String, durationWeeks: Int, startDateMillis: Long) {
        viewModelScope.launch {
            repository.saveSprintConfig(name, goal, durationWeeks, startDateMillis)
        }
    }

    fun selectTaskForFocus(taskId: Long?) {
        pomodoroState.update {
            it.copy(selectedTaskId = taskId, completionMessagePhase = null)
        }
    }

    fun startFocusForTask(task: SprintTaskEntity) {
        val currentPomo = pomodoroState.value
        val switchingTask = currentPomo.selectedTaskId != task.id
        if (switchingTask || currentPomo.phase != PomodoroPhase.FOCUS) {
            pauseTimer()
            pomodoroState.update {
                it.copy(
                    phase = PomodoroPhase.FOCUS,
                    remainingSeconds = PomodoroPhase.FOCUS.durationSeconds,
                    totalSeconds = PomodoroPhase.FOCUS.durationSeconds,
                    isRunning = false,
                    selectedTaskId = task.id,
                    completionMessagePhase = null
                )
            }
        }
        if (task.status == TaskStatus.TODO) {
            moveTask(task.id, TaskStatus.IN_PROGRESS)
        }
        selectDestination(AppDestination.FOCUS)
        startTimer()
    }

    fun switchPomodoroPhase(phase: PomodoroPhase) {
        pauseTimer()
        pomodoroState.update {
            it.copy(
                phase = phase,
                remainingSeconds = phase.durationSeconds,
                totalSeconds = phase.durationSeconds,
                isRunning = false,
                completionMessagePhase = null
            )
        }
    }

    fun toggleTimer() {
        if (pomodoroState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (pomodoroState.value.isRunning) return
        val current = pomodoroState.value
        val startRemaining = if (current.remainingSeconds <= 0) {
            current.phase.durationSeconds
        } else {
            current.remainingSeconds
        }
        pomodoroState.update {
            it.copy(
                remainingSeconds = startRemaining,
                isRunning = true,
                completionMessagePhase = null
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val endElapsedMillis = SystemClock.elapsedRealtime() + startRemaining * 1000L
            while (true) {
                val leftMs = endElapsedMillis - SystemClock.elapsedRealtime()
                val leftSec = ((leftMs + 999L) / 1000L).toInt().coerceAtLeast(0)
                pomodoroState.update { it.copy(remainingSeconds = leftSec) }
                if (leftSec <= 0) {
                    onPhaseFinished()
                    break
                }
                delay(250L)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        pomodoroState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        pomodoroState.update {
            it.copy(
                remainingSeconds = it.phase.durationSeconds,
                totalSeconds = it.phase.durationSeconds,
                isRunning = false,
                completionMessagePhase = null
            )
        }
    }

    fun skipPhase() {
        val nextPhase = if (pomodoroState.value.phase == PomodoroPhase.FOCUS) {
            PomodoroPhase.SHORT_BREAK
        } else {
            PomodoroPhase.FOCUS
        }
        switchPomodoroPhase(nextPhase)
    }

    fun dismissCompletionBanner() {
        pomodoroState.update { it.copy(completionMessagePhase = null) }
    }

    private fun onPhaseFinished() {
        timerJob = null
        val finishedState = pomodoroState.value
        val finishedPhase = finishedState.phase
        val nextPhase = if (finishedPhase == PomodoroPhase.FOCUS) {
            PomodoroPhase.SHORT_BREAK
        } else {
            PomodoroPhase.FOCUS
        }

        if (finishedPhase == PomodoroPhase.FOCUS) {
            val task = uiState.value.allTasks.firstOrNull { it.id == finishedState.selectedTaskId }
            val titleSnapshot = task?.title ?: "Свободная сессия"
            viewModelScope.launch {
                repository.recordCompletedPomodoro(
                    taskId = task?.id,
                    taskTitle = titleSnapshot,
                    durationMinutes = PomodoroPhase.FOCUS.durationMinutes
                )
            }
        }

        pomodoroState.update {
            it.copy(
                phase = nextPhase,
                remainingSeconds = nextPhase.durationSeconds,
                totalSeconds = nextPhase.durationSeconds,
                isRunning = false,
                completionMessagePhase = finishedPhase,
                completionNonce = it.completionNonce + 1L
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: SprintRepository,
        private val defaultSprintName: String,
        private val defaultSprintGoal: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SprintViewModel(repository, defaultSprintName, defaultSprintGoal) as T
        }
    }
}

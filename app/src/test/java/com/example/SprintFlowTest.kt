package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SprintDatabase
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.repository.SprintRepository
import com.example.ui.viewmodel.ColumnFilter
import com.example.ui.viewmodel.PomodoroPhase
import com.example.ui.viewmodel.SprintViewModel
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SprintFlowTest {

    private lateinit var database: SprintDatabase
    private lateinit var repository: SprintRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SprintDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SprintRepository(database.sprintDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun repository_crudAndPomodoroIncrement_worksCorrectly() = runBlocking {
        repository.ensureDefaultSprintInitialized("Sprint #1", "Release v1.0")
        val config = repository.sprintConfig.first()
        assertNotNull(config)
        assertEquals(2, config?.durationWeeks)

        val deadline = DateUtils.addDays(DateUtils.startOfLocalDayMillis(), 3)
        val taskId = repository.saveTask(
            SprintTaskEntity(
                title = "Implement OAuth2 Flow",
                description = "Use PKCE and token refresh",
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                category = TaskCategory.FEATURE,
                deadlineMillis = deadline,
                estimatedPomodoros = 3
            )
        )

        var tasks = repository.allTasks.first()
        assertEquals(1, tasks.size)
        assertEquals(TaskStatus.TODO, tasks.first().status)
        assertEquals(deadline, tasks.first().deadlineMillis)

        repository.moveTaskToStatus(taskId, TaskStatus.IN_PROGRESS)
        tasks = repository.allTasks.first()
        assertEquals(TaskStatus.IN_PROGRESS, tasks.first().status)

        repository.recordCompletedPomodoro(taskId, "Implement OAuth2 Flow", 25)
        tasks = repository.allTasks.first()
        assertEquals(1, tasks.first().pomodorosCompleted)

        val sessions = repository.recentSessions.first()
        assertEquals(1, sessions.size)
        assertEquals(25, sessions.first().durationMinutes)
    }

    @Test
    fun columnFilter_andPomodoroPhaseSwitching_workCorrectly() {
        val vm = SprintViewModel(repository, "Sprint #1", "Goal")

        assertTrue(ColumnFilter.TODO.matches(TaskStatus.TODO))
        assertTrue(!ColumnFilter.TODO.matches(TaskStatus.IN_PROGRESS))
        assertTrue(ColumnFilter.IN_PROGRESS.matches(TaskStatus.IN_PROGRESS))
        assertTrue(ColumnFilter.DONE.matches(TaskStatus.DONE))

        assertEquals(PomodoroPhase.FOCUS, vm.uiState.value.pomodoro.phase)
        assertEquals(25 * 60, vm.uiState.value.pomodoro.remainingSeconds)

        vm.switchPomodoroPhase(PomodoroPhase.SHORT_BREAK)
        assertEquals("05:00", DateUtils.formatCountdown(PomodoroPhase.SHORT_BREAK.durationSeconds))
        assertEquals("25:00", DateUtils.formatCountdown(PomodoroPhase.FOCUS.durationSeconds))
    }
}

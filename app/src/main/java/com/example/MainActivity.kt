package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.SprintDatabase
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskStatus
import com.example.data.repository.SprintRepository
import com.example.ui.components.SprintConfigSheet
import com.example.ui.components.TaskEditorSheet
import com.example.ui.screens.KanbanBoardScreen
import com.example.ui.screens.PomodoroFocusScreen
import com.example.ui.theme.SprintFlowTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.SprintViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SprintFlowTheme {
                SprintFlowApp()
            }
        }
    }
}

@Composable
fun SprintFlowApp() {
    val sprintViewModel: SprintViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = SprintDatabase.getInstance(app)
                val repo = SprintRepository(db.sprintDao())
                SprintViewModel(
                    repository = repo,
                    defaultSprintName = app.getString(R.string.sprint_default_name),
                    defaultSprintGoal = app.getString(R.string.sprint_default_goal)
                )
            }
        }
    )

    val uiState by sprintViewModel.uiState.collectAsStateWithLifecycle()

    var showTaskSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<SprintTaskEntity?>(null) }
    var initialTaskStatus by remember { mutableStateOf(TaskStatus.TODO) }
    var showSprintConfigSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.destination == AppDestination.FOCUS) {
        sprintViewModel.selectDestination(AppDestination.KANBAN)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 640.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBarItem(
                            selected = uiState.destination == AppDestination.KANBAN,
                            onClick = { sprintViewModel.selectDestination(AppDestination.KANBAN) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.destination == AppDestination.KANBAN) {
                                        Icons.Rounded.Dashboard
                                    } else {
                                        Icons.Outlined.Dashboard
                                    },
                                    contentDescription = stringResource(R.string.nav_kanban)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_kanban)) },
                            modifier = Modifier.testTag("nav_kanban")
                        )
                        NavigationBarItem(
                            selected = uiState.destination == AppDestination.FOCUS,
                            onClick = { sprintViewModel.selectDestination(AppDestination.FOCUS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.destination == AppDestination.FOCUS) {
                                        Icons.Rounded.Timer
                                    } else {
                                        Icons.Outlined.Timer
                                    },
                                    contentDescription = stringResource(R.string.nav_focus)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_focus)) },
                            modifier = Modifier.testTag("nav_focus")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (uiState.destination == AppDestination.KANBAN) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingTask = null
                            initialTaskStatus = when (uiState.columnFilter) {
                                com.example.ui.viewmodel.ColumnFilter.IN_PROGRESS -> TaskStatus.IN_PROGRESS
                                com.example.ui.viewmodel.ColumnFilter.DONE -> TaskStatus.DONE
                                else -> TaskStatus.TODO
                            }
                            showTaskSheet = true
                        },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.add_task_cta)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("add_task_fab")
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = uiState.destination == AppDestination.KANBAN,
                            onClick = { sprintViewModel.selectDestination(AppDestination.KANBAN) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.destination == AppDestination.KANBAN) {
                                        Icons.Rounded.Dashboard
                                    } else {
                                        Icons.Outlined.Dashboard
                                    },
                                    contentDescription = stringResource(R.string.nav_kanban)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_kanban)) },
                            modifier = Modifier.testTag("nav_kanban")
                        )
                        NavigationRailItem(
                            selected = uiState.destination == AppDestination.FOCUS,
                            onClick = { sprintViewModel.selectDestination(AppDestination.FOCUS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.destination == AppDestination.FOCUS) {
                                        Icons.Rounded.Timer
                                    } else {
                                        Icons.Outlined.Timer
                                    },
                                    contentDescription = stringResource(R.string.nav_focus)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_focus)) },
                            modifier = Modifier.testTag("nav_focus")
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (uiState.destination) {
                        AppDestination.KANBAN -> {
                            KanbanBoardScreen(
                                uiState = uiState,
                                onEditSprintClick = { showSprintConfigSheet = true },
                                onColumnFilterSelected = sprintViewModel::selectColumnFilter,
                                onSearchQueryChange = sprintViewModel::updateSearchQuery,
                                onToggleViewMode = sprintViewModel::toggleBoardViewMode,
                                onAddTaskClick = { colStatus ->
                                    editingTask = null
                                    initialTaskStatus = colStatus
                                    showTaskSheet = true
                                },
                                onTaskClick = { task ->
                                    editingTask = task
                                    initialTaskStatus = task.status
                                    showTaskSheet = true
                                },
                                onMoveTask = sprintViewModel::moveTask,
                                onStartFocusForTask = sprintViewModel::startFocusForTask,
                                onToggleMiniPomodoro = sprintViewModel::toggleTimer,
                                onOpenFocusScreen = {
                                    sprintViewModel.selectDestination(AppDestination.FOCUS)
                                }
                            )
                        }
                        AppDestination.FOCUS -> {
                            PomodoroFocusScreen(
                                uiState = uiState,
                                onSelectTask = sprintViewModel::selectTaskForFocus,
                                onMoveTaskToInProgress = { taskId ->
                                    sprintViewModel.moveTask(taskId, TaskStatus.IN_PROGRESS)
                                },
                                onSwitchPhase = sprintViewModel::switchPomodoroPhase,
                                onToggleTimer = sprintViewModel::toggleTimer,
                                onResetTimer = sprintViewModel::resetTimer,
                                onSkipPhase = sprintViewModel::skipPhase,
                                onDismissBanner = sprintViewModel::dismissCompletionBanner
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTaskSheet) {
        TaskEditorSheet(
            existingTask = editingTask,
            initialStatus = initialTaskStatus,
            sprintEndMillis = uiState.sprintConfig?.endDateMillis,
            onDismiss = { showTaskSheet = false },
            onSave = { title, desc, status, priority, category, sp, deadline, estPomos ->
                sprintViewModel.saveTask(
                    existingTask = editingTask,
                    title = title,
                    description = desc,
                    status = status,
                    priority = priority,
                    category = category,
                    storyPoints = sp,
                    deadlineMillis = deadline,
                    estimatedPomodoros = estPomos
                )
                showTaskSheet = false
            },
            onDelete = editingTask?.let { task ->
                {
                    sprintViewModel.deleteTask(task.id)
                    showTaskSheet = false
                }
            }
        )
    }

    if (showSprintConfigSheet) {
        SprintConfigSheet(
            currentConfig = uiState.sprintConfig,
            onDismiss = { showSprintConfigSheet = false },
            onSave = { name, goal, weeks, startMillis ->
                sprintViewModel.saveSprintConfig(name, goal, weeks, startMillis)
                showSprintConfigSheet = false
            }
        )
    }
}

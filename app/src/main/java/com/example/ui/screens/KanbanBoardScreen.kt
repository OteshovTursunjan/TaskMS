package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.ViewStream
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.components.MiniPomodoroBar
import com.example.ui.components.SprintHeaderCard
import com.example.ui.components.SprintTaskCard
import com.example.ui.components.statusDisplayName
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.ui.viewmodel.BoardViewMode
import com.example.ui.viewmodel.ColumnFilter
import com.example.ui.viewmodel.SprintUiState

@Composable
fun KanbanBoardScreen(
    uiState: SprintUiState,
    onEditSprintClick: () -> Unit,
    onColumnFilterSelected: (ColumnFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onAddTaskClick: (TaskStatus) -> Unit,
    onTaskClick: (SprintTaskEntity) -> Unit,
    onMoveTask: (Long, TaskStatus) -> Unit,
    onStartFocusForTask: (SprintTaskEntity) -> Unit,
    onToggleMiniPomodoro: () -> Unit,
    onOpenFocusScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showMiniTimer = uiState.pomodoro.isRunning ||
        uiState.pomodoro.remainingSeconds < uiState.pomodoro.totalSeconds

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        SprintHeaderCard(
            sprintConfig = uiState.sprintConfig,
            totalTasks = uiState.allTasks.size,
            doneTasks = uiState.doneCount,
            totalPomodoros = uiState.totalPomodoros,
            onEditSprintClick = onEditSprintClick
        )

        if (showMiniTimer) {
            MiniPomodoroBar(
                pomodoro = uiState.pomodoro,
                selectedTaskTitle = uiState.selectedFocusTask?.title,
                onTogglePlayPause = onToggleMiniPomodoro,
                onOpenFocusScreen = onOpenFocusScreen
            )
        }

        // Search + View mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.search_tasks_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_tasks_input")
            )

            if (uiState.columnFilter == ColumnFilter.ALL) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier
                            .size(52.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_board_view_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.boardViewMode == BoardViewMode.COLUMNS) {
                                Icons.Rounded.ViewStream
                            } else {
                                Icons.Rounded.ViewColumn
                            },
                            contentDescription = stringResource(R.string.view_mode_toggle_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Column Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.columnFilter == ColumnFilter.ALL,
                onClick = { onColumnFilterSelected(ColumnFilter.ALL) },
                label = {
                    Text("${stringResource(R.string.filter_all)} (${uiState.allTasks.size})")
                },
                modifier = Modifier.testTag("filter_chip_all")
            )
            FilterChip(
                selected = uiState.columnFilter == ColumnFilter.TODO,
                onClick = { onColumnFilterSelected(ColumnFilter.TODO) },
                label = {
                    Text("${stringResource(R.string.column_todo)} (${uiState.todoCount})")
                },
                modifier = Modifier.testTag("filter_chip_todo")
            )
            FilterChip(
                selected = uiState.columnFilter == ColumnFilter.IN_PROGRESS,
                onClick = { onColumnFilterSelected(ColumnFilter.IN_PROGRESS) },
                label = {
                    Text("${stringResource(R.string.column_in_progress)} (${uiState.inProgressCount})")
                },
                modifier = Modifier.testTag("filter_chip_in_progress")
            )
            FilterChip(
                selected = uiState.columnFilter == ColumnFilter.DONE,
                onClick = { onColumnFilterSelected(ColumnFilter.DONE) },
                label = {
                    Text("${stringResource(R.string.column_done)} (${uiState.doneCount})")
                },
                modifier = Modifier.testTag("filter_chip_done")
            )
        }

        // Main Content Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (uiState.allTasks.isEmpty()) {
                EmptySprintState(
                    onAddFirstTask = { onAddTaskClick(TaskStatus.TODO) },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (uiState.columnFilter == ColumnFilter.ALL && uiState.boardViewMode == BoardViewMode.COLUMNS) {
                KanbanColumnsBoard(
                    tasks = uiState.filteredTasks,
                    activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                    onAddToColumn = onAddTaskClick,
                    onTaskClick = onTaskClick,
                    onMoveTask = onMoveTask,
                    onStartFocusForTask = onStartFocusForTask
                )
            } else {
                FilteredTaskList(
                    tasks = uiState.filteredTasks,
                    columnFilter = uiState.columnFilter,
                    activeFocusTaskId = uiState.pomodoro.selectedTaskId,
                    onTaskClick = onTaskClick,
                    onMoveTask = onMoveTask,
                    onStartFocusForTask = onStartFocusForTask
                )
            }
        }
    }
}

@Composable
private fun KanbanColumnsBoard(
    tasks: List<SprintTaskEntity>,
    activeFocusTaskId: Long?,
    onAddToColumn: (TaskStatus) -> Unit,
    onTaskClick: (SprintTaskEntity) -> Unit,
    onMoveTask: (Long, TaskStatus) -> Unit,
    onStartFocusForTask: (SprintTaskEntity) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideBoard = maxWidth >= 820.dp
        if (isWideBoard) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaskStatus.entries.forEach { status ->
                    val colTasks = tasks.filter { it.status == status }
                    KanbanColumnContainer(
                        status = status,
                        tasks = colTasks,
                        activeFocusTaskId = activeFocusTaskId,
                        onAddToColumn = { onAddToColumn(status) },
                        onTaskClick = onTaskClick,
                        onMoveTask = onMoveTask,
                        onStartFocusForTask = onStartFocusForTask,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        } else {
            val columnWidth = (maxWidth * 0.86f).coerceIn(280.dp, 360.dp)
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(TaskStatus.entries, key = { it.name }) { status ->
                    val colTasks = tasks.filter { it.status == status }
                    KanbanColumnContainer(
                        status = status,
                        tasks = colTasks,
                        activeFocusTaskId = activeFocusTaskId,
                        onAddToColumn = { onAddToColumn(status) },
                        onTaskClick = onTaskClick,
                        onMoveTask = onMoveTask,
                        onStartFocusForTask = onStartFocusForTask,
                        modifier = Modifier
                            .width(columnWidth)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanColumnContainer(
    status: TaskStatus,
    tasks: List<SprintTaskEntity>,
    activeFocusTaskId: Long?,
    onAddToColumn: () -> Unit,
    onTaskClick: (SprintTaskEntity) -> Unit,
    onMoveTask: (Long, TaskStatus) -> Unit,
    onStartFocusForTask: (SprintTaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor: Color = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.primary
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        TaskStatus.DONE -> MaterialTheme.colorScheme.tertiary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        text = statusDisplayName(status),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = tasks.size.toString(),
                            style = MonoBadgeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onAddToColumn,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_to_column_${status.name}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_task_cta),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_column_title, statusDisplayName(status)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        SprintTaskCard(
                            task = task,
                            isActiveFocusTask = task.id == activeFocusTaskId,
                            onCardClick = { onTaskClick(task) },
                            onMoveStatus = { targetStatus -> onMoveTask(task.id, targetStatus) },
                            onStartFocus = { onStartFocusForTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilteredTaskList(
    tasks: List<SprintTaskEntity>,
    columnFilter: ColumnFilter,
    activeFocusTaskId: Long?,
    onTaskClick: (SprintTaskEntity) -> Unit,
    onMoveTask: (Long, TaskStatus) -> Unit,
    onStartFocusForTask: (SprintTaskEntity) -> Unit
) {
    if (tasks.isEmpty()) {
        val colName = when (columnFilter) {
            ColumnFilter.ALL -> stringResource(R.string.filter_all)
            ColumnFilter.TODO -> stringResource(R.string.column_todo)
            ColumnFilter.IN_PROGRESS -> stringResource(R.string.column_in_progress)
            ColumnFilter.DONE -> stringResource(R.string.column_done)
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.widthIn(max = 360.dp)
            ) {
                Text(
                    text = stringResource(R.string.empty_column_title, colName),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.empty_column_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                SprintTaskCard(
                    task = task,
                    isActiveFocusTask = task.id == activeFocusTaskId,
                    onCardClick = { onTaskClick(task) },
                    onMoveStatus = { targetStatus -> onMoveTask(task.id, targetStatus) },
                    onStartFocus = { onStartFocusForTask(task) }
                )
            }
        }
    }
}

@Composable
private fun EmptySprintState(
    onAddFirstTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_sprint_empty),
                contentDescription = stringResource(R.string.empty_sprint_title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Text(
                text = stringResource(R.string.empty_sprint_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.empty_sprint_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onAddFirstTask,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_first_task_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_first_task_cta),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

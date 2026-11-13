package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.components.CategoryPill
import com.example.ui.components.DeadlineBadge
import com.example.ui.components.statusDisplayName
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.ui.theme.PomodoroTimerTextStyle
import com.example.ui.viewmodel.PomodoroPhase
import com.example.ui.viewmodel.SprintUiState
import com.example.util.DateUtils
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroFocusScreen(
    uiState: SprintUiState,
    onSelectTask: (Long?) -> Unit,
    onMoveTaskToInProgress: (Long) -> Unit,
    onSwitchPhase: (PomodoroPhase) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSkipPhase: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTaskPicker by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val pomodoro = uiState.pomodoro
    val selectedTask = uiState.selectedFocusTask

    LaunchedEffect(pomodoro.completionNonce) {
        if (pomodoro.completionNonce > 0L) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val isFocusPhase = pomodoro.phase == PomodoroPhase.FOCUS
    val accentColor = if (isFocusPhase) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val secondaryGlow = if (isFocusPhase) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = pomodoro.progress,
        animationSpec = spring(),
        label = "pomodoro_arc"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Completion celebration banner
        item {
            AnimatedVisibility(visible = pomodoro.completionMessagePhase != null) {
                val msg = if (pomodoro.completionMessagePhase == PomodoroPhase.FOCUS) {
                    stringResource(R.string.pomodoro_focus_completed_banner)
                } else {
                    stringResource(R.string.pomodoro_break_completed_banner)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        IconButton(onClick = onDismissBanner) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Selected Task Target Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { showTaskPicker = true }
                    .testTag("pomodoro_task_selector"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTask != null) {
                                statusDisplayName(selectedTask.status)
                            } else {
                                stringResource(R.string.pomodoro_select_task_prompt)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.pomodoro_change_task),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (selectedTask != null) {
                        Text(
                            text = selectedTask.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryPill(selectedTask.category)
                            DeadlineBadge(
                                deadlineMillis = selectedTask.deadlineMillis,
                                isDone = selectedTask.status == TaskStatus.DONE
                            )
                            Text(
                                text = "${selectedTask.pomodorosCompleted}/${selectedTask.estimatedPomodoros}",
                                style = MonoBadgeTextStyle,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (selectedTask.status == TaskStatus.TODO) {
                            OutlinedButton(
                                onClick = { onMoveTaskToInProgress(selectedTask.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(stringResource(R.string.pomodoro_move_in_progress))
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.pomodoro_no_task_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Phase switcher chips (25:00 Focus / 05:00 Break)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = pomodoro.phase == PomodoroPhase.FOCUS,
                    onClick = { onSwitchPhase(PomodoroPhase.FOCUS) },
                    label = { Text(stringResource(R.string.pomodoro_tab_focus)) },
                    modifier = Modifier.testTag("phase_chip_focus")
                )
                FilterChip(
                    selected = pomodoro.phase == PomodoroPhase.SHORT_BREAK,
                    onClick = { onSwitchPhase(PomodoroPhase.SHORT_BREAK) },
                    label = { Text(stringResource(R.string.pomodoro_tab_break)) },
                    modifier = Modifier.testTag("phase_chip_break")
                )
            }
        }

        // Chronograph Circular Dial
        item {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val tickInactive = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 14.dp.toPx()
                    val pad = strokePx + 14.dp.toPx()
                    val arcSize = Size(size.width - pad * 2f, size.height - pad * 2f)
                    val topLeft = Offset(pad, pad)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = (size.minDimension / 2f) - 4.dp.toPx()

                    // Precision outer ticks
                    for (i in 0 until 60) {
                        val angleRad = Math.toRadians((i * 6.0) - 90.0)
                        val isMajor = i % 5 == 0
                        val innerR = outerRadius - if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                        val startPt = Offset(
                            center.x + (innerR * cos(angleRad)).toFloat(),
                            center.y + (innerR * sin(angleRad)).toFloat()
                        )
                        val endPt = Offset(
                            center.x + (outerRadius * cos(angleRad)).toFloat(),
                            center.y + (outerRadius * sin(angleRad)).toFloat()
                        )
                        drawLine(
                            color = if (isMajor) accentColor.copy(alpha = 0.65f) else tickInactive,
                            start = startPt,
                            end = endPt,
                            strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Active sweep arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(accentColor, secondaryGlow, accentColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isFocusPhase) {
                            stringResource(R.string.pomodoro_phase_focus)
                        } else {
                            stringResource(R.string.pomodoro_phase_break)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Text(
                        text = DateUtils.formatCountdown(pomodoro.remainingSeconds),
                        style = PomodoroTimerTextStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("pomodoro_countdown_text")
                    )
                    Text(
                        text = stringResource(R.string.pomodoro_cycles_today, uiState.todayFocusCycles),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Transport Controls
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                        .size(52.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("pomodoro_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.pomodoro_reset)
                    )
                }

                Button(
                    onClick = onToggleTimer,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("pomodoro_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (pomodoro.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val actionLabel = when {
                        pomodoro.isRunning -> stringResource(R.string.pomodoro_pause)
                        pomodoro.remainingSeconds < pomodoro.totalSeconds -> stringResource(R.string.pomodoro_resume)
                        else -> stringResource(R.string.pomodoro_start)
                    }
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                FilledTonalIconButton(
                    onClick = onSkipPhase,
                    modifier = Modifier
                        .size(52.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("pomodoro_skip_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.pomodoro_skip)
                    )
                }
            }
        }

        // Recent Focus History
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.pomodoro_history_title),
                    style = MaterialTheme.typography.titleMedium
                )
                if (uiState.recentSessions.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.pomodoro_history_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        items(uiState.recentSessions.take(8), key = { it.id }) { session ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = session.taskTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${session.durationMinutes}m • ${DateUtils.formatTime(session.completedAtMillis)}",
                        style = MonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTaskPicker) {
        TaskPickerSheet(
            tasks = uiState.allTasks.filter { it.status != TaskStatus.DONE },
            selectedTaskId = uiState.pomodoro.selectedTaskId,
            onSelect = { taskId ->
                onSelectTask(taskId)
                showTaskPicker = false
            },
            onDismiss = { showTaskPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskPickerSheet(
    tasks: List<SprintTaskEntity>,
    selectedTaskId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.pomodoro_select_task_prompt),
                style = MaterialTheme.typography.titleLarge
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(null) },
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTaskId == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = stringResource(R.string.pomodoro_free_session),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    val isSelected = task.id == selectedTaskId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(task.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = statusDisplayName(task.status),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${task.pomodorosCompleted}/${task.estimatedPomodoros}",
                                style = MonoBadgeTextStyle
                            )
                        }
                    }
                }
            }
        }
    }
}

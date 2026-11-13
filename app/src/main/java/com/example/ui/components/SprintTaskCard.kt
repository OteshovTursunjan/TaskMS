package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.util.DateUtils

@Composable
fun SprintTaskCard(
    task: SprintTaskEntity,
    isActiveFocusTask: Boolean,
    onCardClick: () -> Unit,
    onMoveStatus: (TaskStatus) -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.primary
        TaskPriority.HIGH -> MaterialTheme.colorScheme.secondary
        TaskPriority.CRITICAL -> MaterialTheme.colorScheme.error
    }

    val borderStroke = if (isActiveFocusTask) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = borderStroke,
        tonalElevation = if (isActiveFocusTask) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top metadata row: Category + Story Points + Edit icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryPill(category = task.category)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${task.storyPoints} SP",
                                style = MonoBadgeTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (isActiveFocusTask) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "FOCUS",
                                    style = MonoBadgeTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onCardClick,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("edit_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_task_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Title & optional notes
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.status == TaskStatus.DONE) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Deadline & Pomodoro count badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeadlineBadge(
                        deadlineMillis = task.deadlineMillis,
                        isDone = task.status == TaskStatus.DONE
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${task.pomodorosCompleted}/${task.estimatedPomodoros}",
                                style = MonoBadgeTextStyle,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Bottom action row: Column move controls + Focus CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prevStatus = when (task.status) {
                            TaskStatus.TODO -> null
                            TaskStatus.IN_PROGRESS -> TaskStatus.TODO
                            TaskStatus.DONE -> TaskStatus.IN_PROGRESS
                        }
                        val nextStatus = when (task.status) {
                            TaskStatus.TODO -> TaskStatus.IN_PROGRESS
                            TaskStatus.IN_PROGRESS -> TaskStatus.DONE
                            TaskStatus.DONE -> null
                        }

                        if (prevStatus != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onMoveStatus(prevStatus) }
                                    .testTag("move_prev_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(
                                            R.string.move_to_column_cd,
                                            statusDisplayName(prevStatus)
                                        ),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = statusDisplayName(prevStatus),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (nextStatus != null) {
                            val nextBg = if (nextStatus == TaskStatus.DONE) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                            val nextFg = if (nextStatus == TaskStatus.DONE) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = nextBg,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onMoveStatus(nextStatus) }
                                    .testTag("move_next_${task.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = statusDisplayName(nextStatus),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = nextFg
                                    )
                                    Icon(
                                        imageVector = if (nextStatus == TaskStatus.DONE) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = stringResource(
                                            R.string.move_to_column_cd,
                                            statusDisplayName(nextStatus)
                                        ),
                                        tint = nextFg,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (task.status != TaskStatus.DONE) {
                        FilledTonalButton(
                            onClick = onStartFocus,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("focus_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.focus_on_task),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPill(category: TaskCategory) {
    val label = when (category) {
        TaskCategory.FEATURE -> stringResource(R.string.cat_feature)
        TaskCategory.BUG -> stringResource(R.string.cat_bug)
        TaskCategory.REFACTOR -> stringResource(R.string.cat_refactor)
        TaskCategory.DEVOPS -> stringResource(R.string.cat_devops)
        TaskCategory.DOCS -> stringResource(R.string.cat_docs)
    }
    val (bg, fg) = when (category) {
        TaskCategory.FEATURE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TaskCategory.BUG -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        TaskCategory.REFACTOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TaskCategory.DEVOPS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        TaskCategory.DOCS -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun DeadlineBadge(deadlineMillis: Long?, isDone: Boolean) {
    if (deadlineMillis == null) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(R.string.deadline_none),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val diffDays = DateUtils.daysFromToday(deadlineMillis)
    val shortDate = DateUtils.formatShortDate(deadlineMillis)

    val (text, bg, fg) = when {
        isDone -> Triple(
            shortDate,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        diffDays < 0 -> Triple(
            stringResource(R.string.deadline_overdue, shortDate),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        diffDays == 0 -> Triple(
            stringResource(R.string.deadline_today),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        diffDays == 1 -> Triple(
            stringResource(R.string.deadline_tomorrow),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Triple(
            stringResource(R.string.deadline_days_left, diffDays, shortDate),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
fun statusDisplayName(status: TaskStatus): String = when (status) {
    TaskStatus.TODO -> stringResource(R.string.column_todo)
    TaskStatus.IN_PROGRESS -> stringResource(R.string.column_in_progress)
    TaskStatus.DONE -> stringResource(R.string.column_done)
}

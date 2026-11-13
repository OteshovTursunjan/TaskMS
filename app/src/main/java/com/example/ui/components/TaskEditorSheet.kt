package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SprintTaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorSheet(
    existingTask: SprintTaskEntity?,
    initialStatus: TaskStatus,
    sprintEndMillis: Long?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        status: TaskStatus,
        priority: TaskPriority,
        category: TaskCategory,
        storyPoints: Int,
        deadlineMillis: Long?,
        estimatedPomodoros: Int
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }
    var status by remember(existingTask, initialStatus) {
        mutableStateOf(existingTask?.status ?: initialStatus)
    }
    var priority by remember(existingTask) {
        mutableStateOf(existingTask?.priority ?: TaskPriority.MEDIUM)
    }
    var category by remember(existingTask) {
        mutableStateOf(existingTask?.category ?: TaskCategory.FEATURE)
    }
    var storyPoints by remember(existingTask) {
        mutableIntStateOf(existingTask?.storyPoints ?: 2)
    }
    var deadlineMillis by remember(existingTask) {
        mutableStateOf<Long?>(
            existingTask?.let { it.deadlineMillis }
                ?: if (existingTask == null) DateUtils.addDays(DateUtils.startOfLocalDayMillis(), 2) else null
        )
    }
    var estimatedPomodoros by remember(existingTask) {
        mutableIntStateOf(existingTask?.estimatedPomodoros ?: 2)
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val todayMillis = remember { DateUtils.startOfLocalDayMillis() }
    val tomorrowMillis = remember { DateUtils.addDays(todayMillis, 1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTask == null) {
                        stringResource(R.string.sheet_new_task)
                    } else {
                        stringResource(R.string.sheet_edit_task)
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                if (existingTask != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete_task))
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title_label)) },
                placeholder = { Text(stringResource(R.string.field_title_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.field_desc_label)) },
                placeholder = { Text(stringResource(R.string.field_desc_placeholder)) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_desc_input")
            )

            // Column selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.field_column_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskStatus.entries.forEach { col ->
                        FilterChip(
                            selected = status == col,
                            onClick = { status = col },
                            label = { Text(statusDisplayName(col)) },
                            modifier = Modifier.testTag("editor_status_${col.name}")
                        )
                    }
                }
            }

            // Priority selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.field_priority_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        val pLabel = when (p) {
                            TaskPriority.LOW -> stringResource(R.string.priority_low)
                            TaskPriority.MEDIUM -> stringResource(R.string.priority_medium)
                            TaskPriority.HIGH -> stringResource(R.string.priority_high)
                            TaskPriority.CRITICAL -> stringResource(R.string.priority_critical)
                        }
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(pLabel) }
                        )
                    }
                }
            }

            // Category & Story Points
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.field_category_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskCategory.entries.forEach { c ->
                        val cLabel = when (c) {
                            TaskCategory.FEATURE -> stringResource(R.string.cat_feature)
                            TaskCategory.BUG -> stringResource(R.string.cat_bug)
                            TaskCategory.REFACTOR -> stringResource(R.string.cat_refactor)
                            TaskCategory.DEVOPS -> stringResource(R.string.cat_devops)
                            TaskCategory.DOCS -> stringResource(R.string.cat_docs)
                        }
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(cLabel) }
                        )
                    }
                }
            }

            // Deadline picker section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.field_deadline_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = deadlineMillis?.let { DateUtils.formatShortDate(it) }
                            ?: stringResource(R.string.deadline_none),
                        style = MonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = deadlineMillis == todayMillis,
                        onClick = { deadlineMillis = todayMillis },
                        label = { Text(stringResource(R.string.deadline_today)) }
                    )
                    FilterChip(
                        selected = deadlineMillis == tomorrowMillis,
                        onClick = { deadlineMillis = tomorrowMillis },
                        label = { Text(stringResource(R.string.deadline_tomorrow)) }
                    )
                    if (sprintEndMillis != null) {
                        FilterChip(
                            selected = deadlineMillis == sprintEndMillis,
                            onClick = { deadlineMillis = sprintEndMillis },
                            label = { Text(stringResource(R.string.deadline_sprint_end)) }
                        )
                    }
                    FilterChip(
                        selected = deadlineMillis == null,
                        onClick = { deadlineMillis = null },
                        label = { Text(stringResource(R.string.deadline_none)) }
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("deadline_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.deadline_pick_date))
                    }
                }
            }

            // Story Points & Estimated Pomodoros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.field_est_pomodoros),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 5, 8).forEach { sp ->
                            FilterChip(
                                selected = storyPoints == sp,
                                onClick = { storyPoints = sp },
                                label = { Text("$sp SP") }
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { if (estimatedPomodoros > 1) estimatedPomodoros-- },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Уменьшить")
                        }
                        Text(
                            text = "${estimatedPomodoros}x25м",
                            style = MonoBadgeTextStyle.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { if (estimatedPomodoros < 12) estimatedPomodoros++ },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Увеличить")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        title,
                        description,
                        status,
                        priority,
                        category,
                        storyPoints,
                        deadlineMillis,
                        estimatedPomodoros
                    )
                },
                enabled = title.trim().isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_task_button")
            ) {
                Text(
                    text = stringResource(R.string.save_task),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showDatePicker) {
        val initialSelectedUtc = deadlineMillis?.let { DateUtils.localDayToUtcPickerMillis(it) }
            ?: DateUtils.localDayToUtcPickerMillis(todayMillis)
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedUtc)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            deadlineMillis = DateUtils.utcPickerMillisToLocalDayStart(utcMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save_sprint))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

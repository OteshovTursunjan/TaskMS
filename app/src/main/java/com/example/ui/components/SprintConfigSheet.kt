package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SprintConfigEntity
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SprintConfigSheet(
    currentConfig: SprintConfigEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, goal: String, durationWeeks: Int, startDateMillis: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(currentConfig) {
        mutableStateOf(currentConfig?.sprintName ?: "Sprint #1")
    }
    var goal by remember(currentConfig) {
        mutableStateOf(currentConfig?.sprintGoal ?: "")
    }
    var durationWeeks by remember(currentConfig) {
        mutableIntStateOf(currentConfig?.durationWeeks ?: 2)
    }
    var startDateMillis by remember(currentConfig) {
        mutableLongStateOf(currentConfig?.startDateMillis ?: DateUtils.startOfLocalDayMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val calculatedEndMillis = remember(startDateMillis, durationWeeks) {
        DateUtils.addDays(startDateMillis, (durationWeeks * 7) - 1)
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sheet_sprint_settings),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_sprint_name)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sprint_name_input")
            )

            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text(stringResource(R.string.field_sprint_goal)) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.field_sprint_duration),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = durationWeeks == 1,
                        onClick = { durationWeeks = 1 },
                        label = { Text(stringResource(R.string.sprint_week_1)) },
                        modifier = Modifier.testTag("duration_1_week")
                    )
                    FilterChip(
                        selected = durationWeeks == 2,
                        onClick = { durationWeeks = 2 },
                        label = { Text(stringResource(R.string.sprint_week_2)) },
                        modifier = Modifier.testTag("duration_2_weeks")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${DateUtils.formatShortDate(startDateMillis)} — ${DateUtils.formatShortDate(calculatedEndMillis)}",
                        style = MonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = { startDateMillis = DateUtils.startOfLocalDayMillis() },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(stringResource(R.string.sprint_start_today))
                    }
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
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

            Button(
                onClick = { onSave(name, goal, durationWeeks, startDateMillis) },
                enabled = name.trim().isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_sprint_button")
            ) {
                Text(
                    text = stringResource(R.string.save_sprint),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.localDayToUtcPickerMillis(startDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utc ->
                            startDateMillis = DateUtils.utcPickerMillisToLocalDayStart(utc)
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

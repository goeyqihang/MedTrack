package com.qihang.medtrack.ui.symptoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qihang.medtrack.R
import com.qihang.medtrack.data.symptom.Symptom
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(
    modifier: Modifier = Modifier,
    onViewTrends: () -> Unit,
    viewModel: SymptomsViewModel = viewModel(
        factory = SymptomsViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // UI-only ephemeral state
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            launch { snackbarHostState.showSnackbar("Symptom logged successfully!") }
            viewModel.consumeSaveSuccess()
        }
    }

    val targetColor = when (state.severity.toInt()) {
        in 1..3 -> colorResource(id = R.color.severity_mild)
        in 4..6 -> colorResource(id = R.color.severity_moderate)
        else -> colorResource(id = R.color.severity_severe)
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "severityColor"
    )
    val severityLabel = when (state.severity.toInt()) {
        in 1..3 -> "Mild"
        in 4..6 -> "Moderate"
        else -> "Severe"
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(text = "Log Symptom", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // ── Category ──────────────────────────────────────────
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = state.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        SymptomsUiState.CATEGORY_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onCategoryChange(option)
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Severity slider ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Severity: ${state.severity.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = severityLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = animatedColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = state.severity,
                    onValueChange = { value -> viewModel.onSeverityChange(value) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedColor,
                        activeTrackColor = animatedColor,
                        inactiveTrackColor = animatedColor.copy(alpha = 0.24f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Date + Time pickers ───────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showDatePicker = !showDatePicker
                                showTimePicker = false
                            }
                    ) {
                        OutlinedTextField(
                            value = state.selectedDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Date *") },
                            isError = state.dateTimeError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (state.dateTimeError != null)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                disabledLabelColor = if (state.dateTimeError != null)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showTimePicker = !showTimePicker
                                showDatePicker = false
                            }
                    ) {
                        OutlinedTextField(
                            value = state.selectedTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Time *") },
                            isError = state.dateTimeError != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (state.dateTimeError != null)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                disabledLabelColor = if (state.dateTimeError != null)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (showDatePicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DatePicker(state = datePickerState)
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // DatePicker reports UTC midnight, so read the date in UTC
                                // (the local zone would shift it a day back west of UTC).
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                viewModel.onDateChange(date.toString())
                            }
                            showDatePicker = false
                        }) {
                            Text("Confirm Date")
                        }
                    }
                }

                if (showTimePicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                        Button(onClick = {
                            val formatted = String.format(
                                Locale.ROOT,
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            viewModel.onTimeChange(formatted)
                            showTimePicker = false
                        }) {
                            Text("Confirm Time")
                        }
                    }
                }

                state.dateTimeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Notes ─────────────────────────────────────────────
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { value -> viewModel.onNotesChange(value) },
                    label = { Text("Notes (Optional)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ),
                    supportingText = {
                        Text(
                            text = "${state.notes.length}/${SymptomsUiState.NOTES_MAX_LENGTH}",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                state.generalError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Save button ───────────────────────────────────────
                Button(
                    onClick = { viewModel.save() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Symptom")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Symptom History",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TextButton(onClick = onViewTrends) {
                        Text("View Trends")
                    }
                }
            }

            // ── History list ──────────────────────────────────────────
            if (state.history.isEmpty()) {
                item {
                    Text(
                        text = "No symptoms logged yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(items = state.history, key = { it.id }) { symptom ->
                    SymptomCard(symptom = symptom)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SymptomCard(symptom: Symptom) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = symptom.category, style = MaterialTheme.typography.titleMedium)

                val (color, label) = when (symptom.severity) {
                    in 1..3 -> Pair(colorResource(id = R.color.severity_mild), "Mild")
                    in 4..6 -> Pair(colorResource(id = R.color.severity_moderate), "Moderate")
                    else -> Pair(colorResource(id = R.color.severity_severe), "Severe")
                }

                Box(
                    modifier = Modifier
                        .background(color, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$label (${symptom.severity})",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = symptom.dateTime, style = MaterialTheme.typography.bodySmall)
            if (symptom.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = symptom.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

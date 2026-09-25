package com.qihang.medtrack.ui.addmed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = viewModel(
        factory = AddMedicationViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // UI-only ephemeral state — does NOT belong in the VM
    var unitExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            launch { snackbarHostState.showSnackbar("Medication saved successfully!") }
            delay(800)
            viewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Medication", style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ── Medication Name ───────────────────────────────────────
            OutlinedTextField(
                value = state.name,
                onValueChange = { value -> viewModel.onNameChange(value) },
                label = { Text("Medication Name *") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Dosage Amount + Unit ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = state.dosageAmount,
                    onValueChange = { value -> viewModel.onDosageAmountChange(value) },
                    label = { Text("Amount *") },
                    isError = state.dosageError != null,
                    supportingText = { state.dosageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(2f)
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.dosageUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                        },
                        modifier = Modifier.menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        AddMedicationUiState.UNIT_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.onDosageUnitChange(option)
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Frequency ─────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = !frequencyExpanded }
            ) {
                OutlinedTextField(
                    value = state.frequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    AddMedicationUiState.FREQUENCY_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onFrequencyChange(option)
                                frequencyExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Scheduled Time ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = !showTimePicker }
            ) {
                OutlinedTextField(
                    value = state.time,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Scheduled Time *") },
                    isError = state.timeError != null,
                    supportingText = { state.timeError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (state.timeError != null)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        disabledLabelColor = if (state.timeError != null)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
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

            Spacer(modifier = Modifier.height(16.dp))

            // ── Medication Type ───────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = state.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medication Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    AddMedicationUiState.TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onTypeChange(option)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Notes ─────────────────────────────────────────────────
            OutlinedTextField(
                value = state.notes,
                onValueChange = { value -> viewModel.onNotesChange(value) },
                label = { Text("Notes (Optional)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            state.generalError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Buttons ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearForm() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Clear")
                }

                Button(
                    onClick = { viewModel.save() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

package com.qihang.medtrack.ui.medcoach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qihang.medtrack.data.drug.DrugInfo
import com.qihang.medtrack.data.medcoachtip.MedCoachTip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedCoachScreen(
    modifier: Modifier = Modifier,
    viewModel: MedCoachViewModel = viewModel(
        factory = MedCoachViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAllTipsDialog by remember { mutableStateOf(false) }

    if (showAllTipsDialog) {
        AllTipsDialog(
            tips = state.allTips,
            onDismiss = { showAllTipsDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "MedCoach", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // ══ Drug Information ═════════════════════════════════════════
        SectionTitle("Drug Information")
        Spacer(modifier = Modifier.height(8.dp))

        if (state.medicationNames.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = state.drugQuery,
                    onValueChange = { value -> viewModel.onQueryChange(value) },
                    label = { Text("Medication name") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.searchDrug() }),
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    state.medicationNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.onQueryChange(name)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = state.drugQuery,
                onValueChange = { value -> viewModel.onQueryChange(value) },
                label = { Text("Medication name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.searchDrug() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.searchDrug() },
            enabled = !state.isSearching,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isSearching) {
                ButtonSpinner()
            } else {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Search Drug Info")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val drugError = state.drugError
        val drugInfo = state.drugInfo
        when {
            drugError != null -> ErrorText(drugError)
            drugInfo != null -> DrugInfoCard(info = drugInfo)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // ══ GenAI Medication Tips ════════════════════════════════════
        SectionTitle("Medication Tips")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Get an AI-generated tip to help you stay on top of your schedule.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.generateTip() },
            enabled = !state.isGeneratingTip,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isGeneratingTip) {
                ButtonSpinner()
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Generate Tip")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        state.tipError?.let { error ->
            ErrorText(error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.latestTip?.let { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = { showAllTipsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Show All Tips (${state.allTips.size})")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Reusable pieces ──────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ButtonSpinner() {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 2.dp
    )
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun DrugInfoCard(info: DrugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── fixed header ──────────────────────────────────────────
            Text(
                text = info.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── scrollable details (capped height) ───────────────────
            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DrugField(label = "Purpose", value = info.purpose)
                DrugField(label = "Active Ingredient", value = info.activeIngredient)
                DrugField(label = "Dosage & Administration", value = info.dosage)
                DrugField(label = "Warnings", value = info.warnings)
            }
        }
    }
}

@Composable
private fun DrugField(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AllTipsDialog(tips: List<MedCoachTip>, onDismiss: () -> Unit) {
    val formatter = remember {
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ENGLISH)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Tips") },
        text = {
            if (tips.isEmpty()) {
                Text("No tips generated yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = tips, key = { it.id }) { tip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = formatter.format(Date(tip.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tip.tipText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

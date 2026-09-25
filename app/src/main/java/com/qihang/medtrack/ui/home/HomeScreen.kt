package com.qihang.medtrack.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qihang.medtrack.data.medication.Medication

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddMedication: () -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val error = state.error
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> HomeContent(
                state = state,
                onTakenChange = viewModel::onTakenChange,
                onNavigateToAddMedication = onNavigateToAddMedication
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onTakenChange: (Int, Boolean) -> Unit,
    onNavigateToAddMedication: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // ── greeting + patient ID ──────────────────────────────────────
        Text(
            text = "Hello, ${state.patientName}",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Patient ID: ${state.patientId}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = state.formattedDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${state.takenCount} of ${state.totalMeds} medications taken today",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── medication list ────────────────────────────────────────────
        if (state.medications.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No medications scheduled.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = state.medications, key = { it.id }) { med ->
                    MedicationCard(
                        medication = med,
                        isTaken = state.takenStates[med.id] == true,
                        onTakenChange = { taken -> onTakenChange(med.id, taken) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onNavigateToAddMedication) {
                Text("Add Medication")
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    isTaken: Boolean,
    onTakenChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    textDecoration = if (isTaken) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dosage: ${medication.dosage}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Frequency: ${medication.frequency}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Time: ${medication.time}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = isTaken,
                onCheckedChange = onTakenChange
            )
        }
    }
}

package com.qihang.medtrack.ui.cliniciandashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qihang.medtrack.data.clinician.ClinicStats

@Composable
fun ClinicianDashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: ClinicianDashboardViewModel = viewModel(
        factory = ClinicianDashboardViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Clinician Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // ══ Aggregate Statistics ═════════════════════════════════════
        SectionTitle("Aggregate Statistics")
        Spacer(modifier = Modifier.height(8.dp))

        val stats = state.stats
        val statsError = state.statsError
        when {
            state.isLoadingStats -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            statsError != null -> {
                Text(statsError, color = MaterialTheme.colorScheme.error)
            }

            stats != null -> StatsGrid(stats)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ══ GenAI Insights ═══════════════════════════════════════════
        SectionTitle("AI-Powered Insights")
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.findPatterns() },
            enabled = !state.isGeneratingInsights && state.stats != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isGeneratingInsights) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Find Patterns")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        state.insightsError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.insights.forEachIndexed { index, insight ->
            InsightCard(number = index + 1, text = insight)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun StatsGrid(stats: ClinicStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatRow(
            StatItem("Total Patients", stats.totalPatients.toString()),
            StatItem("Total Medications", stats.totalMedications.toString())
        )
        StatRow(
            StatItem(
                "Avg Meds / Patient",
                "%.1f".format(stats.avgMedicationsPerPatient)
            ),
            StatItem("Total Symptoms", stats.totalSymptoms.toString())
        )
        StatRow(
            StatItem("Top Symptom", stats.mostCommonSymptomCategory),
            StatItem(
                "Avg Severity",
                "%.1f / 10".format(stats.avgSymptomSeverity)
            )
        )
    }
}

private data class StatItem(val label: String, val value: String)

@Composable
private fun StatRow(left: StatItem, right: StatItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(item = left, modifier = Modifier.weight(1f))
        StatCard(item = right, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(item: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightCard(number: Int, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$number.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

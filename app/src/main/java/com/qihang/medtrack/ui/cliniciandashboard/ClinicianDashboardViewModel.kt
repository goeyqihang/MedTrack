package com.qihang.medtrack.ui.cliniciandashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.clinician.ClinicStats
import com.qihang.medtrack.data.clinician.ClinicianRepository
import com.qihang.medtrack.data.genai.GenAiRepository
import com.qihang.medtrack.data.genai.GenAiResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClinicianDashboardViewModel(
    private val clinicianRepo: ClinicianRepository,
    private val genAiRepo: GenAiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClinicianDashboardUiState())
    val uiState: StateFlow<ClinicianDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, statsError = null) }
            try {
                val stats = clinicianRepo.getAggregateStats()
                _uiState.update { it.copy(stats = stats, isLoadingStats = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingStats = false, statsError = "Failed to load statistics.")
                }
            }
        }
    }

    fun findPatterns() {
        val stats = _uiState.value.stats ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingInsights = true, insightsError = null) }

            when (val result = genAiRepo.generate(buildInsightPrompt(stats))) {
                is GenAiResult.Success -> _uiState.update {
                    it.copy(
                        isGeneratingInsights = false,
                        insights = parseInsights(result.text),
                        insightsError = null
                    )
                }
                is GenAiResult.Error -> _uiState.update {
                    it.copy(isGeneratingInsights = false, insightsError = result.message)
                }
            }
        }
    }

    private fun buildInsightPrompt(stats: ClinicStats): String = """
        You are a clinical data analyst. Below is aggregated, anonymised data
        from a medication-tracking app. Identify exactly 3 interesting patterns
        or observations about this data.
        Reply with ONLY 3 short points, one per line, no numbering and no extra text.

        - Total patients: ${stats.totalPatients}
        - Total medications: ${stats.totalMedications}
        - Average medications per patient: ${"%.1f".format(stats.avgMedicationsPerPatient)}
        - Total symptoms logged: ${stats.totalSymptoms}
        - Most common symptom category: ${stats.mostCommonSymptomCategory}
        - Average symptom severity: ${"%.1f".format(stats.avgSymptomSeverity)} out of 10
    """.trimIndent()

    /** Splits the model's reply into individual insight lines (max 3). */
    private fun parseInsights(text: String): List<String> =
        text.lines()
            .map { it.trim().removePrefix("-").removePrefix("*").trim() }
            .map { it.replace(Regex("^\\d+[.)]\\s*"), "") }
            .filter { it.isNotBlank() }
            .take(3)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ClinicianDashboardViewModel(container.clinicianRepository, container.genAiRepository)
            }
        }
    }
}

package com.qihang.medtrack.ui.symptomtrend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.data.symptom.Symptom
import com.qihang.medtrack.data.symptom.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SymptomTrendViewModel(
    private val repository: SymptomRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomTrendUiState())
    val uiState: StateFlow<SymptomTrendUiState> = _uiState.asStateFlow()

    /** All symptoms for this patient, sorted oldest → newest. Cached for re-filtering. */
    private var allSymptoms: List<Symptom> = emptyList()

    init {
        load()
    }

    private fun load() {
        val patientId = session.loggedInPatientId
        if (patientId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Not logged in.") }
            return
        }

        viewModelScope.launch {
            allSymptoms = repository.getByPatient(patientId)
                .sortedBy { it.dateTime }   // chronological order for the chart
            val categories = allSymptoms.map { it.category }.distinct().sorted()
            _uiState.update { it.copy(isLoading = false, categories = categories) }
            recompute()
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        recompute()
    }

    /** Rebuilds the chart points from the cached symptoms + current filter. */
    private fun recompute() {
        val selected = _uiState.value.selectedCategory
        val filtered = if (selected == null) {
            allSymptoms
        } else {
            allSymptoms.filter { it.category == selected }
        }

        val points = filtered.map { TrendPoint(it.severity, shortLabel(it.dateTime)) }
        val average = if (filtered.isNotEmpty()) {
            filtered.map { it.severity }.average()
        } else 0.0

        _uiState.update { it.copy(points = points, averageSeverity = average) }
    }

    /** "2026-03-18 09:30" → "03-18" */
    private fun shortLabel(dateTime: String): String {
        val datePart = dateTime.substringBefore(" ")
        return if (datePart.length >= 10) datePart.substring(5) else datePart
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SymptomTrendViewModel(container.symptomRepository, container.sessionManager)
            }
        }
    }
}

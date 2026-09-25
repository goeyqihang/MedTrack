package com.qihang.medtrack.ui.symptoms

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

class SymptomsViewModel(
    private val repository: SymptomRepository,
    session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomsUiState())
    val uiState: StateFlow<SymptomsUiState> = _uiState.asStateFlow()

    private val patientId: String? = session.loggedInPatientId

    init {
        observeHistory()
    }

    private fun observeHistory() {
        val id = patientId ?: return
        viewModelScope.launch {
            repository.observeByPatient(id).collect { list ->
                _uiState.update { it.copy(history = list) }
            }
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(category = value) }
    }

    fun onSeverityChange(value: Float) {
        _uiState.update { it.copy(severity = value) }
    }

    fun onNotesChange(value: String) {
        if (value.length <= SymptomsUiState.NOTES_MAX_LENGTH) {
            _uiState.update { it.copy(notes = value) }
        }
    }

    fun onDateChange(value: String) {
        _uiState.update { it.copy(selectedDate = value, dateTimeError = null) }
    }

    fun onTimeChange(value: String) {
        _uiState.update { it.copy(selectedTime = value, dateTimeError = null) }
    }

    fun save() {
        val s = _uiState.value

        if (s.selectedDate.isBlank() || s.selectedTime.isBlank()) {
            _uiState.update { it.copy(dateTimeError = "Date and Time are required") }
            return
        }

        val id = patientId
        if (id == null) {
            _uiState.update { it.copy(generalError = "Session expired. Please log in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val symptom = Symptom(
                patientID = id,
                category = s.category,
                severity = s.severity.toInt(),
                notes = s.notes.trim(),
                dateTime = "${s.selectedDate} ${s.selectedTime}"
            )
            repository.insert(symptom)

            _uiState.update {
                it.copy(isLoading = false, saveSuccess = true).resetForm()
            }
        }
    }

    fun clearForm() {
        _uiState.update { it.resetForm() }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun SymptomsUiState.resetForm(): SymptomsUiState = copy(
        category = SymptomsUiState.CATEGORY_OPTIONS[0],
        severity = 1f,
        notes = "",
        selectedDate = "",
        selectedTime = "",
        dateTimeError = null
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SymptomsViewModel(container.symptomRepository, container.sessionManager)
            }
        }
    }
}

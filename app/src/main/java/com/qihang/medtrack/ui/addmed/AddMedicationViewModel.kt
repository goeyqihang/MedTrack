package com.qihang.medtrack.ui.addmed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationRepository
import com.qihang.medtrack.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddMedicationViewModel(
    private val repository: MedicationRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onDosageAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(dosageAmount = value, dosageError = null) }
        }
    }

    fun onDosageUnitChange(value: String) {
        _uiState.update { it.copy(dosageUnit = value, dosageError = null) }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { it.copy(frequency = value) }
    }

    fun onTimeChange(value: String) {
        _uiState.update { it.copy(time = value, timeError = null) }
    }

    fun onTypeChange(value: String) {
        _uiState.update { it.copy(type = value) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun clearForm() {
        _uiState.value = AddMedicationUiState()
    }

    fun save() {
        val s = _uiState.value
        val nameError = if (s.name.isBlank()) "Medication name is required" else null
        val dosageError = when {
            s.dosageAmount.isBlank() -> "Dosage amount is required"
            !"${s.dosageAmount}${s.dosageUnit}".matches(
                Regex("^\\d+(\\.\\d+)?(mg|ml|g)$")
            ) -> "Invalid number format"
            else -> null
        }
        val timeError = if (s.time.isBlank()) "Time is required" else null

        if (nameError != null || dosageError != null || timeError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    dosageError = dosageError,
                    timeError = timeError
                )
            }
            return
        }

        val patientId = session.loggedInPatientId
        if (patientId == null) {
            _uiState.update {
                it.copy(generalError = "Session expired. Please log in again.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val newMed = Medication(
                patientID = patientId,
                name = s.name.trim(),
                dosage = "${s.dosageAmount}${s.dosageUnit}",
                frequency = s.frequency,
                time = s.time,
                type = s.type,
                notes = s.notes.trim()
            )
            repository.insert(newMed)

            _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                AddMedicationViewModel(container.medicationRepository, container.sessionManager)
            }
        }
    }
}

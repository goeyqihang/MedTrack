package com.qihang.medtrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val patientRepo: PatientRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val patientId = session.loggedInPatientId
        if (patientId == null) {
            _uiState.update {
                it.copy(isLoading = false, error = "Not logged in.")
            }
            return
        }

        viewModelScope.launch {
            val patient = patientRepo.getById(patientId)
            if (patient == null) {
                _uiState.update {
                    it.copy(isLoading = false, error = "User record not found.")
                }
            } else {
                _uiState.update {
                    it.copy(
                        patientId = patient.patientID,
                        name = patient.name,
                        phoneNumber = patient.phoneNumber,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(container.patientRepository, container.sessionManager)
            }
        }
    }
}

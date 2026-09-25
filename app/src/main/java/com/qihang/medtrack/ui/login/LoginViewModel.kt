package com.qihang.medtrack.ui.login

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

class LoginViewModel(
    private val repository: PatientRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPatientIdChange(value: String) {
        _uiState.update {
            it.copy(patientId = value, patientIdError = null, generalError = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, generalError = null)
        }
    }

    fun login() {
        val s = _uiState.value
        val idEmpty = s.patientId.isBlank()
        val pwdEmpty = s.password.isBlank()

        if (idEmpty || pwdEmpty) {
            _uiState.update {
                it.copy(
                    patientIdError = if (idEmpty) "PatientID cannot be empty" else null,
                    passwordError = if (pwdEmpty) "Password cannot be empty" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val patient = repository.login(s.patientId.trim(), s.password)
            if (patient == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Invalid PatientID or password"
                    )
                }
            } else {
                session.login(patient.patientID)
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                LoginViewModel(container.patientRepository, container.sessionManager)
            }
        }
    }
}

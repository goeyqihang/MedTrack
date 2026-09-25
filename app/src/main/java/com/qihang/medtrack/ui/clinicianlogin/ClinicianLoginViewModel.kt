package com.qihang.medtrack.ui.clinicianlogin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClinicianLoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClinicianLoginUiState())
    val uiState: StateFlow<ClinicianLoginUiState> = _uiState.asStateFlow()

    fun onKeyChange(value: String) {
        _uiState.update { it.copy(accessKey = value, error = null) }
    }

    fun verify() {
        if (_uiState.value.accessKey.trim() == ACCESS_KEY) {
            _uiState.update { it.copy(loginSuccess = true, error = null) }
        } else {
            _uiState.update { it.copy(error = "Incorrect access key.") }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    companion object {
        private const val ACCESS_KEY = "dollar-entry-apples"
    }
}

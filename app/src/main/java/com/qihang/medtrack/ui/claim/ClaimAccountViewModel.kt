package com.qihang.medtrack.ui.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.ui.validation.PasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClaimAccountViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClaimAccountUiState())
    val uiState: StateFlow<ClaimAccountUiState> = _uiState.asStateFlow()

    fun onPatientIdChange(value: String) {
        _uiState.update {
            it.copy(patientId = value, patientIdError = null, generalError = null)
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update {
            it.copy(phone = value, phoneError = null, generalError = null)
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update {
            it.copy(newPassword = value, passwordError = null, generalError = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
        }
    }

    fun claim() {
        val s = _uiState.value

        val patientIdError = if (s.patientId.isBlank()) "PatientID cannot be empty" else null
        val phoneError = if (s.phone.isBlank()) "Phone number cannot be empty" else null
        val passwordError = PasswordValidator.validate(s.newPassword)
        val confirmPasswordError =
            PasswordValidator.validateConfirmation(s.newPassword, s.confirmPassword)

        if (patientIdError != null || phoneError != null ||
            passwordError != null || confirmPasswordError != null
        ) {
            _uiState.update {
                it.copy(
                    patientIdError = patientIdError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val patient = repository.getForClaim(s.patientId.trim(), s.phone.trim())
            if (patient == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "PatientID and phone number do not match any account"
                    )
                }
                return@launch
            }

            if (patient.password != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "This account has already been claimed. Please log in."
                    )
                }
                return@launch
            }

            repository.setPassword(patient.patientID, s.newPassword)
            _uiState.update { it.copy(isLoading = false, claimSuccess = true) }
        }
    }

    fun consumeClaimSuccess() {
        _uiState.update { it.copy(claimSuccess = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ClaimAccountViewModel(container.patientRepository)
            }
        }
    }
}

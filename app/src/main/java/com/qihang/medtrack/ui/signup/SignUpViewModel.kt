package com.qihang.medtrack.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.ui.validation.PasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, nameError = null, generalError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmError = null, generalError = null)
        }
    }

    fun signUp() {
        val s = _uiState.value

        val nameError = if (s.fullName.isBlank()) "Full name is required" else null
        val phoneError = when {
            s.phone.isBlank() -> "Phone number is required"
            !s.phone.matches(Regex("^04\\d{8}$")) ->
                "Must start with '04' and be exactly 10 digits"
            else -> null
        }
        val passwordError = PasswordValidator.validate(s.password)
        val confirmError = PasswordValidator.validateConfirmation(s.password, s.confirmPassword)

        if (nameError != null || phoneError != null ||
            passwordError != null || confirmError != null
        ) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    confirmError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val existing = repository.getByPhone(s.phone)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        phoneError = "Phone number is already registered"
                    )
                }
                return@launch
            }

            val newId = repository.generateNextPatientId()
            val newPatient = Patient(
                patientID = newId,
                phoneNumber = s.phone,
                name = s.fullName.trim(),
                password = s.password
            )
            repository.insert(newPatient)

            _uiState.update { it.copy(isLoading = false, generatedPatientId = newId) }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(generatedPatientId = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SignUpViewModel(container.patientRepository)
            }
        }
    }
}

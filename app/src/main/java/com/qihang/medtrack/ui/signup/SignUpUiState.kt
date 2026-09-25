package com.qihang.medtrack.ui.signup

data class SignUpUiState(
    val fullName: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val generatedPatientId: String? = null
)

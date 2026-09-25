package com.qihang.medtrack.ui.login

data class LoginUiState(
    val patientId: String = "",
    val password: String = "",
    val patientIdError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

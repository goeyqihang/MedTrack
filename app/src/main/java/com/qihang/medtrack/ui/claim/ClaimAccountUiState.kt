package com.qihang.medtrack.ui.claim

data class ClaimAccountUiState(
    val patientId: String = "",
    val phone: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val patientIdError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val claimSuccess: Boolean = false
)

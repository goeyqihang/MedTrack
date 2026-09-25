package com.qihang.medtrack.ui.clinicianlogin

data class ClinicianLoginUiState(
    val accessKey: String = "",
    val error: String? = null,
    val loginSuccess: Boolean = false
)

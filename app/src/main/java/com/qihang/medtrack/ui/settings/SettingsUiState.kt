package com.qihang.medtrack.ui.settings

data class SettingsUiState(
    val patientId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

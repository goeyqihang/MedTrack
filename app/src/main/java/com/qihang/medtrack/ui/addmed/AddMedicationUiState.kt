package com.qihang.medtrack.ui.addmed

data class AddMedicationUiState(
    val name: String = "",
    val dosageAmount: String = "",
    val dosageUnit: String = "mg",
    val frequency: String = "Once daily",
    val time: String = "",
    val type: String = "Tablet",
    val notes: String = "",

    val nameError: String? = null,
    val dosageError: String? = null,
    val timeError: String? = null,
    val generalError: String? = null,

    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false
) {
    companion object {
        val UNIT_OPTIONS = listOf("mg", "ml", "g")
        val FREQUENCY_OPTIONS =
            listOf("Once daily", "Twice daily", "Three times daily", "As needed")
        val TYPE_OPTIONS =
            listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")
    }
}

package com.qihang.medtrack.ui.symptoms

import com.qihang.medtrack.data.symptom.Symptom

data class SymptomsUiState(
    // form
    val category: String = CATEGORY_OPTIONS[0],
    val severity: Float = 1f,
    val notes: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val dateTimeError: String? = null,
    val generalError: String? = null,

    // history list
    val history: List<Symptom> = emptyList(),

    // async state
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false
) {
    companion object {
        val CATEGORY_OPTIONS = listOf(
            "Pain", "Nausea", "Dizziness", "Fatigue",
            "Headache", "Skin Reaction", "Other"
        )
        const val NOTES_MAX_LENGTH = 200
    }
}

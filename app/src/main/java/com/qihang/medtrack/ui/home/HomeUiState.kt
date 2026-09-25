package com.qihang.medtrack.ui.home

import com.qihang.medtrack.data.medication.Medication
import java.time.LocalDate

data class HomeUiState(
    val patientId: String = "",
    val patientName: String = "",
    /** The day the checklist belongs to; taken toggles are recorded against it. */
    val date: LocalDate? = null,
    val formattedDate: String = "",
    val medications: List<Medication> = emptyList(),
    val takenStates: Map<Int, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val totalMeds: Int get() = medications.size
    val takenCount: Int get() = takenStates.values.count { it }
}

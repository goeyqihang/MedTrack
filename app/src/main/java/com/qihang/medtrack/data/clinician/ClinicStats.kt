package com.qihang.medtrack.data.clinician

/** Aggregate statistics across all patients, shown on the Clinician Dashboard. */
data class ClinicStats(
    val totalPatients: Int,
    val totalMedications: Int,
    val avgMedicationsPerPatient: Double,
    val totalSymptoms: Int,
    val mostCommonSymptomCategory: String,
    val avgSymptomSeverity: Double
)

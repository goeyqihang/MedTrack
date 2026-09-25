package com.qihang.medtrack.data.clinician

class ClinicianRepository(private val clinicianDao: ClinicianDao) {

    /** Aggregate statistics across every patient in the database. */
    suspend fun getAggregateStats(): ClinicStats {
        val totals = clinicianDao.getTotals()

        val avgMeds = if (totals.totalPatients > 0) {
            totals.totalMedications.toDouble() / totals.totalPatients
        } else 0.0

        return ClinicStats(
            totalPatients = totals.totalPatients,
            totalMedications = totals.totalMedications,
            avgMedicationsPerPatient = avgMeds,
            totalSymptoms = totals.totalSymptoms,
            mostCommonSymptomCategory = totals.topCategory ?: "N/A",
            avgSymptomSeverity = totals.avgSeverity ?: 0.0
        )
    }
}

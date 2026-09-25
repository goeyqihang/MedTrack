package com.qihang.medtrack.data.clinician

import androidx.room.Dao
import androidx.room.Query

/** Raw dashboard totals, aggregated by SQLite rather than in memory. */
data class ClinicTotals(
    val totalPatients: Int,
    val totalMedications: Int,
    val totalSymptoms: Int,
    /** Null when no symptoms have been logged. */
    val avgSeverity: Double?,
    /** Null when no symptoms have been logged; ties go to the alphabetically first. */
    val topCategory: String?
)

@Dao
interface ClinicianDao {

    /** Computes every dashboard total in one statement, so they come from one snapshot. */
    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM patients) AS totalPatients,
            (SELECT COUNT(*) FROM medications) AS totalMedications,
            (SELECT COUNT(*) FROM symptoms) AS totalSymptoms,
            (SELECT AVG(severity) FROM symptoms) AS avgSeverity,
            (SELECT category FROM symptoms
                GROUP BY category
                ORDER BY COUNT(*) DESC, category
                LIMIT 1) AS topCategory
        """
    )
    suspend fun getTotals(): ClinicTotals
}

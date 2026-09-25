package com.qihang.medtrack.data.takenstatus

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.qihang.medtrack.data.medication.Medication

/**
 * Records whether a medication was marked "taken" on a given calendar day.
 * Composite primary key (medicationId + date) → one row per med per day,
 * which makes the daily reset automatic: a query for today's date simply
 * finds nothing for a brand-new day.
 */
@Entity(
    tableName = "taken_status",
    primaryKeys = ["medicationId", "date"],
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicationId")]
)
data class TakenStatus(
    val medicationId: Int,
    val date: String,   // ISO format, e.g. "2026-05-16"
    val taken: Boolean
)

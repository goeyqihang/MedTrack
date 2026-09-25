package com.qihang.medtrack.data.medcoachtip

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.qihang.medtrack.data.patient.Patient

@Entity(
    tableName = "med_coach_tips",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientID"],
        childColumns = ["patientID"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientID")]
)
data class MedCoachTip(
    val patientID: String,
    val tipText: String,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

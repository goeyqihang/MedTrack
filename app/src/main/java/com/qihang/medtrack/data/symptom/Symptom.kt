package com.qihang.medtrack.data.symptom

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.qihang.medtrack.data.patient.Patient

@Entity(
    tableName = "symptoms",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientID"],
        childColumns = ["patientID"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientID")]
)
data class Symptom(
    val patientID: String,
    val category: String,
    val severity: Int,
    val notes: String,
    val dateTime: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

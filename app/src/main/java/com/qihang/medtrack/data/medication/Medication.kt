package com.qihang.medtrack.data.medication

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.qihang.medtrack.data.patient.Patient

@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientID"],
        childColumns = ["patientID"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientID")]
)
data class Medication(
    val patientID: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val time: String,
    val type: String,
    val notes: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

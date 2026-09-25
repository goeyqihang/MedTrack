package com.qihang.medtrack.data.patient

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val patientID: String,
    val phoneNumber: String,
    val name: String,
    val password: String? = null
)

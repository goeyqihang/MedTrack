package com.qihang.medtrack.data.patient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Query("SELECT * FROM patients WHERE patientID = :patientID LIMIT 1")
    suspend fun getById(patientID: String): Patient?

    @Query("SELECT * FROM patients WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getByPhone(phoneNumber: String): Patient?

    @Query("SELECT patientID FROM patients")
    suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM patients WHERE patientID = :patientID AND phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getForClaim(patientID: String, phoneNumber: String): Patient?

    @Query("SELECT * FROM patients WHERE patientID = :patientID AND password = :password LIMIT 1")
    suspend fun login(patientID: String, password: String): Patient?

    @Query("UPDATE patients SET password = :password WHERE patientID = :patientID")
    suspend fun setPassword(patientID: String, password: String)
}

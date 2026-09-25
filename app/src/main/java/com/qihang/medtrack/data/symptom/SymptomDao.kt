package com.qihang.medtrack.data.symptom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptom: Symptom): Long

    @Query("SELECT * FROM symptoms WHERE patientID = :patientID ORDER BY dateTime DESC")
    fun observeByPatient(patientID: String): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE patientID = :patientID ORDER BY dateTime DESC")
    suspend fun getByPatient(patientID: String): List<Symptom>
}

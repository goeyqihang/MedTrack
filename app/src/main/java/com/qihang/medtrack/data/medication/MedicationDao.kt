package com.qihang.medtrack.data.medication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Query("SELECT * FROM medications WHERE patientID = :patientID ORDER BY time ASC")
    fun observeByPatient(patientID: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE patientID = :patientID")
    suspend fun getByPatient(patientID: String): List<Medication>
}

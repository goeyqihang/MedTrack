package com.qihang.medtrack.data.medcoachtip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tip: MedCoachTip): Long

    @Query("SELECT * FROM med_coach_tips WHERE patientID = :patientID ORDER BY timestamp DESC")
    fun observeByPatient(patientID: String): Flow<List<MedCoachTip>>
}

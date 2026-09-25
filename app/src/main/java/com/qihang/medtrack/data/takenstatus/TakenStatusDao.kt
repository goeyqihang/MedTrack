package com.qihang.medtrack.data.takenstatus

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TakenStatusDao {

    /** Insert or replace — composite PK means same (med, date) overwrites. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(status: TakenStatus)

    /** IDs of medications marked taken on [date]. */
    @Query("SELECT medicationId FROM taken_status WHERE date = :date AND taken = 1")
    fun observeTakenIdsOnDate(date: String): Flow<List<Int>>
}

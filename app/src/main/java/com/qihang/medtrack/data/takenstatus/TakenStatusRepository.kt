package com.qihang.medtrack.data.takenstatus

import kotlinx.coroutines.flow.Flow

class TakenStatusRepository(private val dao: TakenStatusDao) {

    fun observeTakenIdsOnDate(date: String): Flow<List<Int>> =
        dao.observeTakenIdsOnDate(date)

    suspend fun setTaken(medicationId: Int, date: String, taken: Boolean) =
        dao.upsert(TakenStatus(medicationId, date, taken))
}

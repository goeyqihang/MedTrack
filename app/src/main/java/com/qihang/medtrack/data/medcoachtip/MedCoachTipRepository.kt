package com.qihang.medtrack.data.medcoachtip

import kotlinx.coroutines.flow.Flow

class MedCoachTipRepository(private val dao: MedCoachTipDao) {

    fun observeByPatient(patientID: String): Flow<List<MedCoachTip>> =
        dao.observeByPatient(patientID)

    suspend fun insert(tip: MedCoachTip): Long = dao.insert(tip)
}

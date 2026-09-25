package com.qihang.medtrack.data.medication

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val dao: MedicationDao) {

    fun observeByPatient(patientID: String): Flow<List<Medication>> =
        dao.observeByPatient(patientID)

    suspend fun getByPatient(patientID: String): List<Medication> =
        dao.getByPatient(patientID)

    suspend fun insert(medication: Medication): Long = dao.insert(medication)
}

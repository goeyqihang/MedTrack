package com.qihang.medtrack.data.symptom

import kotlinx.coroutines.flow.Flow

class SymptomRepository(private val dao: SymptomDao) {

    fun observeByPatient(patientID: String): Flow<List<Symptom>> =
        dao.observeByPatient(patientID)

    suspend fun getByPatient(patientID: String): List<Symptom> =
        dao.getByPatient(patientID)

    suspend fun insert(symptom: Symptom): Long = dao.insert(symptom)
}

package com.qihang.medtrack.data.patient

class PatientRepository(private val patientDao: PatientDao) {

    suspend fun login(patientID: String, password: String): Patient? =
        patientDao.login(patientID, password)

    suspend fun getById(patientID: String): Patient? =
        patientDao.getById(patientID)

    suspend fun getForClaim(patientID: String, phoneNumber: String): Patient? =
        patientDao.getForClaim(patientID, phoneNumber)

    suspend fun setPassword(patientID: String, password: String) =
        patientDao.setPassword(patientID, password)

    suspend fun insert(patient: Patient) = patientDao.insert(patient)

    suspend fun getByPhone(phoneNumber: String): Patient? =
        patientDao.getByPhone(phoneNumber)

    suspend fun generateNextPatientId(): String {
        val existingIds = patientDao.getAllIds()
        val maxNum = existingIds
            .mapNotNull { it.removePrefix("P").toIntOrNull() }
            .maxOrNull() ?: 1000
        return "P${(maxNum + 1).toString().padStart(4, '0')}"
    }
}

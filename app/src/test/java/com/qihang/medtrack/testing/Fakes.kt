package com.qihang.medtrack.testing

import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationDao
import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.patient.PatientDao
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.data.takenstatus.TakenStatus
import com.qihang.medtrack.data.takenstatus.TakenStatusDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// In-memory stand-ins for the Room DAOs and the SharedPreferences session, so
// ViewModels can be tested on the JVM without an Android device.

class FakeSessionManager(override var loggedInPatientId: String? = null) : SessionManager {
    override fun login(patientId: String) {
        loggedInPatientId = patientId
    }

    override fun logout() {
        loggedInPatientId = null
    }
}

class FakePatientDao(vararg initial: Patient) : PatientDao {
    private val patients = initial.associateBy { it.patientID }.toMutableMap()

    override suspend fun insert(patient: Patient) {
        patients[patient.patientID] = patient
    }

    override suspend fun getById(patientID: String) = patients[patientID]

    override suspend fun getByPhone(phoneNumber: String) =
        patients.values.firstOrNull { it.phoneNumber == phoneNumber }

    override suspend fun getAllIds() = patients.keys.toList()

    override suspend fun getForClaim(patientID: String, phoneNumber: String) =
        patients[patientID]?.takeIf { it.phoneNumber == phoneNumber }

    override suspend fun login(patientID: String, password: String) =
        patients[patientID]?.takeIf { it.password == password }

    override suspend fun setPassword(patientID: String, password: String) {
        patients[patientID]?.let { patients[patientID] = it.copy(password = password) }
    }
}

class FakeMedicationDao(initial: List<Medication> = emptyList()) : MedicationDao {
    private val medications = MutableStateFlow(initial)

    override suspend fun insert(medication: Medication): Long {
        val id = (medications.value.maxOfOrNull { it.id } ?: 0) + 1
        medications.update { it + medication.copy(id = id) }
        return id.toLong()
    }

    override fun observeByPatient(patientID: String): Flow<List<Medication>> =
        medications.map { all -> all.filter { it.patientID == patientID }.sortedBy { it.time } }

    override suspend fun getByPatient(patientID: String) =
        medications.value.filter { it.patientID == patientID }
}

class FakeTakenStatusDao : TakenStatusDao {
    /** (medicationId, date) -> taken, mirroring the table's composite primary key. */
    private val rows = MutableStateFlow<Map<Pair<Int, String>, Boolean>>(emptyMap())

    override suspend fun upsert(status: TakenStatus) {
        rows.update { it + ((status.medicationId to status.date) to status.taken) }
    }

    override fun observeTakenIdsOnDate(date: String): Flow<List<Int>> =
        rows.map { all -> all.filter { (key, taken) -> key.second == date && taken }.map { it.key.first } }
}

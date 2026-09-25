package com.qihang.medtrack.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.core.content.edit
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.session.SharedPrefsSessionManager
import com.qihang.medtrack.data.symptom.Symptom

/**
 * Fills a newly created database with the sample CSVs from `assets/`, plus any data the
 * pre-Room version of the app stored as JSON in SharedPreferences.
 *
 * It runs inside Room's database-creation transaction, so no query can see the database
 * before seeding has finished — even a login fired the instant the app first opens.
 */
class DatabaseSeeder(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        readPatientsCsv().forEach { db.insertPatient(it) }
        readMedicationsCsv().forEach { db.insertMedication(it) }
        readSymptomsCsv().forEach { db.insertSymptom(it) }
        migrateLegacyData(db)
    }

    /** Moves patients and medications saved as JSON by the pre-Room version into [db]. */
    private fun migrateLegacyData(db: SupportSQLiteDatabase) {
        val prefs = context.getSharedPreferences(
            SharedPrefsSessionManager.PREFS_NAME, Context.MODE_PRIVATE
        )
        // Rows that fail a constraint are skipped; REPLACE resolves ID clashes with the CSVs.
        prefs.getString(LEGACY_KEY_USERS, null)?.let { json ->
            parseJsonList<Patient>(json).forEach { runCatching { db.insertPatient(it) } }
        }
        prefs.getString(LEGACY_KEY_MEDICATIONS, null)?.let { json ->
            parseJsonList<Medication>(json).forEach { runCatching { db.insertMedication(it) } }
        }
        prefs.edit {
            remove(LEGACY_KEY_USERS)
            remove(LEGACY_KEY_MEDICATIONS)
        }
    }

    private inline fun <reified T> parseJsonList(json: String): List<T> =
        runCatching { Gson().fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type) }
            .getOrNull()
            .orEmpty()

    // Room's DAOs cannot be used while the database is still being created, so these
    // insert directly. Column names match the entity property names.

    private fun SupportSQLiteDatabase.insertPatient(patient: Patient) {
        insert(
            "patients", SQLiteDatabase.CONFLICT_REPLACE, contentValuesOf(
                "patientID" to patient.patientID,
                "phoneNumber" to patient.phoneNumber,
                "name" to patient.name,
                "password" to patient.password
            )
        )
    }

    private fun SupportSQLiteDatabase.insertMedication(medication: Medication) {
        insert(
            "medications", SQLiteDatabase.CONFLICT_REPLACE, contentValuesOf(
                "patientID" to medication.patientID,
                "name" to medication.name,
                "dosage" to medication.dosage,
                "frequency" to medication.frequency,
                "time" to medication.time,
                "type" to medication.type,
                "notes" to medication.notes
            )
        )
    }

    private fun SupportSQLiteDatabase.insertSymptom(symptom: Symptom) {
        insert(
            "symptoms", SQLiteDatabase.CONFLICT_REPLACE, contentValuesOf(
                "patientID" to symptom.patientID,
                "category" to symptom.category,
                "severity" to symptom.severity,
                "notes" to symptom.notes,
                "dateTime" to symptom.dateTime
            )
        )
    }

    private fun readPatientsCsv(): List<Patient> =
        readCsv("patients.csv", minColumns = 3) { cols ->
            Patient(
                patientID = cols[0],
                phoneNumber = cols[1],
                name = cols[2],
                password = null
            )
        }

    private fun readMedicationsCsv(): List<Medication> =
        readCsv("medications.csv", minColumns = 7) { cols ->
            Medication(
                patientID = cols[0],
                name = cols[1],
                dosage = cols[2],
                frequency = cols[3],
                time = cols[4],
                type = cols[5],
                notes = cols[6]
            )
        }

    private fun readSymptomsCsv(): List<Symptom> =
        readCsv("symptoms.csv", minColumns = 5) { cols ->
            Symptom(
                patientID = cols[0],
                category = cols[1],
                severity = cols[2].toIntOrNull() ?: 0,
                notes = cols[3],
                dateTime = cols[4]
            )
        }

    /** Parses [fileName] from assets, skipping the header and rows with too few columns. */
    private fun <T> readCsv(fileName: String, minColumns: Int, toRow: (List<String>) -> T): List<T> =
        runCatching {
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.drop(1)
                    .map { line -> line.split(",").map { it.trim() } }
                    .filter { it.size >= minColumns }
                    .map(toRow)
                    .toList()
            }
        }.getOrDefault(emptyList())

    private companion object {
        // Keys used by the pre-Room version of the app
        const val LEGACY_KEY_USERS = "users"
        const val LEGACY_KEY_MEDICATIONS = "medications"
    }
}

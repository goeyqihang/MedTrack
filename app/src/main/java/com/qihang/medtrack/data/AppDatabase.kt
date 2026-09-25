package com.qihang.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qihang.medtrack.data.clinician.ClinicianDao
import com.qihang.medtrack.data.medcoachtip.MedCoachTip
import com.qihang.medtrack.data.medcoachtip.MedCoachTipDao
import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationDao
import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.patient.PatientDao
import com.qihang.medtrack.data.symptom.Symptom
import com.qihang.medtrack.data.symptom.SymptomDao
import com.qihang.medtrack.data.takenstatus.TakenStatus
import com.qihang.medtrack.data.takenstatus.TakenStatusDao

@Database(
    entities = [
        Patient::class,
        Medication::class,
        Symptom::class,
        MedCoachTip::class,
        TakenStatus::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun medCoachTipDao(): MedCoachTipDao
    abstract fun takenStatusDao(): TakenStatusDao
    abstract fun clinicianDao(): ClinicianDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `taken_status` (" +
                        "`medicationId` INTEGER NOT NULL, " +
                        "`date` TEXT NOT NULL, " +
                        "`taken` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`medicationId`, `date`), " +
                        "FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_taken_status_medicationId` " +
                        "ON `taken_status` (`medicationId`)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medtrack.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseSeeder(context.applicationContext))
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

package com.qihang.medtrack

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.qihang.medtrack.data.AppDatabase
import com.qihang.medtrack.data.clinician.ClinicianRepository
import com.qihang.medtrack.data.drug.DrugRepository
import com.qihang.medtrack.data.drug.OpenFdaClient
import com.qihang.medtrack.data.genai.GeminiClient
import com.qihang.medtrack.data.genai.GenAiRepository
import com.qihang.medtrack.data.medcoachtip.MedCoachTipRepository
import com.qihang.medtrack.data.medication.MedicationRepository
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.data.session.SharedPrefsSessionManager
import com.qihang.medtrack.data.symptom.SymptomRepository
import com.qihang.medtrack.data.takenstatus.TakenStatusRepository

/**
 * App-wide dependencies, created once and shared (manual dependency injection).
 * ViewModel factories take what they need from here instead of building their own.
 */
interface AppContainer {
    val sessionManager: SessionManager
    val patientRepository: PatientRepository
    val medicationRepository: MedicationRepository
    val takenStatusRepository: TakenStatusRepository
    val symptomRepository: SymptomRepository
    val medCoachTipRepository: MedCoachTipRepository
    val clinicianRepository: ClinicianRepository
    val drugRepository: DrugRepository
    val genAiRepository: GenAiRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    override val sessionManager: SessionManager by lazy { SharedPrefsSessionManager(context) }
    override val patientRepository by lazy { PatientRepository(database.patientDao()) }
    override val medicationRepository by lazy { MedicationRepository(database.medicationDao()) }
    override val takenStatusRepository by lazy { TakenStatusRepository(database.takenStatusDao()) }
    override val symptomRepository by lazy { SymptomRepository(database.symptomDao()) }
    override val medCoachTipRepository by lazy { MedCoachTipRepository(database.medCoachTipDao()) }
    override val clinicianRepository by lazy { ClinicianRepository(database.clinicianDao()) }
    override val drugRepository by lazy { DrugRepository(OpenFdaClient.api) }
    override val genAiRepository by lazy {
        GenAiRepository(GeminiClient.api, BuildConfig.GEMINI_API_KEY)
    }
}

/** The app's [AppContainer], for use inside a `viewModelFactory { initializer { … } }` block. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as MedTrackApplication).container

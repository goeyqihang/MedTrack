package com.qihang.medtrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationRepository
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.data.takenstatus.TakenStatusRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeViewModel(
    private val patientRepo: PatientRepository,
    private val medicationRepo: MedicationRepository,
    private val takenStatusRepo: TakenStatusRepository,
    session: SessionManager,
    /** The day whose checklist is shown; replaceable in tests. */
    private val currentDate: Flow<LocalDate> = dailyDates()
) : ViewModel() {

    private val patientId: String? = session.loggedInPatientId

    /**
     * Only collected while the screen is visible. Collection restarts when the user comes
     * back, so the date is re-read after a night in the background; while the screen is
     * open, [currentDate] ticks over at midnight.
     */
    val uiState: StateFlow<HomeUiState> = if (patientId == null) {
        MutableStateFlow(
            HomeUiState(isLoading = false, error = "User not found. Please log in again.")
        ).asStateFlow()
    } else {
        observeHome(patientId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
    }

    fun onTakenChange(medicationId: Int, taken: Boolean) {
        // Record against the day on screen, so the switch updates where the user sees it
        val date = uiState.value.date ?: return
        viewModelScope.launch {
            takenStatusRepo.setTaken(medicationId, date.toString(), taken)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHome(patientId: String): Flow<HomeUiState> {
        val patientName = flow { emit(patientRepo.getById(patientId)?.name ?: "Guest") }

        // Switching to a new day swaps in that day's (initially empty) taken statuses
        val checklist = currentDate.flatMapLatest { date ->
            combine(
                medicationRepo.observeByPatient(patientId),
                takenStatusRepo.observeTakenIdsOnDate(date.toString())
            ) { meds, takenIds -> DayChecklist(date, meds, takenIds.toSet()) }
        }

        return combine(patientName, checklist) { name, day ->
            HomeUiState(
                patientId = patientId,
                patientName = name,
                date = day.date,
                formattedDate = day.date.format(DATE_FORMAT),
                medications = day.medications,
                takenStates = day.medications.associate { it.id to (it.id in day.takenIds) },
                isLoading = false
            )
        }
    }

    private data class DayChecklist(
        val date: LocalDate,
        val medications: List<Medication>,
        val takenIds: Set<Int>
    )

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                HomeViewModel(
                    patientRepo = container.patientRepository,
                    medicationRepo = container.medicationRepository,
                    takenStatusRepo = container.takenStatusRepository,
                    session = container.sessionManager
                )
            }
        }
    }
}

/** Emits today's date now, and again each time the clock passes midnight. */
private fun dailyDates(): Flow<LocalDate> = flow {
    while (true) {
        val today = LocalDate.now()
        emit(today)
        val untilMidnight = Duration.between(LocalDateTime.now(), today.plusDays(1).atStartOfDay())
        delay(untilMidnight.toMillis() + 1_000) // +1 s so the clock is safely past midnight
    }
}.distinctUntilChanged()

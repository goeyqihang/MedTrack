package com.qihang.medtrack.ui.home

import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationRepository
import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.data.takenstatus.TakenStatusRepository
import com.qihang.medtrack.testing.FakeMedicationDao
import com.qihang.medtrack.testing.FakePatientDao
import com.qihang.medtrack.testing.FakeSessionManager
import com.qihang.medtrack.testing.FakeTakenStatusDao
import com.qihang.medtrack.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 22)
    private val dates = MutableStateFlow(today)

    private val lisinopril = medication(id = 1, name = "Lisinopril", time = "07:00")
    private val metformin = medication(id = 2, name = "Metformin", time = "08:00")

    private fun createViewModel(patientId: String? = "P1001") = HomeViewModel(
        patientRepo = PatientRepository(FakePatientDao(Patient("P1001", "0412345678", "John Smith"))),
        medicationRepo = MedicationRepository(FakeMedicationDao(listOf(metformin, lisinopril))),
        takenStatusRepo = TakenStatusRepository(FakeTakenStatusDao()),
        session = FakeSessionManager(patientId),
        currentDate = dates
    )

    /** uiState only runs while observed (like the screen does), so subscribe for the test. */
    private fun TestScope.observe(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `shows today's medications in schedule order with nothing taken`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("John Smith", state.patientName)
        assertEquals("Tuesday, 22 September 2026", state.formattedDate)
        assertEquals(listOf("Lisinopril", "Metformin"), state.medications.map { it.name })
        assertEquals(0, state.takenCount)
    }

    @Test
    fun `toggling a medication updates its taken state`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)

        viewModel.onTakenChange(metformin.id, taken = true)
        assertEquals(mapOf(lisinopril.id to false, metformin.id to true), viewModel.uiState.value.takenStates)
        assertEquals(1, viewModel.uiState.value.takenCount)

        viewModel.onTakenChange(metformin.id, taken = false)
        assertEquals(0, viewModel.uiState.value.takenCount)
    }

    @Test
    fun `a new day starts with an empty checklist and records toggles against it`() = runTest {
        val viewModel = createViewModel()
        observe(viewModel)
        viewModel.onTakenChange(metformin.id, taken = true)

        dates.value = today.plusDays(1) // midnight passes

        val tomorrow = viewModel.uiState.value
        assertEquals(today.plusDays(1), tomorrow.date)
        assertEquals(0, tomorrow.takenCount)

        viewModel.onTakenChange(lisinopril.id, taken = true)
        assertEquals(mapOf(lisinopril.id to true, metformin.id to false), viewModel.uiState.value.takenStates)
    }

    @Test
    fun `missing session shows an error instead of loading forever`() = runTest {
        val state = createViewModel(patientId = null).uiState.value

        assertFalse(state.isLoading)
        assertEquals("User not found. Please log in again.", state.error)
    }

    private fun medication(id: Int, name: String, time: String) = Medication(
        patientID = "P1001",
        name = name,
        dosage = "10mg",
        frequency = "Once daily",
        time = time,
        type = "Tablet",
        notes = "",
        id = id
    )
}

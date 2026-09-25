package com.qihang.medtrack.ui.login

import com.qihang.medtrack.data.patient.Patient
import com.qihang.medtrack.data.patient.PatientRepository
import com.qihang.medtrack.testing.FakePatientDao
import com.qihang.medtrack.testing.FakeSessionManager
import com.qihang.medtrack.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = FakeSessionManager()

    private fun createViewModel() = LoginViewModel(
        repository = PatientRepository(
            FakePatientDao(Patient("P1001", "0412345678", "John Smith", password = "Medtrack2026"))
        ),
        session = session
    )

    @Test
    fun `blank fields show field errors and do not log in`() {
        val viewModel = createViewModel()

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("PatientID cannot be empty", state.patientIdError)
        assertEquals("Password cannot be empty", state.passwordError)
        assertNull(session.loggedInPatientId)
    }

    @Test
    fun `editing a field clears only that field's error`() {
        val viewModel = createViewModel()
        viewModel.login()

        viewModel.onPatientIdChange("P1001")

        assertNull(viewModel.uiState.value.patientIdError)
        assertEquals("Password cannot be empty", viewModel.uiState.value.passwordError)
    }

    @Test
    fun `wrong password shows a general error`() {
        val viewModel = createViewModel()
        viewModel.onPatientIdChange("P1001")
        viewModel.onPasswordChange("NotMyPassword1")

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Invalid PatientID or password", state.generalError)
        assertFalse(state.isLoading)
        assertFalse(state.loginSuccess)
        assertNull(session.loggedInPatientId)
    }

    @Test
    fun `correct credentials start a session and signal success`() {
        val viewModel = createViewModel()
        viewModel.onPatientIdChange("  P1001 ") // surrounding spaces are trimmed
        viewModel.onPasswordChange("Medtrack2026")

        viewModel.login()

        assertTrue(viewModel.uiState.value.loginSuccess)
        assertEquals("P1001", session.loggedInPatientId)
    }
}

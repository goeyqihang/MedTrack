package com.qihang.medtrack.ui.medcoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qihang.medtrack.appContainer
import com.qihang.medtrack.data.drug.DrugRepository
import com.qihang.medtrack.data.drug.DrugSearchResult
import com.qihang.medtrack.data.genai.GenAiRepository
import com.qihang.medtrack.data.genai.GenAiResult
import com.qihang.medtrack.data.medcoachtip.MedCoachTip
import com.qihang.medtrack.data.medcoachtip.MedCoachTipRepository
import com.qihang.medtrack.data.medication.Medication
import com.qihang.medtrack.data.medication.MedicationRepository
import com.qihang.medtrack.data.session.SessionManager
import com.qihang.medtrack.data.symptom.Symptom
import com.qihang.medtrack.data.symptom.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedCoachViewModel(
    private val drugRepo: DrugRepository,
    private val medicationRepo: MedicationRepository,
    private val symptomRepo: SymptomRepository,
    private val genAiRepo: GenAiRepository,
    private val tipRepo: MedCoachTipRepository,
    session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedCoachUiState())
    val uiState: StateFlow<MedCoachUiState> = _uiState.asStateFlow()

    private val patientId: String? = session.loggedInPatientId

    init {
        loadMedicationNames()
        observeTips()
    }

    // ── Drug Information ─────────────────────────────────────────────

    private fun loadMedicationNames() {
        val id = patientId ?: return
        viewModelScope.launch {
            medicationRepo.observeByPatient(id).collect { meds ->
                _uiState.update {
                    it.copy(medicationNames = meds.map { m -> m.name }.distinct())
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(drugQuery = value, drugError = null) }
    }

    fun searchDrug() {
        val query = _uiState.value.drugQuery
        if (query.isBlank()) {
            _uiState.update { it.copy(drugError = "Please enter a medication name.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, drugError = null, drugInfo = null) }

            when (val result = drugRepo.searchDrug(query)) {
                is DrugSearchResult.Success -> _uiState.update {
                    it.copy(isSearching = false, drugInfo = result.info, drugError = null)
                }
                is DrugSearchResult.NotFound -> _uiState.update {
                    it.copy(
                        isSearching = false,
                        drugInfo = null,
                        drugError = "No information found for \"$query\"."
                    )
                }
                is DrugSearchResult.Error -> _uiState.update {
                    it.copy(isSearching = false, drugInfo = null, drugError = result.message)
                }
            }
        }
    }

    // ── GenAI Tips ───────────────────────────────────────────────────

    private fun observeTips() {
        val id = patientId ?: return
        viewModelScope.launch {
            tipRepo.observeByPatient(id).collect { tips ->
                _uiState.update { it.copy(allTips = tips) }
            }
        }
    }

    fun generateTip() {
        val id = patientId
        if (id == null) {
            _uiState.update { it.copy(tipError = "Session expired. Please log in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingTip = true, tipError = null) }

            // Pull this patient's data so the AI tip is personalised
            val meds = medicationRepo.getByPatient(id)
            val symptoms = symptomRepo.getByPatient(id)
            val prompt = buildPersonalisedPrompt(meds, symptoms)

            when (val result = genAiRepo.generate(prompt)) {
                is GenAiResult.Success -> {
                    tipRepo.insert(
                        MedCoachTip(
                            patientID = id,
                            tipText = result.text,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    _uiState.update {
                        it.copy(isGeneratingTip = false, latestTip = result.text, tipError = null)
                    }
                }
                is GenAiResult.Error -> _uiState.update {
                    it.copy(isGeneratingTip = false, tipError = result.message)
                }
            }
        }
    }

    /**
     * Builds a prompt that includes the patient's medication list and recent
     * symptom history so Gemini produces a personalised, specific tip.
     */
    private fun buildPersonalisedPrompt(
        meds: List<Medication>,
        symptoms: List<Symptom>
    ): String {
        val medText = if (meds.isEmpty()) {
            "The patient has not added any medications yet."
        } else {
            "Medications:\n" + meds.joinToString("\n") { m ->
                "- ${m.name} ${m.dosage}, ${m.frequency} at ${m.time}"
            }
        }

        val symptomText = if (symptoms.isEmpty()) {
            "No symptoms have been logged."
        } else {
            // include only the 5 most recent
            "Recent symptoms:\n" + symptoms.take(5).joinToString("\n") { s ->
                "- ${s.category} (severity ${s.severity}/10)"
            }
        }

        return """
            You are a friendly medication adherence coach.
            Write a short (2-3 sentences) encouraging, personalised message that
            helps this patient stay on top of their medication schedule.
            Refer to their situation where relevant, stay warm and supportive,
            and do NOT give medical advice or diagnose.

            $medText

            $symptomText
        """.trimIndent()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                MedCoachViewModel(
                    drugRepo = container.drugRepository,
                    medicationRepo = container.medicationRepository,
                    symptomRepo = container.symptomRepository,
                    genAiRepo = container.genAiRepository,
                    tipRepo = container.medCoachTipRepository,
                    session = container.sessionManager
                )
            }
        }
    }
}

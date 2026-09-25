package com.qihang.medtrack.ui.cliniciandashboard

import com.qihang.medtrack.data.clinician.ClinicStats

data class ClinicianDashboardUiState(
    // ── Aggregate statistics ─────────────────────────────────────────
    val stats: ClinicStats? = null,
    val isLoadingStats: Boolean = true,
    val statsError: String? = null,

    // ── GenAI-powered insights ───────────────────────────────────────
    val isGeneratingInsights: Boolean = false,
    val insights: List<String> = emptyList(),
    val insightsError: String? = null
)

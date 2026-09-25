package com.qihang.medtrack.ui.symptomtrend

/** One plotted point: a symptom's severity and a short date label. */
data class TrendPoint(
    val severity: Int,
    val label: String
)

data class SymptomTrendUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Distinct categories available for filtering. */
    val categories: List<String> = emptyList(),
    /** Currently selected category filter; null = all categories. */
    val selectedCategory: String? = null,
    /** Chart points after applying the filter, in chronological order. */
    val points: List<TrendPoint> = emptyList(),
    val averageSeverity: Double = 0.0
) {
    val entryCount: Int get() = points.size
}

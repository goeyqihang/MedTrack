package com.qihang.medtrack.data.drug

/**
 * Clean UI-facing model. Each field is the first element of the
 * corresponding OpenFDA string array, or "Not available" if missing.
 */
data class DrugInfo(
    val name: String,
    val purpose: String,
    val warnings: String,
    val dosage: String,
    val activeIngredient: String
)

/** Result of a drug lookup — success, not found, or an error. */
sealed interface DrugSearchResult {
    data class Success(val info: DrugInfo) : DrugSearchResult
    data object NotFound : DrugSearchResult
    data class Error(val message: String) : DrugSearchResult
}

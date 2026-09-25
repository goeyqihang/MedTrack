package com.qihang.medtrack.data.drug

import com.google.gson.annotations.SerializedName

/**
 * Top-level JSON shape returned by https://api.fda.gov/drug/label.json
 * Only the fields we care about are declared; Gson ignores the rest.
 */
data class DrugLabelResponse(
    @SerializedName("results") val results: List<DrugLabelResult>?
)

data class DrugLabelResult(
    @SerializedName("purpose") val purpose: List<String>?,
    @SerializedName("warnings") val warnings: List<String>?,
    @SerializedName("dosage_and_administration") val dosageAndAdministration: List<String>?,
    @SerializedName("active_ingredient") val activeIngredient: List<String>?,
    @SerializedName("indications_and_usage") val indicationsAndUsage: List<String>?,
    @SerializedName("openfda") val openfda: OpenFda?
)

data class OpenFda(
    @SerializedName("brand_name") val brandName: List<String>?,
    @SerializedName("generic_name") val genericName: List<String>?
)

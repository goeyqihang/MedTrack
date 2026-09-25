package com.qihang.medtrack.data.drug

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class DrugRepository(private val api: OpenFdaApi = OpenFdaClient.api) {

    /**
     * Searches the OpenFDA drug label database for [query], matching either
     * the brand name or the generic name.
     */
    suspend fun searchDrug(query: String): DrugSearchResult {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return DrugSearchResult.Error("Please enter a medication name.")
        }

        return try {
            val search =
                "openfda.brand_name:\"$trimmed\" OR openfda.generic_name:\"$trimmed\""
            val response = api.searchDrugLabel(search = search, limit = 1)
            val result = response.results?.firstOrNull()

            if (result == null) {
                DrugSearchResult.NotFound
            } else {
                DrugSearchResult.Success(result.toDrugInfo(trimmed))
            }
        } catch (e: HttpException) {
            // OpenFDA returns 404 when no records match the query
            if (e.code() == 404) {
                DrugSearchResult.NotFound
            } else {
                DrugSearchResult.Error("Server error (${e.code()}). Please try again.")
            }
        } catch (e: IOException) {
            DrugSearchResult.Error("Network unavailable. Check your connection.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DrugSearchResult.Error("Something went wrong. Please try again.")
        }
    }

    /** Maps a raw API result to the clean [DrugInfo] model. */
    private fun DrugLabelResult.toDrugInfo(fallbackName: String): DrugInfo {
        fun List<String>?.firstOr(default: String = "Not available"): String =
            this?.firstOrNull()?.takeIf { it.isNotBlank() } ?: default

        val displayName = openfda?.brandName?.firstOrNull()
            ?: openfda?.genericName?.firstOrNull()
            ?: fallbackName.replaceFirstChar { it.uppercase() }

        return DrugInfo(
            name = displayName,
            purpose = purpose.firstOr(),
            warnings = warnings.firstOr(),
            dosage = dosageAndAdministration.firstOr(),
            activeIngredient = activeIngredient.firstOr()
        )
    }
}

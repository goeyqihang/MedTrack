package com.qihang.medtrack.data.drug

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    /**
     * GET https://api.fda.gov/drug/label.json?search=...&limit=1
     * The [search] value is built by the repository, e.g.
     *   openfda.brand_name:"ibuprofen"
     */
    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): DrugLabelResponse
}

package com.qihang.medtrack.data.drug

import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class DrugRepositoryTest {

    /** Returns [response] (or throws [error]) and records the search it was sent. */
    private class FakeOpenFdaApi(
        private val response: DrugLabelResponse = DrugLabelResponse(results = emptyList()),
        private val error: Exception? = null
    ) : OpenFdaApi {
        var lastSearch: String? = null

        override suspend fun searchDrugLabel(search: String, limit: Int): DrugLabelResponse {
            lastSearch = search
            error?.let { throw it }
            return response
        }
    }

    private fun label(
        brandName: String? = null,
        genericName: String? = null,
        purpose: String? = null
    ) = DrugLabelResult(
        purpose = purpose?.let { listOf(it) },
        warnings = null,
        dosageAndAdministration = listOf(""),
        activeIngredient = listOf("Ibuprofen 200 mg"),
        indicationsAndUsage = null,
        openfda = OpenFda(
            brandName = brandName?.let { listOf(it) },
            genericName = genericName?.let { listOf(it) }
        )
    )

    private fun httpError(code: Int) =
        HttpException(Response.error<DrugLabelResponse>(code, "".toResponseBody()))

    @Test
    fun `blank query is rejected without calling the API`() = runBlocking {
        val api = FakeOpenFdaApi()

        val result = DrugRepository(api).searchDrug("   ")

        assertTrue(result is DrugSearchResult.Error)
        assertNull(api.lastSearch)
    }

    @Test
    fun `searches brand and generic names with the trimmed query`() = runBlocking {
        val api = FakeOpenFdaApi()

        DrugRepository(api).searchDrug("  ibuprofen ")

        assertEquals(
            "openfda.brand_name:\"ibuprofen\" OR openfda.generic_name:\"ibuprofen\"",
            api.lastSearch
        )
    }

    @Test
    fun `maps the first label and fills in missing fields`() = runBlocking {
        val api = FakeOpenFdaApi(
            DrugLabelResponse(
                listOf(label(brandName = "Advil", genericName = "IBUPROFEN", purpose = "Pain reliever"))
            )
        )

        val info = (DrugRepository(api).searchDrug("ibuprofen") as DrugSearchResult.Success).info

        assertEquals("Advil", info.name)
        assertEquals("Pain reliever", info.purpose)
        assertEquals("Not available", info.warnings) // field absent
        assertEquals("Not available", info.dosage) // field blank
        assertEquals("Ibuprofen 200 mg", info.activeIngredient)
    }

    @Test
    fun `falls back to the capitalised query when the label has no names`() = runBlocking {
        val api = FakeOpenFdaApi(DrugLabelResponse(listOf(label())))

        val result = DrugRepository(api).searchDrug("ibuprofen")

        assertEquals("Ibuprofen", (result as DrugSearchResult.Success).info.name)
    }

    @Test
    fun `empty results and HTTP 404 both mean not found`() = runBlocking {
        val empty = DrugRepository(FakeOpenFdaApi()).searchDrug("xyz")
        val notFound = DrugRepository(FakeOpenFdaApi(error = httpError(404))).searchDrug("xyz")

        assertEquals(DrugSearchResult.NotFound, empty)
        assertEquals(DrugSearchResult.NotFound, notFound)
    }

    @Test
    fun `server and network failures become user-facing errors`() = runBlocking {
        val server = DrugRepository(FakeOpenFdaApi(error = httpError(500))).searchDrug("xyz")
        val network = DrugRepository(FakeOpenFdaApi(error = IOException())).searchDrug("xyz")

        assertEquals(DrugSearchResult.Error("Server error (500). Please try again."), server)
        assertEquals(DrugSearchResult.Error("Network unavailable. Check your connection."), network)
    }
}

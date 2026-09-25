package com.qihang.medtrack.data.genai

import android.util.Log
import com.qihang.medtrack.BuildConfig
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** Outcome of a GenAI text-generation request. */
sealed interface GenAiResult {
    data class Success(val text: String) : GenAiResult
    data class Error(val message: String) : GenAiResult
}

class GenAiRepository(
    private val api: GeminiApi = GeminiClient.api,
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    /** Sends [prompt] to Gemini and returns the generated text. */
    suspend fun generate(prompt: String): GenAiResult {

        if (apiKey.isBlank()) {
            return GenAiResult.Error("Gemini API key is missing.")
        }

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = api.generateContent(
                model = MODEL_NAME,
                apiKey = apiKey,
                request = request
            )
            val text = response.firstTextOrNull()
            if (text.isNullOrBlank()) {
                GenAiResult.Error("The AI returned an empty response. Please try again.")
            } else {
                GenAiResult.Success(text)
            }
        } catch (e: HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            Log.e(TAG, "Gemini HTTP ${e.code()}: $body")
            when (e.code()) {
                429 -> GenAiResult.Error(
                    "Rate limit reached. Please wait a moment and try again."
                )
                400 -> GenAiResult.Error("Bad request — check the API key or model name.")
                403 -> GenAiResult.Error("Access denied — the API key may be invalid.")
                else -> GenAiResult.Error("Server error (${e.code()}). Please try again.")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Gemini network error", e)
            GenAiResult.Error("Network unavailable. Check your connection.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini unknown error", e)
            GenAiResult.Error("Could not reach the AI service. Please try again.")
        }
    }

    companion object {
        private const val TAG = "GenAiRepository"
        private const val MODEL_NAME = "gemini-2.5-flash"
    }
}

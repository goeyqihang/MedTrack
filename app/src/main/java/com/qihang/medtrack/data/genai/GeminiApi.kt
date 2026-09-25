package com.qihang.medtrack.data.genai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApi {

    /**
     * POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
     * The API key is sent in the x-goog-api-key header.
     */
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

package com.qihang.medtrack.data.genai

import com.qihang.medtrack.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Lazily-built singleton Retrofit service for the Gemini REST API. */
object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val api: GeminiApi by lazy {
        // Full request/response logging in debug builds only, never the API key.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("x-goog-api-key")
        }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}

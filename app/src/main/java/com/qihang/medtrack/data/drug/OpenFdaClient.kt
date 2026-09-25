package com.qihang.medtrack.data.drug

import com.qihang.medtrack.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Lazily-built singleton Retrofit service for the OpenFDA API. */
object OpenFdaClient {

    private const val BASE_URL = "https://api.fda.gov/"

    val api: OpenFdaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFdaApi::class.java)
    }
}

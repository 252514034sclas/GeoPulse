package com.geopulse.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Workstation LAN IP for physical Android devices over Wi-Fi: 172.23.176.200
    // Emulator loopback alias: 10.0.2.2:8000
    // ADB reverse loopback: 127.0.0.1:8000
    var baseUrl: String = "http://172.23.176.200:8000/"
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            retrofitInstance = null
            apiServiceInstance = null
        }

    private var retrofitInstance: Retrofit? = null
    private var apiServiceInstance: GeoPulseApiService? = null

    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build().also { retrofitInstance = it }
        }
    }

    val service: GeoPulseApiService
        get() = apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: getRetrofit().create(GeoPulseApiService::class.java).also {
                apiServiceInstance = it
            }
        }
}

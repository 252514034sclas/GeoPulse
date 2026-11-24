package com.geopulse.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val DEFAULT_PRODUCTION_URL = "https://api.geopulse.app/"
    const val DEFAULT_DEV_LOOPBACK = "http://127.0.0.1:8000/"
    private const val PREFS_NETWORK = "geopulse_network_config"
    private const val KEY_BASE_URL = "configured_base_url"

    // Default to local development bridge; persists configured production HTTPS URLs
    var baseUrl: String = DEFAULT_DEV_LOOPBACK
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            retrofitInstance = null
            apiServiceInstance = null
        }

    fun initFromContext(context: android.content.Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NETWORK, android.content.Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_BASE_URL, null)
            if (!saved.isNullOrBlank()) {
                baseUrl = saved
            }
        } catch (_: Exception) {}
    }

    fun setAndPersistBaseUrl(context: android.content.Context, url: String) {
        baseUrl = url
        try {
            context.getSharedPreferences(PREFS_NETWORK, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BASE_URL, baseUrl)
                .apply()
        } catch (_: Exception) {}
    }

    @Volatile
    var authToken: String? = null

    private var retrofitInstance: Retrofit? = null
    private var apiServiceInstance: GeoPulseApiService? = null

    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                authToken?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
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

package com.geopulse.app.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoPulseApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthDto>

    @GET("api/v1/dashboard")
    suspend fun getDashboardSummary(): Response<DashboardSummaryDto>

    @GET("api/v1/fields")
    suspend fun getFields(): Response<List<FieldDto>>

    @POST("api/v1/fields/draw")
    suspend fun createDrawnField(
        @Query("name") name: String,
        @Query("crop_type") cropType: String,
        @Query("boundary_wkt") boundaryWkt: String,
        @Query("sowing_date") sowingDate: String? = null
    ): Response<FieldDto>

    @GET("api/v1/fields/{field_id}/irrigation")
    suspend fun getIrrigationRecommendation(
        @Path("field_id") fieldId: Int,
        @Query("irrigation_method") irrigationMethod: String = "drip"
    ): Response<IrrigationDto>

    @GET("api/v1/fields/{field_id}/sowing-recommendation")
    suspend fun getSowingRecommendation(
        @Path("field_id") fieldId: Int,
        @Query("season") season: String = "Kharif",
        @Query("soil_type") soilType: String = "Loam"
    ): Response<SowingDto>

    @GET("api/v1/satellite/health/{field_id}")
    suspend fun getSatelliteHealth(
        @Path("field_id") fieldId: Int
    ): Response<SatelliteRecordDto>

    @GET("api/v1/weather/current")
    suspend fun getCurrentWeather(): Response<WeatherDto>

    @GET("api/v1/weather/spray-window")
    suspend fun getSprayWindow(): Response<SprayWindowDto>

    @GET("api/v1/advisory/tasks")
    suspend fun getAdvisoryTasks(): Response<List<AdvisoryTaskDto>>

    @POST("api/v1/advisory/tasks/{task_id}/complete")
    suspend fun completeTask(
        @Path("task_id") taskId: Int
    ): Response<AdvisoryTaskDto>

    @GET("api/v1/mandi/rates")
    suspend fun getMandiRates(
        @Query("commodity") commodity: String? = null
    ): Response<List<MandiPriceDto>>
}

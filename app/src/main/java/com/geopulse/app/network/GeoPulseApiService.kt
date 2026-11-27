package com.geopulse.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoPulseApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("health")
    suspend fun checkHealth(): Response<HealthDto>

    @GET("api/v1/dashboard")
    suspend fun getDashboardSummary(
        @Query("field_id") fieldId: Int? = null
    ): Response<DashboardSummaryDto>

    @GET("api/v1/fields")
    suspend fun getFields(): Response<List<FieldDto>>

    @GET("api/v1/fields/geojson")
    suspend fun getFieldsGeoJson(): Response<GeoJsonFeatureCollectionDto>


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

    @GET("api/v1/satellite/moisture/{field_id}")
    suspend fun getSatelliteMoisture(
        @Path("field_id") fieldId: Int
    ): Response<SoilMoistureDto>

    @GET("api/v1/weather/current")
    suspend fun getCurrentWeather(
        @Query("field_id") fieldId: Int? = null,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<WeatherDto>

    @GET("api/v1/weather/spray-window")
    suspend fun getSprayWindow(
        @Query("field_id") fieldId: Int? = null,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<SprayWindowDto>

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

    @GET("api/v1/budget/summary")
    suspend fun getBudgetSummary(): Response<BudgetSummaryDto>

    // -------------------------------------------------------------
    // Intelligent Notification & Device Registration Endpoints
    // -------------------------------------------------------------

    @POST("api/v1/notifications/devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegisterRequestDto
    ): Response<DeviceRegisterResponseDto>

    @POST("api/v1/notifications/devices/unregister")
    suspend fun unregisterDevice(
        @Body request: DeviceUnregisterRequestDto
    ): Response<Map<String, Any>>

    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("category") category: String? = null,
        @Query("field_id") fieldId: Int? = null,
        @Query("unread_only") unreadOnly: Boolean = false,
        @Query("limit") limit: Int = 50
    ): Response<List<NotificationDto>>

    @GET("api/v1/notifications/{id}")
    suspend fun getNotificationDetail(
        @Path("id") id: Int
    ): Response<NotificationDto>

    @POST("api/v1/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: Int
    ): Response<Map<String, Any>>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Map<String, Any>>

    @GET("api/v1/notifications/preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferenceDto>

    @POST("api/v1/notifications/evaluate")
    suspend fun triggerNotificationEvaluation(
        @Query("field_id") fieldId: Int? = null,
        @Query("bypass_quiet_hours") bypassQuiet: Boolean = true
    ): Response<Map<String, Any>>
}

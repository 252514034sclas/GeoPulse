package com.geopulse.app.network

import com.google.gson.annotations.SerializedName

// -------------------------------------------------------------
// Health
// -------------------------------------------------------------
data class HealthDto(
    val status: String,
    val version: String,
    val timestamp: String
)

// -------------------------------------------------------------
// Fields & Land Marking
// -------------------------------------------------------------
data class FieldDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    val name: String,
    @SerializedName("crop_type") val cropType: String,
    @SerializedName("area_acres") val areaAcres: Double,
    @SerializedName("sowing_date") val sowingDate: String?,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val boundary: String?
)

// -------------------------------------------------------------
// Weather & Spray Window
// -------------------------------------------------------------
data class WeatherDto(
    val id: Int,
    val location: String,
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("humidity_percent") val humidityPercent: Double,
    @SerializedName("precipitation_mm") val precipitationMm: Double,
    @SerializedName("wind_speed_kmh") val windSpeedKmh: Double,
    @SerializedName("spray_safe") val spraySafe: Boolean,
    @SerializedName("spray_advice") val sprayAdvice: String,
    @SerializedName("status_message") val statusMessage: String
)

data class SprayWindowDto(
    @SerializedName("is_safe_now") val isSafeNow: Boolean,
    @SerializedName("current_wind_kmh") val currentWindKmh: Double,
    @SerializedName("current_humidity_pct") val currentHumidityPct: Double,
    val verdict: String,
    @SerializedName("recommended_hours") val recommendedHours: List<String>,
    val cautions: List<String>
)

// -------------------------------------------------------------
// FAO-56 Irrigation Intelligence (mm & Litres)
// -------------------------------------------------------------
data class IrrigationDto(
    @SerializedName("field_area_ha") val fieldAreaHa: Double,
    @SerializedName("field_area_acres") val fieldAreaAcres: Double,
    val crop: String,
    @SerializedName("growth_stage") val growthStage: String,
    @SerializedName("daily_crop_water_demand_mm") val dailyCropWaterDemandMm: Double,
    @SerializedName("current_moisture_pct") val currentMoisturePct: Double,
    @SerializedName("irrigation_recommendation") val irrigationRecommendation: String,
    @SerializedName("action_explanation") val actionExplanation: String,
    @SerializedName("recommended_timing") val recommendedTiming: String,
    @SerializedName("recommended_depth_mm") val recommendedDepthMm: Double,
    @SerializedName("total_water_volume_litres") val totalWaterVolumeLitres: Long,
    @SerializedName("total_water_volume_m3") val totalWaterVolumeM3: Double,
    val confidence: String
)

// -------------------------------------------------------------
// AI-Powered Sowing & Seed Variety Recommendation
// -------------------------------------------------------------
data class SowingDto(
    @SerializedName("crop_name") val cropName: String,
    @SerializedName("field_area_ha") val fieldAreaHa: Double,
    @SerializedName("field_area_acres") val fieldAreaAcres: Double,
    @SerializedName("recommended_variety") val recommendedVariety: String,
    val season: String,
    val duration: String,
    @SerializedName("soil_suitability") val soilSuitability: String,
    @SerializedName("recommended_sowing_window") val recommendedSowingWindow: String,
    @SerializedName("soil_preparation_steps") val soilPreparationSteps: String,
    @SerializedName("seed_rate_per_ha_kg") val seedRatePerHaKg: Double,
    @SerializedName("seed_rate_per_acre_kg") val seedRatePerAcreKg: Double,
    @SerializedName("total_seed_required_kg") val totalSeedRequiredKg: Double,
    @SerializedName("approx_bags_needed") val approxBagsNeeded: String,
    @SerializedName("planting_geometry") val plantingGeometry: String,
    @SerializedName("sowing_depth") val sowingDepth: String,
    @SerializedName("expected_yield_range") val expectedYieldRange: String,
    @SerializedName("total_water_requirement") val totalWaterRequirement: String,
    @SerializedName("authoritative_source") val authoritativeSource: String
)

// -------------------------------------------------------------
// Satellite Health (NDVI)
// -------------------------------------------------------------
data class SatelliteRecordDto(
    val id: Int,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("recorded_date") val recordedDate: String,
    @SerializedName("ndvi_value") val ndviValue: Double,
    @SerializedName("ndwi_value") val ndwiValue: Double,
    @SerializedName("chlorophyll_spad") val chlorophyllSpad: Double,
    @SerializedName("canopy_cover_pct") val canopyCoverPct: Double,
    @SerializedName("health_status") val healthStatus: String,
    @SerializedName("layman_advice") val laymanAdvice: String
)

// -------------------------------------------------------------
// Advisory Tasks
// -------------------------------------------------------------
data class AdvisoryTaskDto(
    val id: Int,
    @SerializedName("task_type") val taskType: String,
    val title: String,
    val description: String,
    @SerializedName("time_badge") val timeBadge: String,
    val priority: String,
    @SerializedName("is_completed") val isCompleted: Boolean
)

// -------------------------------------------------------------
// Mandi Market Rates
// -------------------------------------------------------------
data class MandiPriceDto(
    val id: Int,
    val commodity: String,
    @SerializedName("mandi_name") val mandiName: String,
    val state: String,
    @SerializedName("min_price") val minPrice: Double,
    @SerializedName("max_price") val maxPrice: Double,
    @SerializedName("modal_price") val modalPrice: Double,
    @SerializedName("price_unit") val priceUnit: String,
    @SerializedName("trend_pct") val trendPct: Double,
    @SerializedName("recorded_date") val recordedDate: String
)

// -------------------------------------------------------------
// Fertilizer Calculation Response
// -------------------------------------------------------------
data class FertilizerCalcDto(
    @SerializedName("crop_type") val cropType: String,
    @SerializedName("area_acres") val areaAcres: Double,
    @SerializedName("urea_total_kg") val ureaTotalKg: Double,
    @SerializedName("dap_total_kg") val dapTotalKg: Double,
    @SerializedName("mop_total_kg") val mopTotalKg: Double,
    @SerializedName("urea_per_acre_kg") val ureaPerAcreKg: Double,
    @SerializedName("dap_per_acre_kg") val dapPerAcreKg: Double,
    @SerializedName("mop_per_acre_kg") val mopPerAcreKg: Double,
    @SerializedName("safety_tips") val safetyTips: List<String>
)

// -------------------------------------------------------------
// Dashboard Summary
// -------------------------------------------------------------
data class DashboardSummaryDto(
    @SerializedName("farmer_name") val farmerName: String,
    @SerializedName("farm_name") val farmName: String,
    @SerializedName("total_fields") val totalFields: Int,
    @SerializedName("active_alerts_count") val activeAlertsCount: Int,
    @SerializedName("average_soil_moisture") val averageSoilMoisture: Double,
    @SerializedName("average_crop_vigor") val averageCropVigor: Double,
    @SerializedName("spraying_safe_today") val sprayingSafeToday: Boolean,
    val fields: List<FieldDto>,
    @SerializedName("todays_tasks") val todaysTasks: List<AdvisoryTaskDto>,
    val weather: WeatherDto?,
    @SerializedName("mandi_highlights") val mandiHighlights: List<MandiPriceDto>
)

package com.geopulse.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoPulseRepository(
    private val api: GeoPulseApiService = ApiClient.service
) {
    suspend fun getDashboardSummary(): DashboardSummaryDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getDashboardSummary()
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {
            // Live network call failed: return clear offline empty state
        }
        return@withContext DashboardSummaryDto(
            farmerName = "Farmer",
            farmName = "My Farm",
            totalFields = 0,
            activeAlertsCount = 0,
            averageSoilMoisture = 0.0,
            averageCropVigor = 0.0,
            sprayingSafeToday = false,
            fields = emptyList(),
            todaysTasks = emptyList(),
            weather = null,
            mandiHighlights = emptyList()
        )
    }

    suspend fun getFields(): List<FieldDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFields()
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}
        return@withContext emptyList()
    }

    suspend fun getFieldsGeoJson(): GeoJsonFeatureCollectionDto? = withContext(Dispatchers.IO) {
        try {
            val response = api.getFieldsGeoJson()
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}
        return@withContext null
    }

    suspend fun getSatelliteHealth(fieldId: Int = 1): SatelliteRecordDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getSatelliteHealth(fieldId)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}

        return@withContext SatelliteRecordDto(
            id = 0,
            fieldId = fieldId,
            recordedDate = "",
            ndviValue = 0.0,
            ndwiValue = 0.0,
            chlorophyllSpad = 0.0,
            canopyCoverPct = 0.0,
            healthStatus = "Data Unavailable",
            laymanAdvice = "Satellite observation is currently unavailable for this parcel. Please ensure live internet connectivity and retry.",
            disclaimer = "No Live Satellite Data"
        )
    }

    suspend fun getSatelliteMoisture(fieldId: Int = 1): SoilMoistureDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getSatelliteMoisture(fieldId)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}

        return@withContext SoilMoistureDto(
            id = 0,
            fieldId = fieldId,
            moisturePercentage = 0.0,
            depthCm = 30.0,
            status = "Data Unavailable",
            wateringAdvice = "Soil moisture data is temporarily unavailable. Check back shortly.",
            dataSource = "Unavailable"
        )
    }

    suspend fun getIrrigationRecommendation(fieldId: Int, method: String = "drip"): IrrigationDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getIrrigationRecommendation(fieldId, method)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}

        return@withContext IrrigationDto(
            fieldAreaHa = 0.0,
            fieldAreaAcres = 0.0,
            crop = "",
            growthStage = "",
            dailyCropWaterDemandMm = 0.0,
            currentMoisturePct = 0.0,
            irrigationRecommendation = "UNAVAILABLE",
            actionExplanation = "Unable to compute water balance. Satellite moisture and weather services are temporarily unreachable.",
            recommendedTiming = "Retry when network connection is established.",
            recommendedDepthMm = 0.0,
            totalWaterVolumeLitres = 0L,
            totalWaterVolumeM3 = 0.0,
            confidence = "N/A"
        )
    }

    suspend fun getSowingRecommendation(fieldId: Int, season: String = "Kharif"): SowingDto = withContext(Dispatchers.IO) {
        try {
            val response = api.getSowingRecommendation(fieldId, season)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}

        return@withContext SowingDto(
            cropName = "",
            fieldAreaHa = 0.0,
            fieldAreaAcres = 0.0,
            recommendedVariety = "Consult Local Extension",
            season = season,
            duration = "",
            soilSuitability = "Data unavailable offline.",
            recommendedSowingWindow = "Check local agricultural university calendar.",
            soilPreparationSteps = "Standard agronomic practices.",
            seedRatePerHaKg = 0.0,
            seedRatePerAcreKg = 0.0,
            totalSeedRequiredKg = 0.0,
            approxBagsNeeded = "0",
            plantingGeometry = "",
            sowingDepth = "",
            expectedYieldRange = "",
            totalWaterRequirement = "",
            authoritativeSource = "TNAU / ICAR"
        )
    }

    suspend fun createDrawnField(name: String, cropType: String, boundaryWkt: String): FieldDto? = withContext(Dispatchers.IO) {
        try {
            val response = api.createDrawnField(name, cropType, boundaryWkt)
            if (response.isSuccessful) return@withContext response.body()
        } catch (_: Exception) {}
        return@withContext null
    }

    suspend fun getMandiRates(commodity: String? = null): List<MandiPriceDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMandiRates(commodity)
            if (response.isSuccessful && response.body() != null) return@withContext response.body()!!
        } catch (_: Exception) {}

        return@withContext emptyList()
    }
}

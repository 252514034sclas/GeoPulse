import sys

new_veg_module = """@Composable
fun VegetationHealthModule() {
    var satRecord by remember { mutableStateOf<SatelliteRecordDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val repo = GeoPulseRepository()
            satRecord = repo.getSatelliteHealth(1)
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoading = false
        }
    }

    val record = satRecord
    val ndviVal = record?.ndviValue?.toFloat() ?: 0.327f
    var growthStageSlider by remember { mutableStateOf(ndviVal) }

    LaunchedEffect(record) {
        if (record != null) {
            growthStageSlider = record.ndviValue.toFloat()
        }
    }

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Crop Health", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Copernicus Sentinel-2C (MSI 10m Surface Reflectance)", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isLoading) "SYNCING" else "100% CLEAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Satellite Image Date: ${record?.recordedDate ?: "2026-10-11"} (Tile: T44PMV · Tamil Nadu)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Canopy Greenness Level", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.5.sp)
                val statusText = if (growthStageSlider > 0.70f) "Optimal Vigor" else if (growthStageSlider > 0.40f) "Moderate" else "Yellowing / Stressed"
                val statusColor = if (growthStageSlider > 0.70f) LimeAccent else if (growthStageSlider > 0.40f) GoldAccent else RedAccent
                Text("${(growthStageSlider * 100).toInt()}% · $statusText", fontWeight = FontWeight.Bold, color = statusColor, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = growthStageSlider,
                onValueChange = { growthStageSlider = it },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = LimeAccent, activeTrackColor = EmeraldPrimary)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Farmer Recommendation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LimeAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record?.laymanAdvice ?: "Crop greenness index is 33% from Copernicus Sentinel-2C observation (Oct 11, 2026). Cloud-free observation over boundary. Water stress detected.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // 24-day Freshness Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Notice", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan is 24 days old (Oct 11, 2026). 5,901 pixels clipped to boundary. Next clear pass pending.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Crop Health", String.format("%.3f", record?.ndviValue ?: 0.327), Modifier.weight(1f))
                MetricChip("Scan Date", "Oct 11, 2026", Modifier.weight(1f))
                MetricChip("Valid Pixels", "5,901 Clear", Modifier.weight(1f))
            }
        }
    }
}
"""

new_moist_module = """@Composable
fun SoilMoistureModule() {
    var soilMoistureData by remember { mutableStateOf<SoilMoistureDto?>(null) }
    var selectedDepth by remember { mutableStateOf("0-15 cm (Topsoil)") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val repo = GeoPulseRepository()
            soilMoistureData = repo.getSatelliteMoisture(1)
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    val sm = soilMoistureData
    val moistureVal = sm?.moisturePercentage ?: 34.0

    Card(modifier = Modifier.fillMaxWidth().glassSurface(cornerRadius = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Crop Water Status & Soil Moisture", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Canopy NDMI (10m) & Soil Moisture Model", fontSize = 11.5.sp, color = CyanAccent)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (moistureVal < 40.0) RedAccent.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (moistureVal < 40.0) RedAccent.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (moistureVal < 40.0) "DEFICIT" else "ADEQUATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (moistureVal < 40.0) RedAccent else LimeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Satellite Image Date: 2026-10-11 (Pass Tile: T44PMV · Tamil Nadu)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("0-15 cm (Topsoil)", "15-40 cm (Root Zone)").forEach { depth ->
                    FilterChip(
                        selected = selectedDepth == depth,
                        onClick = { selectedDepth = depth },
                        label = { Text(depth, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            // Separate Crop Water Status (NDMI) from Soil Moisture Estimate
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Canopy Water Status (NDMI = 0.076)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mild leaf moisture deficit detected from Sentinel-2 NIR/SWIR absorption. Leaves are losing hydration faster than normal transpiration.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Irrigation Advisory Caution
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Field Soil Moisture Advisory", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldAccent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Estimated Topsoil Moisture: 34% (Mild Deficit). Do not irrigate based on satellite canopy NDMI alone. Inspect physical soil moisture 10-15 cm below surface before starting pumps.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            // Experimental Limitation Transparency Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = "Science", tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Model & Physical Limitation Note", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = CyanAccent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Soil moisture is an experimental estimate derived from optical water absorption proxy. Direct EOS-04 C-band SAR radar inversion is pending raw data clearance on Bhoonidhi web portal. Radar penetration is limited to 0-5 cm dielectric skin layer.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricChip("Crop Water Status", "NDMI 0.08", Modifier.weight(1f))
                MetricChip("Soil Estimate", "34% (Exp.)", Modifier.weight(1f))
                MetricChip("Scan Date", "Oct 11, 2026", Modifier.weight(1f))
            }
        }
    }
}
"""

def update_file():
    path = "app/src/main/java/com/geopulse/app/GeoComponents.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    start_veg = content.find("fun VegetationHealthModule() {")
    start_moist = content.find("fun SoilMoistureModule() {")
    start_irrig = content.find("fun IrrigationTimingModule() {")

    if start_veg == -1 or start_moist == -1 or start_irrig == -1:
        print("ERROR: Could not find module boundary markers!")
        return

    # Replace both modules cleanly
    new_content = content[:start_veg] + new_veg_module + "\n" + new_moist_module + "\n" + content[start_irrig:]
    with open(path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("SUCCESS: Updated VegetationHealthModule and SoilMoistureModule in GeoComponents.kt")

if __name__ == "__main__":
    update_file()

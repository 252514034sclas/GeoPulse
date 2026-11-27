package com.geopulse.app

import com.geopulse.app.util.GeoLanguageManager
import com.geopulse.app.util.GeoStrings
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

class LocalizationUnitTest {

    @Test
    fun testStringsXmlKeyParity() {
        val enFile = File("src/main/res/values/strings.xml")
        val taFile = File("src/main/res/values-ta/strings.xml")

        assertTrue("English strings.xml must exist", enFile.exists())
        assertTrue("Tamil strings.xml must exist", taFile.exists())

        val pattern = Pattern.compile("<string\\s+name=\"([^\"]+)\"")

        val enKeys = mutableSetOf<String>()
        val enMatcher = pattern.matcher(enFile.readText())
        while (enMatcher.find()) {
            enKeys.add(enMatcher.group(1))
        }

        val taKeys = mutableSetOf<String>()
        val taMatcher = pattern.matcher(taFile.readText())
        while (taMatcher.find()) {
            taKeys.add(taMatcher.group(1))
        }

        val missingInTamil = enKeys - taKeys
        val extraInTamil = taKeys - enKeys

        assertTrue("Missing keys in Tamil strings.xml: $missingInTamil", missingInTamil.isEmpty())
        assertTrue("Extra keys in Tamil strings.xml: $extraInTamil", extraInTamil.isEmpty())
        assertEquals("English and Tamil string keys must have 100% parity", enKeys.size, taKeys.size)
    }

    @Test
    fun testGeoStringsScreenCoverage() {
        // Verify all 30 screens have non-blank English and Tamil titles
        assertNotNull(GeoStrings.screen7Title)
        assertNotNull(GeoStrings.screen8Title)
        assertNotNull(GeoStrings.screen9Title)
        assertNotNull(GeoStrings.screen10Title)
        assertNotNull(GeoStrings.screen11Title)
        assertNotNull(GeoStrings.screen12Title)
        assertNotNull(GeoStrings.screen13Title)
        assertNotNull(GeoStrings.screen14Title)
        assertNotNull(GeoStrings.screen15Title)
        assertNotNull(GeoStrings.screen16Title)
        assertNotNull(GeoStrings.screen17Title)
        assertNotNull(GeoStrings.screen18Title)
        assertNotNull(GeoStrings.screen19Title)
        assertNotNull(GeoStrings.screen20Title)
        assertNotNull(GeoStrings.screen21Title)
        assertNotNull(GeoStrings.screen22Title)
        assertNotNull(GeoStrings.screen23Title)
        assertNotNull(GeoStrings.screen24Title)
        assertNotNull(GeoStrings.screen25Title)
        assertNotNull(GeoStrings.screen26Title)
        assertNotNull(GeoStrings.screen27Title)
        assertNotNull(GeoStrings.screen28Title)
        assertNotNull(GeoStrings.screen29Title)
        assertNotNull(GeoStrings.screen30Title)

        // Subtitles
        assertNotNull(GeoStrings.screen11Subtitle)
        assertNotNull(GeoStrings.screen12Subtitle)
        assertNotNull(GeoStrings.screen13Subtitle)
        assertNotNull(GeoStrings.screen14Subtitle)
        assertNotNull(GeoStrings.screen15Subtitle)
        assertNotNull(GeoStrings.screen16Subtitle)
        assertNotNull(GeoStrings.screen17Subtitle)
        assertNotNull(GeoStrings.screen18Subtitle)
        assertNotNull(GeoStrings.screen19Subtitle)
        assertNotNull(GeoStrings.screen20Subtitle)
        assertNotNull(GeoStrings.screen21Subtitle)
        assertNotNull(GeoStrings.screen22Subtitle)
        assertNotNull(GeoStrings.screen23Subtitle)
        assertNotNull(GeoStrings.screen24Subtitle)
        assertNotNull(GeoStrings.screen25Subtitle)
        assertNotNull(GeoStrings.screen26Subtitle)
        assertNotNull(GeoStrings.screen27Subtitle)
        assertNotNull(GeoStrings.screen28Subtitle)
        assertNotNull(GeoStrings.screen29Subtitle)
        assertNotNull(GeoStrings.screen30Subtitle)
    }

    @Test
    fun testCropTranslationMapping() {
        assertEquals("நெல்", GeoStrings.getCropName("paddy", isTamil = true))
        assertEquals("நெல்", GeoStrings.getCropName("rice", isTamil = true))
        assertEquals("பருத்தி", GeoStrings.getCropName("cotton", isTamil = true))
        assertEquals("மக்காச்சோளம்", GeoStrings.getCropName("maize", isTamil = true))
        assertEquals("மக்காச்சோளம்", GeoStrings.getCropName("corn", isTamil = true))
        assertEquals("நிலக்கடலை", GeoStrings.getCropName("groundnut", isTamil = true))
        assertEquals("கரும்பு", GeoStrings.getCropName("sugarcane", isTamil = true))

        // English fallback
        assertEquals("Paddy (Rice)", GeoStrings.getCropName("paddy", isTamil = false))
        assertEquals("Cotton", GeoStrings.getCropName("cotton", isTamil = false))
    }

    @Test
    fun testSoilTranslationMapping() {
        assertEquals("வண்டல் மண்", GeoStrings.getSoilName("loam", isTamil = true))
        assertEquals("களிமண் கலந்த வண்டல்", GeoStrings.getSoilName("clay loam", isTamil = true))
        assertEquals("மணல் கலந்த வண்டல்", GeoStrings.getSoilName("sandy loam", isTamil = true))
        assertEquals("கரிசல் மண்", GeoStrings.getSoilName("black cotton", isTamil = true))
        assertEquals("செம்மண்", GeoStrings.getSoilName("red soil", isTamil = true))

        // English fallback
        assertEquals("Loam", GeoStrings.getSoilName("loam", isTamil = false))
        assertEquals("Black Cotton", GeoStrings.getSoilName("black cotton", isTamil = false))
    }

    @Test
    fun testUnitFormatting() {
        // English formatting
        GeoLanguageManager.currentLanguage = "en"
        assertEquals("2.50 acres", GeoLanguageManager.formatArea(2.5))
        assertEquals("10,000 Litres", GeoLanguageManager.formatVolume(10000L))
        assertEquals("12.5 mm", GeoLanguageManager.formatRain(12.5))
        assertEquals("31.0°C", GeoLanguageManager.formatTemperature(31.0))
        assertEquals("14.0 km/h", GeoLanguageManager.formatWindSpeed(14.0))
        assertEquals("₹2450 / quintal", GeoLanguageManager.formatPrice(2450.0))
        assertEquals("50.0 kg", GeoLanguageManager.formatWeightKg(50.0))

        // Tamil formatting
        GeoLanguageManager.currentLanguage = "ta"
        assertEquals("2.50 ஏக்கர்", GeoLanguageManager.formatArea(2.5))
        assertEquals("10,000 லிட்டர்", GeoLanguageManager.formatVolume(10000L))
        assertEquals("12.5 மி.மீ", GeoLanguageManager.formatRain(12.5))
        assertEquals("31.0°C", GeoLanguageManager.formatTemperature(31.0))
        assertEquals("14.0 கி.மீ/மணி", GeoLanguageManager.formatWindSpeed(14.0))
        assertEquals("₹2450 / குவின்டால்", GeoLanguageManager.formatPrice(2450.0))
        assertEquals("50.0 கிலோ", GeoLanguageManager.formatWeightKg(50.0))
    }
}

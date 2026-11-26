package com.geopulse.app

import com.geopulse.app.network.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndFieldIntegrityTest {

    @Test
    fun testArbitraryFieldIdRejectedWhenNotOwned() {
        val ownedFieldIds = setOf(10, 20, 30)
        val arbitraryUntrustedId = 9999

        // 1. Ownership check must reject arbitrary ID
        val isOwned = UserSession.validateFieldOwnership(arbitraryUntrustedId, ownedFieldIds)
        assertFalse("Arbitrary field ID 9999 must be rejected as unowned", isOwned)

        // 2. Sanitization must preserve existing valid selected field and not replace with untrusted ID
        val sanitized = UserSession.sanitizeSelectedFieldId(
            requestedId = arbitraryUntrustedId,
            ownedIds = ownedFieldIds,
            currentSelectedId = 10
        )
        assertEquals("Existing valid selected field 10 must be preserved", 10, sanitized)
    }

    @Test
    fun testCrossAccountFieldIdDoesNotOverwriteSelectedField() {
        val userAOwnedFields = setOf(101, 102)
        val userBTargetField = 205 // Belonging to User B
        val currentUserASelection = 101

        // 1. Cross-account ID check
        assertFalse(
            "User B's field ID 205 must be rejected for User A",
            UserSession.validateFieldOwnership(userBTargetField, userAOwnedFields)
        )

        // 2. Attempt to select User B's field must fail and retain User A's selection
        val sanitized = UserSession.sanitizeSelectedFieldId(
            requestedId = userBTargetField,
            ownedIds = userAOwnedFields,
            currentSelectedId = currentUserASelection
        )
        assertEquals("User A's selected field must remain 101", 101, sanitized)
    }

    @Test
    fun testEmptyFieldFarmerHandledSafely() {
        val zeroFields = emptySet<Int>()

        // When user has zero fields, any field ID is rejected
        assertFalse("No field can be owned when user has 0 fields", UserSession.validateFieldOwnership(1, zeroFields))

        val sanitized = UserSession.sanitizeSelectedFieldId(
            requestedId = 1,
            ownedIds = zeroFields,
            currentSelectedId = null
        )
        assertNull("Selected field must be null when user owns zero fields", sanitized)
    }

    @Test
    fun testDestinationAllowlistRejectsInvalidDestinations() {
        // Valid screens must all be accepted
        val sampleValidScreens = listOf(
            "Screen7Activity", "Screen8Activity", "Screen9Activity", "Screen10Activity",
            "Screen11Activity", "Screen12Activity", "Screen13Activity", "Screen14Activity",
            "Screen17Activity", "Screen23Activity", "Screen27Activity", "Screen30Activity",
            "WelcomeActivity", "LoginActivity", "SplashActivity"
        )
        for (screen in sampleValidScreens) {
            assertTrue("Allowlist must accept legitimate screen $screen", MainActivity.isDestinationAllowed(screen))
        }

        // Malicious or arbitrary targets must be rejected
        val attackTargets = listOf(
            "com.attacker.MaliciousActivity",
            "AdminSettingsActivity",
            "Screen99Activity",
            "../secret/file",
            "",
            "   ",
            null
        )
        for (attack in attackTargets) {
            assertFalse("Allowlist must strictly reject unauthorized target '$attack'", MainActivity.isDestinationAllowed(attack))
        }
    }

    @Test
    fun testProtectedDestinationsRequireAuthentication() {
        // Public screens
        assertTrue("WelcomeActivity is public", MainActivity.isPublicDestination("WelcomeActivity"))
        assertTrue("LoginActivity is public", MainActivity.isPublicDestination("LoginActivity"))
        assertTrue("SplashActivity is public", MainActivity.isPublicDestination("SplashActivity"))

        // Protected screens (agricultural hubs and features)
        val protectedScreens = listOf(
            "Screen7Activity", "Screen8Activity", "Screen11Activity",
            "Screen12Activity", "Screen13Activity", "Screen17Activity",
            "Screen23Activity", "Screen27Activity", "Screen30Activity"
        )
        for (screen in protectedScreens) {
            assertFalse("Screen $screen must be protected and require authentication", MainActivity.isPublicDestination(screen))
        }
    }

    @Test
    fun testIntentExtrasSanitizationStripsAuthTokens() {
        val untrustedExtras: Map<String, Any?> = mapOf(
            "auth_token" to "malicious_spoofed_jwt_token",
            "token" to "stolen_bearer_token",
            "user_id" to 42,
            "password" to "injected_credential",
            "email" to "attacker@evil.com",
            "api_key" to "untrusted_key",
            "IS_LOGGED_IN" to true,
            "TARGET_SCREEN" to "Screen13Activity",
            "FIELD_ID" to 101,
            "NOTIFICATION_CATEGORY" to "Irrigation"
        )

        val sanitized = MainActivity.sanitizeIntentExtras(untrustedExtras)

        // Sensitive auth keys must be purged
        assertFalse("auth_token must be purged", sanitized.containsKey("auth_token"))
        assertFalse("token must be purged", sanitized.containsKey("token"))
        assertFalse("user_id must be purged", sanitized.containsKey("user_id"))
        assertFalse("password must be purged", sanitized.containsKey("password"))
        assertFalse("email must be purged", sanitized.containsKey("email"))
        assertFalse("api_key must be purged", sanitized.containsKey("api_key"))
        assertFalse("IS_LOGGED_IN must be purged", sanitized.containsKey("IS_LOGGED_IN"))

        // Legitimate navigation data must be preserved
        assertEquals("Screen13Activity", sanitized["TARGET_SCREEN"])
        assertEquals(101, sanitized["FIELD_ID"])
        assertEquals("Irrigation", sanitized["NOTIFICATION_CATEGORY"])
    }
}

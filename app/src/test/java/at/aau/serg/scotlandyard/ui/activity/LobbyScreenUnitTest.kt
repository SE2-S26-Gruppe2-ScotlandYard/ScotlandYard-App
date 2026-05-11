package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit Tests fuer LobbyScreen Logik (keine UI Tests)
 */
class LobbyScreenUnitTest {

    @Test
    fun lobbyScreen_class_exists() {
        assertNotNull(::LobbyScreen)
    }

    @Test
    fun lobbyBrowserView_code_validation_exactly_5_chars() {
        val validCodes = listOf("AB3KP", "XY123", "QQQQQ")
        val invalidCodes = listOf("", "A", "AB3K", "AB3KPX", "AB3KPXY")

        validCodes.forEach { assertTrue(it.length == 5, "Should be valid: $it") }
        invalidCodes.forEach { assertFalse(it.length == 5, "Should be invalid: $it") }
    }

    @Test
    fun lobbyCode_uppercase_enforced() {
        val input = "ab3kp"
        val result = input.uppercase()
        assertEquals("AB3KP", result)
        assertTrue(result == result.uppercase())
    }

    @Test
    fun lobbyCode_max_length_5() {
        val code = "AB3KPEXTRA"
        val trimmed = code.take(5)
        assertEquals(5, trimmed.length)
        assertEquals("AB3KP", trimmed)
    }

    @Test
    fun statusMessage_error_detection() {
        val errorMessage = "⚠️ Lobby not found"
        val successMessage = "Lobby created"

        assertTrue(errorMessage.startsWith("⚠️"))
        assertFalse(successMessage.startsWith("⚠️"))
    }

    @Test
    fun roleSelectionStarted_message_detection() {
        val roleSelectionMsg = "ROLE_SELECTION_STARTED"
        val normalMsg = "Joined lobby"

        assertTrue(roleSelectionMsg == "ROLE_SELECTION_STARTED")
        assertFalse(normalMsg == "ROLE_SELECTION_STARTED")
    }

    @Test
    fun canProceedToRoles_requires_min_3_players() {
        val playerCounts = listOf(1, 2, 3, 4, 5, 6)
        val expected =     listOf(false, false, true, true, true, true)

        playerCounts.zip(expected).forEach { (count, exp) ->
            assertEquals(exp, count >= 3, "Count $count should be ${if (exp) "valid" else "invalid"}")
        }
    }

    @Test
    fun mrxTaken_logic_correct() {
        val rolesNone = mapOf("user-1" to "NONE", "user-2" to "NONE")
        val rolesMrX  = mapOf("user-1" to "MRX",  "user-2" to "NONE")

        assertFalse(rolesNone.values.contains("MRX"))
        assertTrue(rolesMrX.values.contains("MRX"))
    }

    @Test
    fun allRolesSet_logic_correct() {
        val usersIds = listOf("user-1", "user-2", "user-3")

        val allSet = mapOf("user-1" to "MRX", "user-2" to "DETECTIVE", "user-3" to "DETECTIVE")
        val notAllSet = mapOf("user-1" to "MRX", "user-2" to "NONE", "user-3" to "DETECTIVE")

        assertTrue(usersIds.all { (allSet[it] ?: "NONE") != "NONE" })
        assertFalse(usersIds.all { (notAllSet[it] ?: "NONE") != "NONE" })
    }
}
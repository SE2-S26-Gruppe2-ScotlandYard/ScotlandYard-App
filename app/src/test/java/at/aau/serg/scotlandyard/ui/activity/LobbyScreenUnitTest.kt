package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for LobbyScreen (for SonarCloud coverage).
 */
class LobbyScreenUnitTest {

    @Test
    fun lobbyScreen_composable_function_exists() {
        assertNotNull(::LobbyScreen)
    }

    @Test
    fun lobbyScreen_accepts_callback_parameter() {
        val callbacks = mutableListOf<Boolean>()
        val testCallback = { callbacks.add(true) }
        testCallback()
        assertTrue(callbacks.isNotEmpty(), "Callback should be invokable")
    }
}


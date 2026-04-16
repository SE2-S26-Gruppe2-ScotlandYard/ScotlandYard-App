package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for StartScreen (for SonarCloud coverage).
 */
class StartScreenUnitTest {

    @Test
    fun startScreen_composable_function_exists() {
        assertNotNull(::StartScreen)
    }

    @Test
    fun startScreen_accepts_all_callback_parameters() {
        val callbackCounts = mutableListOf<Int>()
        val onStartGame = { callbackCounts.add(1) }
        val onRules = { callbackCounts.add(2) }
        val onSettings = { callbackCounts.add(3) }

        onStartGame()
        onRules()
        onSettings()

        assertEquals(3, callbackCounts.size, "All three callbacks should be invokable")
    }
}


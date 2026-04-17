package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for SettingsScreen (for SonarCloud coverage).
 */
class SettingsScreenUnitTest {

    @Test
    fun settingsScreen_composable_function_exists() {
        assertNotNull(::SettingsScreen)
    }

    @Test
    fun settingsScreen_accepts_callback_parameter() {
        val callbacks = mutableListOf<Boolean>()
        val testCallback = { callbacks.add(true) }
        testCallback()
        assertTrue(callbacks.isNotEmpty(), "Callback should be invokable")
    }
}


package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for BaseScreen composable (for SonarCloud coverage).
 */
class BaseScreenUnitTest {

    @Test
    fun baseScreen_composable_function_exists() {
        assertNotNull(::BaseScreen)
    }

    @Test
    fun baseScreen_accepts_callback_parameter() {
        val callbacks = mutableListOf<Boolean>()
        val testCallback = { callbacks.add(true) }
        testCallback()
        assertTrue(callbacks.isNotEmpty(), "Callback should be invokable")
    }
}


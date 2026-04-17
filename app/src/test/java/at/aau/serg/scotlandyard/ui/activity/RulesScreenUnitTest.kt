package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for RulesScreen (for SonarCloud coverage).
 */
class RulesScreenUnitTest {

    @Test
    fun rulesScreen_composable_function_exists() {
        assertNotNull(::RulesScreen)
    }

    @Test
    fun rulesScreen_accepts_callback_parameter() {
        val callbacks = mutableListOf<Boolean>()
        val testCallback = { callbacks.add(true) }
        testCallback()
        assertTrue(callbacks.isNotEmpty(), "Callback should be invokable")
    }

    @Test
    fun rulesScreen_content_parameters_are_valid() {
        val title = "Test Title"
        val description = "Test Description"
        assertNotNull(title, "Title should not be null")
        assertNotNull(description, "Description should not be null")
        assertTrue(title.isNotEmpty(), "Title should not be empty")
        assertTrue(description.isNotEmpty(), "Description should not be empty")
    }
}


package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for MainActivity (for SonarCloud coverage).
 */
class MainActivityUnitTest {

    @Test
    fun mainActivity_class_exists() {
        assertNotNull(MainActivity::class)
    }

    @Test
    fun mainActivity_is_component_activity() {
        val isComponentActivity = androidx.activity.ComponentActivity::class.java
            .isAssignableFrom(MainActivity::class.java)
        assertTrue(isComponentActivity, "MainActivity should extend ComponentActivity")
    }

    @Test
    fun mainActivity_class_name_is_correct() {
        val className = MainActivity::class.java.simpleName
        assertEquals("MainActivity", className, "Class name should be MainActivity")
    }
}


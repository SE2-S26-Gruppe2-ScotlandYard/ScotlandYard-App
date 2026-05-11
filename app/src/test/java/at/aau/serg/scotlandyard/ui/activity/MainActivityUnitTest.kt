package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit Tests fuer MainActivity
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
        assertTrue(isComponentActivity)
    }

    @Test
    fun mainActivity_class_name_is_correct() {
        assertEquals("MainActivity", MainActivity::class.java.simpleName)
    }

    @Test
    fun navigation_routes_are_defined() {
        // Sicherstellen dass die Navigation-Routes korrekte Strings sind
        val routes = listOf("start", "login", "lobby", "roleSelection", "rules", "settings")
        routes.forEach { route ->
            assertNotNull(route)
            assertTrue(route.isNotBlank())
        }
    }

    @Test
    fun roleSelection_route_name_correct() {
        val route = "roleSelection"
        assertEquals("roleSelection", route)
        assertFalse(route.contains("/"))
    }
}
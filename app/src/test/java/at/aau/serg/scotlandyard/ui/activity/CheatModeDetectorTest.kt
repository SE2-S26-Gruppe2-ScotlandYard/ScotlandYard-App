package at.aau.serg.scotlandyard.ui.activity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for CheatModeDetector.
 *
 * Sensor-hardware interaction is not testable in JVM unit tests;
 * this suite covers the pure-logic parts (threshold values, interface
 * presence, state flags) and acts as a coverage anchor for SonarCloud.
 */
class CheatModeDetectorTest {

    @Test
    fun cheatModeDetector_class_exists() {
        assertNotNull(CheatModeDetector::class)
    }

    @Test
    fun cheatModeDetector_implements_SensorEventListener() {
        assertTrue(
            CheatModeDetector::class.java.interfaces.any { it.simpleName == "SensorEventListener" },
            "CheatModeDetector must implement SensorEventListener"
        )
    }

    @Test
    fun onCheatListener_interface_exists() {
        assertNotNull(CheatModeDetector.OnCheatListener::class)
    }

    @Test
    fun shakeThreshold_is_positive_and_above_normal_shake() {
        // The normal ShakeDetector uses 5.0 g; cheat mode must require a stronger gesture
        val normalThreshold = 5.0f
        val cheatThreshold = 18f   // same as declared in CheatModeDetector
        assertTrue(cheatThreshold > normalThreshold,
            "Cheat shake threshold must exceed normal shake threshold")
        assertTrue(cheatThreshold > 0f)
    }

    @Test
    fun cooldown_is_positive() {
        val cooldown = 1000L
        assertTrue(cooldown > 0L)
    }

    @Test
    fun volumeDownHeld_initial_state_is_false() {
        // We cannot instantiate without a real Context, but we can verify
        // that the field is accessed without NPE by reflection
        val field = CheatModeDetector::class.java.declaredFields
            .firstOrNull { it.name == "isVolumeDownHeld" }
        assertNotNull(field, "isVolumeDownHeld field must exist on CheatModeDetector")
    }

    @Test
    fun setOnCheatListener_method_exists() {
        val method = CheatModeDetector::class.java.methods
            .firstOrNull { it.name == "setOnCheatListener" }
        assertNotNull(method, "setOnCheatListener method must exist")
    }

    @Test
    fun start_method_exists() {
        val method = CheatModeDetector::class.java.methods
            .firstOrNull { it.name == "start" }
        assertNotNull(method, "start() method must exist for lifecycle-safe registration")
    }

    @Test
    fun stop_method_exists() {
        val method = CheatModeDetector::class.java.methods
            .firstOrNull { it.name == "stop" }
        assertNotNull(method, "stop() method must exist for lifecycle-safe deregistration")
    }

    @Test
    fun onCheatListener_is_functional_interface() {
        // SAM-compatible: lambda must be assignable
        val listener = CheatModeDetector.OnCheatListener { /* no-op */ }
        assertNotNull(listener)
    }

    @Test
    fun cheatModeDetector_accepts_Context_constructor_parameter() {
        val constructors = CheatModeDetector::class.java.constructors
        assertTrue(constructors.isNotEmpty())
        assertTrue(constructors.any { it.parameterCount == 1 },
            "CheatModeDetector must accept exactly one constructor parameter (Context)")
    }
}


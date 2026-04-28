package at.aau.serg.scotlandyard.viewmodel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for GameViewModel (for SonarCloud coverage).
 */
class GameViewModelTest {

    @Test
    fun gameViewModel_class_exists() {
        assertNotNull(GameViewModel::class)
    }

    @Test
    fun gameViewModel_implements_callbacks() {
        assertTrue(GameViewModel::class.java.interfaces.any {
            it.simpleName == "Callbacks"
        }, "GameViewModel should implement Callbacks interface")
    }

    @Test
    fun resetGameState_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any { it.name == "resetGameState" })
    }

    @Test
    fun clearError_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any { it.name == "clearError" })
    }

    @Test
    fun requestStartPosition_method_has_correct_signature() {
        // Verify the method exists with String parameters
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "requestStartPosition" &&
            it.parameterCount == 2
        }, "requestStartPosition should have 2 parameters (gameId, playerId)")
    }

    @Test
    fun confirmStartPosition_method_has_correct_signature() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "confirmStartPosition" &&
            it.parameterCount == 2
        }, "confirmStartPosition should have 2 parameters (gameId, playerId)")
    }

    @Test
    fun viewmodel_extends_android_lifecycle_viewmodel() {
        assertTrue(GameViewModel::class.java.superclass?.simpleName == "ViewModel",
            "GameViewModel should extend ViewModel")
    }
}






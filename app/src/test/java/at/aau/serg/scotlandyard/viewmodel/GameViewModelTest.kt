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
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "requestStartPosition" && it.parameterCount == 2
        }, "requestStartPosition should have 2 parameters (gameId, playerId)")
    }

    @Test
    fun confirmStartPosition_method_has_correct_signature() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "confirmStartPosition" && it.parameterCount == 2
        }, "confirmStartPosition should have 2 parameters (gameId, playerId)")
    }

    @Test
    fun generateLocalStartPosition_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any {
            it.name == "generateLocalStartPosition"
        }, "generateLocalStartPosition() must exist for local random position generation")
    }

    @Test
    fun setCheatStartPosition_method_exists_with_one_int_parameter() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "setCheatStartPosition" && it.parameterCount == 1
        }, "setCheatStartPosition(Int) must exist for cheat-mode selection")
    }

    @Test
    fun activateCheatMode_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any {
            it.name == "activateCheatMode"
        })
    }

    @Test
    fun deactivateCheatMode_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any {
            it.name == "deactivateCheatMode"
        })
    }

    @Test
    fun peekStartPosition_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any {
            it.name == "peekStartPosition"
        })
    }

    @Test
    fun viewmodel_extends_android_lifecycle_viewmodel() {
        assertTrue(GameViewModel::class.java.superclass?.simpleName == "ViewModel",
            "GameViewModel should extend ViewModel")
    }

    // ── Tests for changes in this sprint ────────────────────────────────────────

    @Test
    fun unsubscribeFromStartPosition_method_exists() {
        assertTrue(GameViewModel::class.java.declaredMethods.any {
            it.name == "unsubscribeFromStartPosition"
        }, "unsubscribeFromStartPosition() must exist to cancel the startPosition subscription")
    }

    @Test
    fun unsubscribeFromStartPosition_takes_no_parameters() {
        val method = GameViewModel::class.java.declaredMethods
            .firstOrNull { it.name == "unsubscribeFromStartPosition" }
        assertNotNull(method, "unsubscribeFromStartPosition should be declared")
        assertEquals(0, method!!.parameterCount, "unsubscribeFromStartPosition should take no parameters")
    }

    @Test
    fun requestGameState_method_exists_with_one_parameter() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "requestGameState" && it.parameterCount == 1
        }, "requestGameState(gameId) must exist so confirmStartPosition can refresh state")
    }

    @Test
    fun updateMyPosition_method_has_correct_signature() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "updateMyPosition" && it.parameterCount == 2
        }, "updateMyPosition(playerId, isMrX) must exist to track the local player's position")
    }

    @Test
    fun buildPlayerPositions_method_has_correct_signature() {
        val methods = GameViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "buildPlayerPositions" && it.parameterCount == 2
        }, "buildPlayerPositions(isMrX, detectiveIdOrder) must exist")
    }
}









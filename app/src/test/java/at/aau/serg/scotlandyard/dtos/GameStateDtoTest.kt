package at.aau.serg.scotlandyard.dtos

import org.junit.Assert.*
import org.junit.Test

class GameStateDtoTest {
    private fun createDto(phase: String) = GameStateDto(
        gameId = "game1",
        currentRound = 3,
        currentPhase = phase,
        detectivePositions = mapOf("p1" to 10, "p2" to 20),
        mrXPosition = 42,
        doubleMoveActive = false,
        mrxMovesRemaining = 5
    )

    @Test
    fun isMrXPhase_true_for_MRX() {
        assertTrue(createDto("MRX").isMrXPhase)
    }

    @Test
    fun isMrXPhase_false_for_DETECTIVES() {
        assertFalse(createDto("DETECTIVES").isMrXPhase)
    }

    @Test
    fun isDetectivesPhase_true_for_DETECTIVES() {
        assertTrue(createDto("DETECTIVES").isDetectivesPhase)
    }

    @Test
    fun isDetectivesPhase_false_for_MRX() {
        assertFalse(createDto("MRX").isDetectivesPhase)
    }

    @Test
    fun both_flags_false_for_unknown_phase() {
        val dto = createDto("UNKNOWN")
        assertFalse(dto.isMrXPhase)
        assertFalse(dto.isDetectivesPhase)
    }
}
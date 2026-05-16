package at.aau.serg.scotlandyard.dtos

data class GameStateDto(
    val gameId: String,
    val currentRound: Int,
    val currentPhase: String,
    val detectivePositions: Map<String, Int>,
    val mrXPosition: Int?,
    val doubleMoveActive: Boolean,
    val mrxMovesRemaining: Int
) {
    val isMrXPhase: Boolean get() = currentPhase == "MRX"
    val isDetectivesPhase: Boolean get() = currentPhase == "DETECTIVES"
}
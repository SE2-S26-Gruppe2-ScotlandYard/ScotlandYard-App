package at.aau.serg.scotlandyard.dtos

data class GameStateDto(
    val gameId: String,
    val currentRound: Int,
    val hostId: String? = null,
    val playerNames: Map<String, String>? = null,
    val currentPhase: String,
    val detectivePositions: Map<String, Int>,
    val mrXPosition: Int?,
    val doubleMoveActive: Boolean,
    val mrxMovesRemaining: Int,
    val playerTickets: Map<String, Map<String, Int>> = emptyMap(),
    val mrXSpecialTickets: Map<String, Int> = emptyMap(),
    val mrXMoveHistory: List<String> = emptyList(),
    val mrXRevealedPositions: Map<Int, Int> = emptyMap(),
    val allPlayersReady: Boolean = false
) {
    val isMrXPhase: Boolean get() = currentPhase == "MRX"
    val isDetectivesPhase: Boolean get() = currentPhase == "DETECTIVES"
}
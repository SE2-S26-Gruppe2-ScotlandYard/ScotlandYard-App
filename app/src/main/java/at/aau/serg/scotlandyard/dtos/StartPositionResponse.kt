package at.aau.serg.scotlandyard.dtos

/**
 * Data class representing the backend response for start position assignment
 *
 * Expected JSON from backend:
 * {
 *   "type": "START_POSITION_ASSIGNED" or "ERROR",
 *   "gameId": "<gameId>",
 *   "playerId": "<playerId>",
 *   "startPosition": 42 (nullable),
 *   "message": "optional error message" (nullable)
 * }
 */
data class StartPositionResponse(
    val type: String,
    val gameId: String,
    val playerId: String,
    val startPosition: Int?,
    val message: String?
)


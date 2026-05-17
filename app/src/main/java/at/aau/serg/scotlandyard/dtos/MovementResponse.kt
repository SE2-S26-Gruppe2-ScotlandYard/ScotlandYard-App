package at.aau.serg.scotlandyard.dtos

import at.aau.serg.scotlandyard.model.TicketType

data class MovementResponse(
    val success: Boolean,
    val message: String,
    val newPosition: Int,
    val movementData: MovementData
)

data class MovementData(
    val playerId: String,
    val ticketUsed: TicketType,
    val fromPosition: Int,
    val toPosition: Int,
    val isMrx: Boolean
)
package at.aau.serg.scotlandyard.network

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class MyStompTest {
    @Test
    fun testValidPositionRange() {
        val validPositions = listOf(1, 50, 100, 150, 199)
        val invalidPositions = listOf(0, 200, -1, 300)

        validPositions.forEach { position ->
            assertTrue(position in 1..199)
        }

        invalidPositions.forEach { position ->
            assertFalse(position in 1..199)
        }
    }

    @Test
    fun testTicketTypes() {
        val validTickets = listOf("WALKING", "ESCOOTER", "CARSHARING", "BLACK", "DOUBLE")
        val invalidTickets = listOf("FLYING", "TELEPORT", "INVALID")

        validTickets.forEach { ticket ->
            assertTrue(ticket in validTickets)
        }

        invalidTickets.forEach { ticket ->
            assertFalse(ticket in validTickets)
        }
    }
}
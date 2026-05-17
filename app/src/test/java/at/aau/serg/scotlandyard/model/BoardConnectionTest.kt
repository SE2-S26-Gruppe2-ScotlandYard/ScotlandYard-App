package at.aau.serg.scotlandyard.model

import org.junit.Assert.*
import org.junit.Test

class BoardConnectionTest {

    // BoardConnection.init() requires Android context to call

    private fun parseViaReflection(json: String): Map<Int, Map<TicketType, Set<Int>>> {
        val parseMethod = BoardConnection::class.java.getDeclaredMethod("parse", String::class.java)
        parseMethod.isAccessible = true
        return parseMethod.invoke(BoardConnection, json) as Map<Int, Map<TicketType, Set<Int>>>
    }

    private fun injectAdjacency(map: Map<Int, Map<TicketType, Set<Int>>>) {
        val adjacencyField = BoardConnection::class.java.getDeclaredField("adjacency")
        adjacencyField.isAccessible = true
        adjacencyField.set(BoardConnection, map)

        val initializedField = BoardConnection::class.java.getDeclaredField("initialized")
        initializedField.isAccessible = true
        initializedField.set(BoardConnection, true)
    }

    private val simpleJson = """
        {
          "1": [
            {"to": 2, "transport": "WALKING"},
            {"to": 3, "transport": "ESCOOTER"},
            {"to": 4, "transport": "CARSHARING"}
          ],
          "2": [
            {"to": 1, "transport": "WALKING"},
            {"to": 5, "transport": "WALKING"}
          ]
        }
        """.trimIndent()

    @Test
    fun parse_validJson_returnsCorrectNodeCount() {
        val result = parseViaReflection(simpleJson)
        assertEquals(2, result.size)
    }

    @Test
    fun parse_validJson_extractsWalkingNeighbors() {
        val result = parseViaReflection(simpleJson)
        assertEquals(setOf(2), result[1]?.get(TicketType.WALKING))
    }

    @Test
    fun parse_validJson_extractsEscooterNeighbors() {
        val result = parseViaReflection(simpleJson)
        assertEquals(setOf(3), result[1]?.get(TicketType.ESCOOTER))
    }

    @Test
    fun parse_validJson_extractsCarsharingNeighbors() {
        val result = parseViaReflection(simpleJson)
        assertEquals(setOf(4), result[1]?.get(TicketType.CARSHARING))
    }

    @Test
    fun parse_nodeWithMultipleWalkingEdges_returnsAllAsSet() {
        val result = parseViaReflection(simpleJson)
        assertEquals(setOf(1, 5), result[2]?.get(TicketType.WALKING))
    }

    @Test
    fun parse_unknownTransportType_ignoresIt() {
        val json = """{"1": [{"to": 9, "transport": "SPACESHIP"}]}"""
        val result = parseViaReflection(json)
        assertTrue(result[1]?.isEmpty() ?: true)
    }

    @Test
    fun reachableFrom_unknownNode_returnsEmptySet() {
        injectAdjacency(parseViaReflection(simpleJson))
        assertEquals(emptySet<Int>(), BoardConnection.reachableFrom(99, TicketType.WALKING))
    }

    @Test
    fun reachableFrom_existingNodeWithWalkingTicket_returnsCorrectNeighbors() {
        injectAdjacency(parseViaReflection(simpleJson))
        assertEquals(setOf(2), BoardConnection.reachableFrom(1, TicketType.WALKING))
    }

    @Test
    fun reachableFrom_blackTicket_returnsAllNeighbors() {
        injectAdjacency(parseViaReflection(simpleJson))
        val result = BoardConnection.reachableFrom(1, TicketType.BLACK)
        assertEquals(setOf(2, 3, 4), result)
    }

    @Test
    fun reachableFrom_ticketWithNoEdges_returnsEmptySet() {
        injectAdjacency(parseViaReflection(simpleJson))
        assertEquals(emptySet<Int>(), BoardConnection.reachableFrom(1, TicketType.DOUBLE))
    }

    @Test
    fun parse_emptyJsonObject_returnsEmptyMap() {
        val result = parseViaReflection("{}")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parse_nodeWithEmptyConnections_returnsEmptyMap() {
        val json = """{"5": []}"""
        val result = parseViaReflection(json)
        assertTrue(result[5]?.isEmpty() ?: true)
    }
}
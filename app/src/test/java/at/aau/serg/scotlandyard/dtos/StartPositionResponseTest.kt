package at.aau.serg.scotlandyard.dtos

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.json.JSONObject

class StartPositionResponseTest {

    // ── Data class basics ─────────────────────────────────────────────────

    @Test
    fun startPositionResponse_stores_all_fields() {
        val response = StartPositionResponse(
            type = "START_POSITION_ASSIGNED",
            gameId = "game-123",
            playerId = "player-456",
            startPosition = 42,
            message = null
        )
        assertEquals("START_POSITION_ASSIGNED", response.type)
        assertEquals("game-123", response.gameId)
        assertEquals("player-456", response.playerId)
        assertEquals(42, response.startPosition)
        assertNull(response.message)
    }

    @Test
    fun startPositionResponse_equality() {
        val r1 = StartPositionResponse("START_POSITION_ASSIGNED", "g1", "p1", 10, null)
        val r2 = StartPositionResponse("START_POSITION_ASSIGNED", "g1", "p1", 10, null)
        assertEquals(r1, r2)
    }

    @Test
    fun startPositionResponse_inequality_different_position() {
        val r1 = StartPositionResponse("START_POSITION_ASSIGNED", "g1", "p1", 10, null)
        val r2 = StartPositionResponse("START_POSITION_ASSIGNED", "g1", "p1", 20, null)
        assertNotEquals(r1, r2)
    }

    @Test
    fun startPositionResponse_inequality_different_type() {
        val r1 = StartPositionResponse("START_POSITION_ASSIGNED", "g1", "p1", 10, null)
        val r2 = StartPositionResponse("ERROR", "g1", "p1", 10, null)
        assertNotEquals(r1, r2)
    }

    // ── Success response ───────────────────────────────────────────────────

    @Test
    fun startPositionResponse_success_has_position() {
        val response = StartPositionResponse(
            type = "START_POSITION_ASSIGNED",
            gameId = "game-1",
            playerId = "player-1",
            startPosition = 99,
            message = null
        )
        assertEquals("START_POSITION_ASSIGNED", response.type)
        assertNotNull(response.startPosition)
        assertEquals(99, response.startPosition)
        assertNull(response.message)
    }

    @Test
    fun startPositionResponse_success_position_zero_is_valid() {
        val response = StartPositionResponse(
            type = "START_POSITION_ASSIGNED",
            gameId = "g", playerId = "p",
            startPosition = 0,
            message = null
        )
        assertNotNull(response.startPosition)
        assertEquals(0, response.startPosition)
    }

    @Test
    fun startPositionResponse_success_large_position() {
        val response = StartPositionResponse(
            type = "START_POSITION_ASSIGNED",
            gameId = "g", playerId = "p",
            startPosition = 199,
            message = null
        )
        assertEquals(199, response.startPosition)
    }

    // ── Error response ─────────────────────────────────────────────────────

    @Test
    fun startPositionResponse_error_has_message_no_position() {
        val response = StartPositionResponse(
            type = "ERROR",
            gameId = "game-1",
            playerId = "player-1",
            startPosition = null,
            message = "Game not found"
        )
        assertEquals("ERROR", response.type)
        assertNull(response.startPosition)
        assertEquals("Game not found", response.message)
    }

    @Test
    fun startPositionResponse_error_already_assigned() {
        val response = StartPositionResponse(
            type = "ERROR",
            gameId = "g", playerId = "p",
            startPosition = null,
            message = "Position already assigned to another player"
        )
        assertEquals("ERROR", response.type)
        assertNull(response.startPosition)
        assertNotNull(response.message)
    }

    // ── Type checks ────────────────────────────────────────────────────────

    @Test
    fun startPositionResponse_type_START_POSITION_ASSIGNED_is_success() {
        val response = StartPositionResponse("START_POSITION_ASSIGNED", "g", "p", 5, null)
        assertTrue(response.type == "START_POSITION_ASSIGNED")
    }

    @Test
    fun startPositionResponse_type_ERROR_is_error() {
        val response = StartPositionResponse("ERROR", "g", "p", null, "error")
        assertTrue(response.type == "ERROR")
    }

    @Test
    fun startPositionResponse_unknown_type_is_neither() {
        val response = StartPositionResponse("UNKNOWN", "g", "p", null, null)
        assertFalse(response.type == "START_POSITION_ASSIGNED")
        assertFalse(response.type == "ERROR")
    }

    // ── JSON format check ──────────────────────────────────────────────────

    @Test
    fun startPosition_request_json_has_correct_fields() {
        val json = JSONObject().apply {
            put("gameId", "game-123")
            put("playerId", "player-456")
        }
        assertTrue(json.has("gameId"))
        assertTrue(json.has("playerId"))
        assertEquals("game-123", json.getString("gameId"))
        assertEquals("player-456", json.getString("playerId"))
        assertEquals(2, json.length())
    }

    @Test
    fun startPositionResponse_copy_creates_new_instance() {
        val original = StartPositionResponse("START_POSITION_ASSIGNED", "g", "p", 42, null)
        val copy = original.copy(startPosition = 100)
        assertEquals(42, original.startPosition)
        assertEquals(100, copy.startPosition)
        assertNotEquals(original, copy)
    }

    @Test
    fun startPositionResponse_toString_contains_type() {
        val response = StartPositionResponse("START_POSITION_ASSIGNED", "g", "p", 42, null)
        assertTrue(response.toString().contains("START_POSITION_ASSIGNED"))
    }
}


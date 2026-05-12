package at.aau.serg.scotlandyard.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class LobbyModelsTest {

    // ── LobbyUserData ──────────────────────────────────────────────────────

    @Test
    fun lobbyUserData_stores_id_and_name() {
        val user = LobbyUserData(id = "user-1", name = "Hans")
        assertEquals("user-1", user.id)
        assertEquals("Hans", user.name)
    }

    @Test
    fun lobbyUserData_equality() {
        val a = LobbyUserData("1", "Hans")
        val b = LobbyUserData("1", "Hans")
        assertEquals(a, b)
    }

    @Test
    fun lobbyUserData_inequality_different_id() {
        val a = LobbyUserData("1", "Hans")
        val b = LobbyUserData("2", "Hans")
        assertNotEquals(a, b)
    }

    @Test
    fun lobbyUserData_inequality_different_name() {
        val a = LobbyUserData("1", "Hans")
        val b = LobbyUserData("1", "Fritz")
        assertNotEquals(a, b)
    }

    // ── LobbyData ──────────────────────────────────────────────────────────

    @Test
    fun lobbyData_stores_all_fields() {
        val users = listOf(LobbyUserData("1", "Hans"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Test Lobby", hostId = "1",
            isStarted = false, users = users,
            readyStatus = mapOf("1" to false),
            selectedRoles = mapOf("1" to "NONE")
        )
        assertEquals("AB3KP", lobby.id)
        assertEquals("Test Lobby", lobby.name)
        assertEquals("1", lobby.hostId)
        assertFalse(lobby.isStarted)
        assertEquals(1, lobby.users.size)
    }

    @Test
    fun lobbyData_selectedRoles_mrx_and_detective() {
        val users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users,
            readyStatus = mapOf("1" to false, "2" to false),
            selectedRoles = mapOf("1" to "MRX", "2" to "DETECTIVE")
        )
        assertEquals("MRX", lobby.selectedRoles["1"])
        assertEquals("DETECTIVE", lobby.selectedRoles["2"])
    }

    @Test
    fun lobbyData_empty_users_list() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Empty", hostId = "1",
            isStarted = false, users = emptyList(),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertTrue(lobby.users.isEmpty())
    }

    @Test
    fun lobbyData_mrx_taken_detection() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = listOf(LobbyUserData("1", "Hans")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX")
        )
        assertTrue(lobby.selectedRoles.values.contains("MRX"))
    }

    @Test
    fun lobbyData_all_roles_set() {
        val users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users,
            readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX", "2" to "DETECTIVE")
        )
        val allSet = lobby.users.all { (lobby.selectedRoles[it.id] ?: "NONE") != "NONE" }
        assertTrue(allSet)
    }

    // ── LobbyResponse ──────────────────────────────────────────────────────

    @Test
    fun lobbyResponse_success_true() {
        val response = LobbyResponse(success = true, message = "Lobby created", lobbyId = "AB3KP", lobby = null)
        assertTrue(response.success)
        assertEquals("Lobby created", response.message)
        assertEquals("AB3KP", response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun lobbyResponse_success_false() {
        val response = LobbyResponse(success = false, message = "Error", lobbyId = null, lobby = null)
        assertFalse(response.success)
        assertNull(response.lobbyId)
    }

    // ── JSON Parsing mit Robolectric ───────────────────────────────────────

    @Test
    fun toLobbyResponse_parses_success_response() {
        val json = JSONObject().apply {
            put("success", true)
            put("message", "Lobby created")
            put("lobbyId", "AB3KP")
        }
        val response = json.toLobbyResponse()
        assertTrue(response.success)
        assertEquals("Lobby created", response.message)
        assertEquals("AB3KP", response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun toLobbyResponse_parses_failure_response() {
        val json = JSONObject().apply {
            put("success", false)
            put("message", "Lobby not found")
        }
        val response = json.toLobbyResponse()
        assertFalse(response.success)
        assertEquals("Lobby not found", response.message)
        assertNull(response.lobbyId)
    }

    @Test
    fun toLobbyData_parses_full_lobby() {
        val userJson = JSONObject().apply {
            put("id", "user-1")
            put("nickName", "Hans")
        }
        val lobbyJson = JSONObject().apply {
            put("id", "AB3KP")
            put("name", "Hans's Lobby")
            put("hostId", "user-1")
            put("started", false)
            put("users", JSONArray().apply { put(userJson) })
            put("readyStatus", JSONObject().apply { put("user-1", false) })
            put("selectedRoles", JSONObject().apply { put("user-1", "NONE") })
        }
        val lobby = lobbyJson.toLobbyData()
        assertEquals("AB3KP", lobby.id)
        assertEquals("Hans's Lobby", lobby.name)
        assertEquals("user-1", lobby.hostId)
        assertFalse(lobby.isStarted)
        assertEquals(1, lobby.users.size)
        assertEquals("Hans", lobby.users[0].name)
        assertEquals("user-1", lobby.users[0].id)
        assertEquals(false, lobby.readyStatus["user-1"])
        assertEquals("NONE", lobby.selectedRoles["user-1"])
    }

    @Test
    fun toLobbyData_parses_nickname_field() {
        val userJson = JSONObject().apply {
            put("id", "user-2")
            put("nickName", "Fritz")
        }
        val lobbyJson = JSONObject().apply {
            put("id", "XY123")
            put("name", "Test")
            put("hostId", "user-2")
            put("started", false)
            put("users", JSONArray().apply { put(userJson) })
        }
        val lobby = lobbyJson.toLobbyData()
        assertEquals("Fritz", lobby.users[0].name)
    }

    @Test
    fun toLobbyResponse_with_embedded_lobby() {
        val userJson = JSONObject().apply {
            put("id", "1")
            put("nickName", "Hans")
        }
        val lobbyJson = JSONObject().apply {
            put("id", "AB3KP")
            put("name", "Hans's Lobby")
            put("hostId", "1")
            put("started", false)
            put("users", JSONArray().apply { put(userJson) })
        }
        val responseJson = JSONObject().apply {
            put("success", true)
            put("message", "Lobby created")
            put("lobbyId", "AB3KP")
            put("lobby", lobbyJson)
        }
        val response = responseJson.toLobbyResponse()
        assertTrue(response.success)
        assertNotNull(response.lobby)
        assertEquals("AB3KP", response.lobby!!.id)
        assertEquals(1, response.lobby!!.users.size)
    }

    @Test
    fun toLobbyResponse_role_selection_started() {
        val json = JSONObject().apply {
            put("success", true)
            put("message", "ROLE_SELECTION_STARTED")
            put("lobbyId", "AB3KP")
        }
        val response = json.toLobbyResponse()
        assertTrue(response.success)
        assertEquals("ROLE_SELECTION_STARTED", response.message)
    }

    @Test
    fun toLobbyData_multiple_users() {
        val lobbyJson = JSONObject().apply {
            put("id", "AB3KP")
            put("name", "Lobby")
            put("hostId", "1")
            put("started", false)
            put("users", JSONArray().apply {
                put(JSONObject().apply { put("id", "1"); put("nickName", "Hans") })
                put(JSONObject().apply { put("id", "2"); put("nickName", "Fritz") })
                put(JSONObject().apply { put("id", "3"); put("nickName", "Kurt") })
            })
        }
        val lobby = lobbyJson.toLobbyData()
        assertEquals(3, lobby.users.size)
    }

    @Test
    fun toLobbyData_empty_users() {
        val lobbyJson = JSONObject().apply {
            put("id", "AB3KP")
            put("name", "Lobby")
            put("hostId", "1")
            put("started", false)
            put("users", JSONArray())
        }
        val lobby = lobbyJson.toLobbyData()
        assertTrue(lobby.users.isEmpty())
    }
}
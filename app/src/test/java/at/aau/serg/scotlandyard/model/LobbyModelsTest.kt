package at.aau.serg.scotlandyard.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

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
        assertEquals(LobbyUserData("1", "Hans"), LobbyUserData("1", "Hans"))
    }

    @Test
    fun lobbyUserData_inequality_different_id() {
        assertNotEquals(LobbyUserData("1", "Hans"), LobbyUserData("2", "Hans"))
    }

    @Test
    fun lobbyUserData_inequality_different_name() {
        assertNotEquals(LobbyUserData("1", "Hans"), LobbyUserData("1", "Fritz"))
    }

    // ── LobbyData ──────────────────────────────────────────────────────────

    @Test
    fun lobbyData_stores_all_fields() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test Lobby", hostId = "1",
            isStarted = false,
            users = listOf(LobbyUserData("1", "Hans")),
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
    fun lobbyData_mrx_taken_detection() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX", "2" to "NONE")
        )
        assertTrue(lobby.selectedRoles.values.contains("MRX"))
    }

    @Test
    fun lobbyData_all_roles_set() {
        val users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users, readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX", "2" to "DETECTIVE")
        )
        assertTrue(lobby.users.all { (lobby.selectedRoles[it.id] ?: "NONE") != "NONE" })
    }

    @Test
    fun lobbyData_not_all_roles_set() {
        val users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users, readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX", "2" to "NONE")
        )
        assertFalse(lobby.users.all { (lobby.selectedRoles[it.id] ?: "NONE") != "NONE" })
    }

    @Test
    fun lobbyData_empty_users() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Empty", hostId = "1",
            isStarted = false, users = emptyList(),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertTrue(lobby.users.isEmpty())
    }

    // ── LobbyResponse ──────────────────────────────────────────────────────

    @Test
    fun lobbyResponse_success_true() {
        val response = LobbyResponse(true, "Lobby created", "AB3KP", null)
        assertTrue(response.success)
        assertEquals("Lobby created", response.message)
        assertEquals("AB3KP", response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun lobbyResponse_success_false() {
        val response = LobbyResponse(false, "Error", null, null)
        assertFalse(response.success)
        assertNull(response.lobbyId)
    }

    // ── JSON Parsing mit Gson (kein Robolectric nötig) ────────────────────

    @Test
    fun toLobbyResponse_parses_success() {
        val json = """{"success":true,"message":"Lobby created","lobbyId":"AB3KP"}"""
        val response = json.toLobbyResponse()
        assertTrue(response.success)
        assertEquals("Lobby created", response.message)
        assertEquals("AB3KP", response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun toLobbyResponse_parses_failure() {
        val json = """{"success":false,"message":"Lobby not found"}"""
        val response = json.toLobbyResponse()
        assertFalse(response.success)
        assertEquals("Lobby not found", response.message)
        assertNull(response.lobbyId)
    }

    @Test
    fun toLobbyResponse_parses_with_embedded_lobby() {
        val json = """
        {
            "success": true,
            "message": "Lobby created",
            "lobbyId": "AB3KP",
            "lobby": {
                "id": "AB3KP",
                "name": "Hans's Lobby",
                "hostId": "user-1",
                "started": false,
                "users": [{"id": "user-1", "nickName": "Hans"}],
                "readyStatus": {"user-1": false},
                "selectedRoles": {"user-1": "NONE"}
            }
        }
        """.trimIndent()
        val response = json.toLobbyResponse()
        assertTrue(response.success)
        assertNotNull(response.lobby)
        assertEquals("AB3KP", response.lobby!!.id)
        assertEquals(1, response.lobby!!.users.size)
        assertEquals("Hans", response.lobby!!.users[0].name)
    }

    @Test
    fun toLobbyResponse_parses_nickname_field() {
        val json = """
        {
            "success": true,
            "message": "Joined lobby",
            "lobbyId": "AB3KP",
            "lobby": {
                "id": "AB3KP",
                "name": "Lobby",
                "hostId": "1",
                "started": false,
                "users": [{"id": "1", "nickName": "Fritz"}]
            }
        }
        """.trimIndent()
        val response = json.toLobbyResponse()
        assertEquals("Fritz", response.lobby!!.users[0].name)
    }

    @Test
    fun toLobbyResponse_parses_multiple_users() {
        val json = """
        {
            "success": true,
            "message": "Joined lobby",
            "lobbyId": "AB3KP",
            "lobby": {
                "id": "AB3KP",
                "name": "Lobby",
                "hostId": "1",
                "started": false,
                "users": [
                    {"id": "1", "nickName": "Hans"},
                    {"id": "2", "nickName": "Fritz"},
                    {"id": "3", "nickName": "Kurt"}
                ]
            }
        }
        """.trimIndent()
        val response = json.toLobbyResponse()
        assertEquals(3, response.lobby!!.users.size)
    }

    @Test
    fun toLobbyResponse_role_selection_started() {
        val json = """{"success":true,"message":"ROLE_SELECTION_STARTED","lobbyId":"AB3KP"}"""
        val response = json.toLobbyResponse()
        assertTrue(response.success)
        assertEquals("ROLE_SELECTION_STARTED", response.message)
    }

    @Test
    fun toLobbyResponse_parses_roles() {
        val json = """
        {
            "success": true,
            "message": "Role set",
            "lobbyId": "AB3KP",
            "lobby": {
                "id": "AB3KP",
                "name": "Lobby",
                "hostId": "1",
                "started": false,
                "users": [{"id": "1", "nickName": "Hans"}, {"id": "2", "nickName": "Fritz"}],
                "selectedRoles": {"1": "MRX", "2": "DETECTIVE"}
            }
        }
        """.trimIndent()
        val response = json.toLobbyResponse()
        assertEquals("MRX", response.lobby!!.selectedRoles["1"])
        assertEquals("DETECTIVE", response.lobby!!.selectedRoles["2"])
    }

    @Test
    fun toLobbyResponse_empty_users_list() {
        val json = """
        {
            "success": true,
            "message": "Lobby created",
            "lobbyId": "AB3KP",
            "lobby": {
                "id": "AB3KP",
                "name": "Lobby",
                "hostId": "1",
                "started": false,
                "users": []
            }
        }
        """.trimIndent()
        val response = json.toLobbyResponse()
        assertTrue(response.lobby!!.users.isEmpty())
    }
}
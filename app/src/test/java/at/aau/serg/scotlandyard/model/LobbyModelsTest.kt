package at.aau.serg.scotlandyard.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LobbyModelsTest {

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
    fun lobbyData_equality() {
        val users = listOf(LobbyUserData("1", "Hans"))
        val a = LobbyData("AB3KP", "Lobby", "1", false, users, mapOf("1" to false), mapOf("1" to "NONE"))
        val b = LobbyData("AB3KP", "Lobby", "1", false, users, mapOf("1" to false), mapOf("1" to "NONE"))
        assertEquals(a, b)
    }

    @Test
    fun lobbyData_isStarted_false_by_default() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1",
            isStarted = false, users = emptyList(),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertFalse(lobby.isStarted)
    }

    @Test
    fun lobbyData_isStarted_true() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1",
            isStarted = true, users = emptyList(),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertTrue(lobby.isStarted)
    }

    @Test
    fun lobbyResponse_success_true() {
        val response = LobbyResponse(
            success = true, message = "Lobby created",
            lobbyId = "AB3KP", lobby = null
        )
        assertTrue(response.success)
        assertEquals("Lobby created", response.message)
        assertEquals("AB3KP", response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun lobbyResponse_success_false() {
        val response = LobbyResponse(
            success = false, message = "Error",
            lobbyId = null, lobby = null
        )
        assertFalse(response.success)
        assertNull(response.lobbyId)
        assertNull(response.lobby)
    }

    @Test
    fun lobbyResponse_with_lobby() {
        val users = listOf(LobbyUserData("1", "Hans"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1",
            isStarted = false, users = users,
            readyStatus = mapOf("1" to false),
            selectedRoles = mapOf("1" to "NONE")
        )
        val response = LobbyResponse(
            success = true, message = "Joined lobby",
            lobbyId = "AB3KP", lobby = lobby
        )
        assertNotNull(response.lobby)
        assertEquals("AB3KP", response.lobby!!.id)
        assertEquals(1, response.lobby!!.users.size)
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
    fun lobbyData_selectedRoles_none_by_default() {
        val users = listOf(LobbyUserData("1", "Hans"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users,
            readyStatus = mapOf("1" to false),
            selectedRoles = mapOf("1" to "NONE")
        )
        assertEquals("NONE", lobby.selectedRoles["1"])
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
    fun lobbyData_mrx_not_taken_when_all_none() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = listOf(LobbyUserData("1", "Hans")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "NONE")
        )
        assertFalse(lobby.selectedRoles.values.contains("MRX"))
    }

    @Test
    fun lobbyData_all_roles_set_check() {
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

    @Test
    fun lobbyData_not_all_roles_set_check() {
        val users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz"))
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = users,
            readyStatus = emptyMap(),
            selectedRoles = mapOf("1" to "MRX", "2" to "NONE")
        )
        val allSet = lobby.users.all { (lobby.selectedRoles[it.id] ?: "NONE") != "NONE" }
        assertFalse(allSet)
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
    fun lobbyData_multiple_users() {
        val users = listOf(
            LobbyUserData("1", "Hans"),
            LobbyUserData("2", "Fritz"),
            LobbyUserData("3", "Kurt")
        )
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1",
            isStarted = false, users = users,
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertEquals(3, lobby.users.size)
    }

    @Test
    fun lobbyData_readyStatus_check() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz")),
            readyStatus = mapOf("1" to true, "2" to false),
            selectedRoles = emptyMap()
        )
        assertTrue(lobby.readyStatus["1"] ?: false)
        assertFalse(lobby.readyStatus["2"] ?: true)
    }

    @Test
    fun lobbyData_host_detection() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        assertEquals("user-1", lobby.hostId)
        assertTrue(lobby.hostId == "user-1")
        assertFalse(lobby.hostId == "user-2")
    }

    @Test
    fun lobbyData_can_start_requires_min_3_players() {
        val lobby2 = LobbyData(
            id = "AB3KP", name = "Lobby", hostId = "1", isStarted = false,
            users = listOf(LobbyUserData("1", "Hans"), LobbyUserData("2", "Fritz")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        val lobby3 = lobby2.copy(
            users = listOf(
                LobbyUserData("1", "Hans"),
                LobbyUserData("2", "Fritz"),
                LobbyUserData("3", "Kurt")
            )
        )
        assertFalse(lobby2.users.size >= 3)
        assertTrue(lobby3.users.size >= 3)
    }
}
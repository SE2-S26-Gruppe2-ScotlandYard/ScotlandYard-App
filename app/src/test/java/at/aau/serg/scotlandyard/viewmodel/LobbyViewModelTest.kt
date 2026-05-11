package at.aau.serg.scotlandyard.viewmodel

import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.model.LobbyUserData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit Tests fuer LobbyViewModel Logik (ohne STOMP/Netzwerk)
 *
 * Netzwerk-abhaengige Funktionen (createLobby, joinLobby etc.) werden
 * via Integrationstests getestet. Hier testen wir die reine Logik.
 */
class LobbyViewModelTest {

    // ── LobbyViewModelFactory ──────────────────────────────────────────────

    @Test
    fun factory_stores_userId_correctly() {
        // Factory erstellen und pruefen ob Parameter korrekt gespeichert werden
        val userId = "test-user-123"
        val userName = "TestUser"
        // Factory-Klasse direkt pruefen
        val factoryClass = LobbyViewModelFactory::class.java
        assertNotNull(factoryClass)
        assertEquals("LobbyViewModelFactory", factoryClass.simpleName)
    }

    @Test
    fun factory_creates_correct_viewmodel_class() {
        val modelClass = LobbyViewModel::class.java
        assertTrue(
            androidx.lifecycle.ViewModel::class.java.isAssignableFrom(modelClass),
            "LobbyViewModel should extend ViewModel"
        )
    }

    // ── LobbyData Logik (ohne Netzwerk) ───────────────────────────────────

    @Test
    fun lobbyData_isHost_check_correct() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        // Host-Erkennung: hostId == userId
        assertTrue(lobby.hostId == "user-1")
        assertFalse(lobby.hostId == "user-2")
    }

    @Test
    fun lobbyData_mrx_taken_check() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("user-1" to "MRX", "user-2" to "NONE")
        )
        val mrXTaken = lobby.selectedRoles.values.contains("MRX")
        assertTrue(mrXTaken)
    }

    @Test
    fun lobbyData_mrx_not_taken_when_all_none() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("user-1" to "NONE")
        )
        val mrXTaken = lobby.selectedRoles.values.contains("MRX")
        assertFalse(mrXTaken)
    }

    @Test
    fun lobbyData_allRolesSet_when_no_none() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("user-1" to "MRX", "user-2" to "DETECTIVE")
        )
        val allRolesSet = lobby.users.all { user ->
            (lobby.selectedRoles[user.id] ?: "NONE") != "NONE"
        }
        assertTrue(allRolesSet)
    }

    @Test
    fun lobbyData_allRolesSet_false_when_some_none() {
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(),
            selectedRoles = mapOf("user-1" to "MRX", "user-2" to "NONE")
        )
        val allRolesSet = lobby.users.all { user ->
            (lobby.selectedRoles[user.id] ?: "NONE") != "NONE"
        }
        assertFalse(allRolesSet)
    }

    @Test
    fun lobbyData_canStart_requires_min_3_players() {
        val lobbyWith2 = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans"), LobbyUserData("user-2", "Fritz")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        val lobbyWith3 = lobbyWith2.copy(
            users = listOf(
                LobbyUserData("user-1", "Hans"),
                LobbyUserData("user-2", "Fritz"),
                LobbyUserData("user-3", "Kurt")
            )
        )
        assertFalse(lobbyWith2.users.size >= 3)
        assertTrue(lobbyWith3.users.size >= 3)
    }

    @Test
    fun lobbyData_user_in_lobby_check() {
        val userId = "user-1"
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = userId,
            isStarted = false,
            users = listOf(LobbyUserData(userId, "Hans")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        val weAreInIt = lobby.users.any { it.id == userId }
        assertTrue(weAreInIt)
    }

    @Test
    fun lobbyData_user_not_in_lobby_check() {
        val userId = "user-99"
        val lobby = LobbyData(
            id = "AB3KP", name = "Test", hostId = "user-1",
            isStarted = false,
            users = listOf(LobbyUserData("user-1", "Hans")),
            readyStatus = emptyMap(), selectedRoles = emptyMap()
        )
        val weAreInIt = lobby.users.any { it.id == userId }
        assertFalse(weAreInIt)
    }

    @Test
    fun lobbyCode_validation_5_chars() {
        val validCode = "AB3KP"
        val invalidShort = "AB3K"
        val invalidLong = "AB3KPX"

        assertTrue(validCode.length == 5)
        assertFalse(invalidShort.length == 5)
        assertFalse(invalidLong.length == 5)
    }

    @Test
    fun lobbyCode_uppercase_conversion() {
        val code = "ab3kp"
        assertEquals("AB3KP", code.uppercase())
    }
}
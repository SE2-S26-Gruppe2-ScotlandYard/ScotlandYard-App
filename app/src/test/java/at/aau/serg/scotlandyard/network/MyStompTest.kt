package at.aau.serg.scotlandyard.network

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for MyStomp – verifies API surface via reflection (no Android runtime needed).
 */
class MyStompTest {

    @Test
    fun myStomp_class_exists() {
        assertNotNull(MyStomp::class)
    }

    @Test
    fun subscribeToStartPosition_method_has_correct_signature() {
        val methods = MyStomp::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "subscribeToStartPosition" && it.parameterCount == 2
        }, "subscribeToStartPosition(gameId, playerId) must exist")
    }

    @Test
    fun unsubscribeFromStartPosition_method_exists_with_no_parameters() {
        val method = MyStomp::class.java.declaredMethods
            .firstOrNull { it.name == "unsubscribeFromStartPosition" }
        assertNotNull(method, "unsubscribeFromStartPosition() must be declared in MyStomp")
        assertEquals(0, method!!.parameterCount,
            "unsubscribeFromStartPosition should take no parameters")
    }

    @Test
    fun startPositionJob_field_is_private() {
        val field = MyStomp::class.java.declaredFields
            .firstOrNull { it.name == "startPositionJob" }
        assertNotNull(field, "startPositionJob field must exist in MyStomp")
        assertTrue(java.lang.reflect.Modifier.isPrivate(field!!.modifiers),
            "startPositionJob should be private")
    }

    @Test
    fun requestStartPosition_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "requestStartPosition" && it.parameterCount == 2
        }, "requestStartPosition(gameId, playerId) must exist")
    }

    @Test
    fun sendConfirmedStartPosition_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "sendConfirmedStartPosition" && it.parameterCount == 3
        }, "sendConfirmedStartPosition(gameId, playerId, position) must exist")
    }

    @Test
    fun requestGameState_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "requestGameState" && it.parameterCount == 1
        }, "requestGameState(gameId) must exist")
    }

    @Test
    fun connectToGame_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "connectToGame" && it.parameterCount == 1
        }, "connectToGame(gameId) must exist")
    }

    @Test
    fun connect_method_exists() {
        assertTrue(MyStomp::class.java.declaredMethods.any { it.name == "connect" })
    }

    @Test
    fun shutdown_method_exists() {
        assertTrue(MyStomp::class.java.declaredMethods.any { it.name == "shutdown" })
    }

    @Test
    fun isConnected_stateflow_is_exposed() {
        val field = MyStomp::class.java.declaredFields
            .firstOrNull { it.name == "isConnected" }
        // Kotlin property: backing field may be named differently; check via public getter/property
        val methods = MyStomp::class.java.methods
        assertTrue(
            field != null || methods.any { it.name == "getIsConnected" },
            "isConnected StateFlow must be exposed"
        )
    }

    @Test
    fun sendUserConnect_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "sendUserConnect" && it.parameterCount == 2
        }, "sendUserConnect(nickname, userId) must exist")
    }

    @Test
    fun sendRenameUser_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "sendRenameUser" && it.parameterCount == 2
        }, "sendRenameUser(userId, newNickname) must exist")
    }

    @Test
    fun sendKickPlayerInGame_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "sendKickPlayerInGame" && it.parameterCount == 3
        }, "sendKickPlayerInGame(gameId, requesterId, targetId) must exist")
    }

    @Test
    fun sendDeleteGame_method_has_correct_signature() {
        assertTrue(MyStomp::class.java.declaredMethods.any {
            it.name == "sendDeleteGame" && it.parameterCount == 2
        }, "sendDeleteGame(gameId, requesterId) must exist")
    }
}


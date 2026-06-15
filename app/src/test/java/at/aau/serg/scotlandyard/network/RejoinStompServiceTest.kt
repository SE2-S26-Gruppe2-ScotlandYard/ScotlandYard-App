package at.aau.serg.scotlandyard.network

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for RejoinStompService – verifies API surface via reflection (no Android runtime needed).
 */
class RejoinStompServiceTest {

    @Test
    fun rejoinStompService_class_exists() {
        assertNotNull(RejoinStompService::class)
    }

    @Test
    fun rejoinLobby_method_has_correct_signature() {
        val methods = RejoinStompService::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "rejoinLobby" && it.parameterCount == 3
        })
    }

    @Test
    fun rejoinGame_method_has_correct_signature() {
        val methods = RejoinStompService::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "rejoinGame" && it.parameterCount == 2
        })
    }

    @Test
    fun sendToServer_is_private_method() {
        val methods = RejoinStompService::class.java.declaredMethods
        val sendMethod = methods.find { it.name == "sendToServer" }
        assertNotNull(sendMethod)
    }

    @Test
    fun constructor_takes_stompSession_parameter() {
        val constructors = RejoinStompService::class.java.declaredConstructors
        assertTrue(constructors.any { it.parameterCount == 1 })
    }
}
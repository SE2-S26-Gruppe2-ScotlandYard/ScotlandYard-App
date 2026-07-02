package at.aau.serg.scotlandyard.viewmodel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AuthViewModelTest {

    @Test
    fun authViewModel_class_exists() {
        assertNotNull(AuthViewModel::class)
    }

    @Test
    fun authViewModel_extends_android_lifecycle_viewmodel() {
        assertTrue(
            AuthViewModel::class.java.superclass?.simpleName == "AndroidViewModel",
            "AuthViewModel should extend AndroidViewModel (needs Application context for SharedPreferences)"
        )
    }

    @Test
    fun authViewModel_implements_callbacks() {
        assertTrue(AuthViewModel::class.java.interfaces.any {
            it.simpleName == "Callbacks"
        }, "AuthViewModel should implement Callbacks interface")
    }

    @Test
    fun connectUser_method_has_correct_signature() {
        val methods = AuthViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "connectUser" && it.parameterCount == 1
        }, "connectUser(nickname) must exist")
    }

    @Test
    fun tryAutoConnect_method_exists_with_no_parameters_and_returns_boolean() {
        val method = AuthViewModel::class.java.declaredMethods
            .firstOrNull { it.name == "tryAutoConnect" }
        assertNotNull(method, "tryAutoConnect() must exist so Start Game / Account tab can " +
                "attempt a silent reconnect using the locally saved nickname + userId")
        assertEquals(0, method!!.parameterCount, "tryAutoConnect should take no parameters")
        assertEquals(Boolean::class.javaPrimitiveType, method.returnType,
            "tryAutoConnect should return Boolean (true if an attempt was started)")
    }

    @Test
    fun reconnect_method_exists_with_no_parameters() {
        val method = AuthViewModel::class.java.declaredMethods
            .firstOrNull { it.name == "reconnect" }
        assertNotNull(method, "reconnect() must exist")
        assertEquals(0, method!!.parameterCount)
    }

    @Test
    fun renameNickname_method_has_correct_signature() {
        val methods = AuthViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "renameNickname" && it.parameterCount == 1
        }, "renameNickname(newNickname) must exist so the user can change their own nickname " +
                "from the Account settings tab")
    }

    @Test
    fun clearRejoinEvent_method_exists_with_no_parameters() {
        val method = AuthViewModel::class.java.declaredMethods
            .firstOrNull { it.name == "clearRejoinEvent" }
        assertNotNull(method, "clearRejoinEvent() must exist")
        assertEquals(0, method!!.parameterCount)
    }

    @Test
    fun rejoinEvent_enum_has_lobby_and_game_constants() {
        val constants = RejoinEvent::class.java.enumConstants.map { it.name }
        assertTrue(constants.contains("LOBBY"), "RejoinEvent must contain LOBBY")
        assertTrue(constants.contains("GAME"), "RejoinEvent must contain GAME")
        assertEquals(2, constants.size, "RejoinEvent should only have LOBBY and GAME")
    }

    @Test
    fun currentUser_stateflow_is_exposed() {
        val methods = AuthViewModel::class.java.methods
        assertTrue(methods.any { it.name == "getCurrentUser" }, "currentUser StateFlow must be exposed")
    }

    @Test
    fun errorMessage_stateflow_is_exposed() {
        val methods = AuthViewModel::class.java.methods
        assertTrue(methods.any { it.name == "getErrorMessage" }, "errorMessage StateFlow must be exposed")
    }

    @Test
    fun rejoinEvent_stateflow_is_exposed() {
        val methods = AuthViewModel::class.java.methods
        assertTrue(methods.any { it.name == "getRejoinEvent" }, "rejoinEvent StateFlow must be exposed")
    }

    @Test
    fun getMyStomp_method_exists() {
        assertTrue(AuthViewModel::class.java.declaredMethods.any { it.name == "getMyStomp" },
            "getMyStomp() must exist so RoleSelection/Lobby screens can build a LobbyStompService")
    }

    @Test
    fun onResponse_method_has_correct_signature() {
        val methods = AuthViewModel::class.java.declaredMethods
        assertTrue(methods.any {
            it.name == "onResponse" && it.parameterCount == 1 &&
                    it.parameterTypes[0] == String::class.java
        }, "onResponse(String) must exist (Callbacks interface implementation)")
    }
}

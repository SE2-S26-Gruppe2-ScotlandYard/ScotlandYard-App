package at.aau.serg.scotlandyard.network

import android.util.Log
import at.aau.serg.scotlandyard.model.LobbyResponse
import at.aau.serg.scotlandyard.model.toLobbyResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val TAG = "LobbyStompService"

class LobbyStompService(private val session: StompSession) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _lobbyResponse = MutableStateFlow<LobbyResponse?>(null)
    val lobbyResponse: StateFlow<LobbyResponse?> = _lobbyResponse.asStateFlow()

    // MyStomp subscribed bereits auf /topic/lobby – wir nutzen dessen Callback
    fun subscribe(myStomp: MyStomp? = null) {
        myStomp?.setLobbyCallback { msg ->
            android.util.Log.d("LOBBY_DEBUG", "LobbyStompService received: $msg")
            try {
                val response = msg.toLobbyResponse()
                _lobbyResponse.value = response
            } catch (e: Exception) {
                Log.e(TAG, "Parse error: $msg", e)
            }
        }
    }

    fun unsubscribe(myStomp: MyStomp? = null) {
        myStomp?.setLobbyCallback(null)
    }

    fun createLobby(lobbyName: String, userId: String, nickName: String) {
        sendToServer("/app/lobby/create", JSONObject().apply {
            put("lobbyName", lobbyName)
            put("userId", userId)
            put("nickName", nickName)
        })
    }

    fun joinLobby(lobbyId: String, userId: String, nickName: String) {
        sendToServer("/app/lobby/join", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("userId", userId)
            put("nickName", nickName)
        })
    }

    fun leaveLobby(lobbyId: String, userId: String) {
        sendToServer("/app/lobby/leave", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("userId", userId)
        })
    }

    fun deleteLobby(lobbyId: String, requesterId: String) {
        sendToServer("/app/lobby/delete", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", requesterId)
        })
    }

    fun kickPlayer(lobbyId: String, requesterId: String, targetUserId: String) {
        sendToServer("/app/lobby/kick", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", requesterId)
            put("targetUserId", targetUserId)
        })
    }

    fun setRole(lobbyId: String, requesterId: String, targetUserId: String, role: String) {
        sendToServer("/app/lobby/setRole", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", requesterId)
            put("targetUserId", targetUserId)
            put("role", role)
        })
    }

    fun startRoleSelection(lobbyId: String, requesterId: String) {
        sendToServer("/app/lobby/startRoleSelection", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", requesterId)
        })
    }

    fun startGame(lobbyId: String, requesterId: String) {
        sendToServer("/app/lobby/startGame", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", requesterId)
        })
    }

    private fun sendToServer(destination: String, json: JSONObject) {
        scope.launch {
            try {
                session.sendText(destination, json.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Send failed to $destination", e)
            }
        }
    }
}
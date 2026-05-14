package at.aau.serg.scotlandyard.network

import android.util.Log
import at.aau.serg.scotlandyard.model.LobbyResponse
import at.aau.serg.scotlandyard.model.toLobbyResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.json.JSONObject

private const val TAG = "LobbyStompService"

class LobbyStompService(
    private val session: StompSession,
    private val userId: String,
    private val myStomp: MyStomp
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastKnownHostName: String = "Host"

    private val _lobbyResponse = MutableStateFlow<LobbyResponse?>(null)
    val lobbyResponse: StateFlow<LobbyResponse?> = _lobbyResponse.asStateFlow()

    private var specificLobbyJob: Job? = null
    private var privateCollectJob: Job? = null

    fun subscribe() {
        myStomp.setLobbyCallback { msg ->
            Log.d("LOBBY_DEBUG", "Global: $msg")
            handleIncomingMessage(msg)
        }
        privateCollectJob = scope.launch {
            myStomp.privateMessages.collect { msg ->
                handleIncomingMessage(msg)
            }
        }
    }

    fun unsubscribe() {
        myStomp.setLobbyCallback(null)
        privateCollectJob?.cancel()
        unsubscribeSpecificLobby()
    }

    fun subscribeToSpecificLobby(lobbyId: String) {
        specificLobbyJob?.cancel()
        specificLobbyJob = scope.launch {
            try {
                session.subscribeText("/topic/lobby/$lobbyId").collect { msg ->
                    handleIncomingMessage(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Specific lobby subscription error", e)
            }
        }
    }

    fun unsubscribeSpecificLobby() {
        specificLobbyJob?.cancel()
        specificLobbyJob = null
    }

    private fun handleIncomingMessage(msg: String) {
        try {
            var response = msg.toLobbyResponse()
            response.lobby?.let { lobby ->
                val hostUser = lobby.users?.find { it.id == lobby.hostId }
                if (hostUser != null) lastKnownHostName = hostUser.name
            }
            val customMessage = response.message
            response = response.copy(message = customMessage)
            _lobbyResponse.value = response
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: $msg", e)
        }
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
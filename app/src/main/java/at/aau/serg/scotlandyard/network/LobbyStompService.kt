package at.aau.serg.scotlandyard.network

import android.util.Log
import at.aau.serg.scotlandyard.model.LobbyResponse
import at.aau.serg.scotlandyard.model.toLobbyResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.json.JSONObject

private const val TAG = "LobbyStompService"

class LobbyStompService(
    private val session: StompSession,
    private val myStomp: MyStomp
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _lobbyResponse = MutableSharedFlow<LobbyResponse>(extraBufferCapacity = 1)
    val lobbyResponse: SharedFlow<LobbyResponse> = _lobbyResponse.asSharedFlow()

    private val _sendError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sendError: SharedFlow<String> = _sendError.asSharedFlow()

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
            _lobbyResponse.tryEmit(msg.toLobbyResponse())
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
                _sendError.tryEmit("Send failed: connection error")
            }
        }
    }
}
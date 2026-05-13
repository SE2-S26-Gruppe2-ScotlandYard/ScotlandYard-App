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

class LobbyStompService(private val session: StompSession, private val userId: String) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var globalSubscriptionJob: Job? = null
    private var specificLobbyJob: Job? = null

    // Cache für den Host-Namen. Standard-Fallback
    private var lastKnownHostName: String = "Host"

    private val _lobbyResponse = MutableStateFlow<LobbyResponse?>(null)
    val lobbyResponse: StateFlow<LobbyResponse?> = _lobbyResponse.asStateFlow()

    fun subscribe() {
        globalSubscriptionJob = scope.launch {
            // 1. Globale Lobby-Events (für den Server-Browser)
            launch {
                try {
                    session.subscribeText("/topic/lobby").collect { msg ->
                        Log.d("LOBBY_DEBUG", "Global Lobby update received: $msg")
                        handleIncomingMessage(msg)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Global Subscription error", e)
                }
            }

            // 2. Private User-Events über das dedizierte Topic
            launch {
                try {
                    session.subscribeText("/topic/player/$userId").collect { msg ->
                        Log.d("LOBBY_DEBUG", "Private User response received: $msg")
                        handleIncomingMessage(msg)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Private Subscription error", e)
                }
            }
        }
    }

    private fun handleIncomingMessage(msg: String) {
        try {
            var response = msg.toLobbyResponse()

            // Host-Namen immer aktuell halten, solange wir gültige Lobby-Daten erhalten
            response.lobby?.let { lobby ->
                val hostUser = lobby.users.find { it.id == lobby.hostId }
                if (hostUser != null) {
                    lastKnownHostName = hostUser.name
                }
            }

            // Automatisches Abonnieren der spezifischen Lobby nach erfolgreichem Beitritt
            if (response.success && response.lobbyId != null) {
                if (response.message == "Joined lobby" || response.message == "Lobby created") {
                    subscribeToSpecificLobby(response.lobbyId)
                } else if (response.message == "Left lobby" || response.message == "Lobby deleted" || response.message == "Lobby deleted (empty)" || response.message == "Player kicked") {
                    unsubscribeFromSpecificLobby()
                }
            }

            // Ersetzt generische Server-Texte durch spezifische, auf den Host bezogene Texte
            val lobbyName = "${lastKnownHostName}'s Lobby"
            val customMessage = when (response.message) {
                "Lobby created" -> "$lobbyName created"
                "Joined lobby" -> "Joined $lobbyName"
                "Left lobby" -> "Left $lobbyName"
                "Lobby deleted" -> "$lobbyName deleted"
                "Lobby deleted (empty)" -> "$lobbyName deleted (empty)"
                else -> response.message // Unveränderte Originalnachricht (z.B. Fehler oder "Player kicked")
            }

            response = response.copy(message = customMessage)

            _lobbyResponse.value = response
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: $msg", e)
        }
    }

    private fun subscribeToSpecificLobby(lobbyId: String) {
        if (specificLobbyJob?.isActive == true) return // Bereits abonniert

        specificLobbyJob = scope.launch {
            try {
                session.subscribeText("/topic/lobby/$lobbyId").collect { msg ->
                    Log.d("LOBBY_DEBUG", "Specific Lobby update received ($lobbyId): $msg")
                    handleIncomingMessage(msg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Specific Lobby Subscription error", e)
            }
        }
    }

    private fun unsubscribeFromSpecificLobby() {
        specificLobbyJob?.cancel()
        specificLobbyJob = null
    }

    fun unsubscribe() {
        globalSubscriptionJob?.cancel()
        unsubscribeFromSpecificLobby()
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
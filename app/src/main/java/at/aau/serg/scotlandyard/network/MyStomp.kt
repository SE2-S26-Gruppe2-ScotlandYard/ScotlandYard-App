package at.aau.serg.scotlandyard.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.scotlandyard.Callbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/scotlandyard"

class MyStomp(val callbacks: Callbacks) {

    private lateinit var client: StompClient
    private var session: StompSession? = null

    fun getSession(): StompSession? = session

    private var currentUserId: String? = null
    private var currentGameId: String? = null
    private var lobbyCallback: ((String) -> Unit)? = null

    fun setLobbyCallback(callback: ((String) -> Unit)?) {
        lobbyCallback = callback
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val errorMsg: String = "Error: Not connected"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun connect() {
        client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            var isSuccessfullyConnected = false

            while (!isSuccessfullyConnected) {
                try {
                    Log.d("MyStomp", "Versuche Verbindung zum Server...")
                    val activeSession = client.connect(WEBSOCKET_URI)
                    session = activeSession
                    _isConnected.value = true

                    subscribeToServer(activeSession)

                    isSuccessfullyConnected = true
                    callback("connected to server")
                    Log.d("MyStomp", "Verbindung erfolgreich hergestellt.")

                } catch (e: Exception) {
                    Log.e("MyStomp", "Verbindung fehlgeschlagen. Erneuter Versuch in 5 Sekunden...", e)
                    _isConnected.value = false
                    kotlinx.coroutines.delay(5000)
                }
            }
        }
    }

    private fun subscribeToServer(activeSession: StompSession) {
        scope.launch {
            try {
                activeSession.subscribeText("/topic/hello-response").collect { msg ->
                    callback(msg)
                }
            } catch (e: Exception) { handleDisconnect() }
        }

        scope.launch {
            try {
                activeSession.subscribeText("/topic/rcv-object").collect { msg ->
                    val o = JSONObject(msg)
                    callback(o.get("text").toString())
                }
            } catch (e: Exception) { handleDisconnect() }
        }

        // Exklusives Topic nur für den allerersten Login.
        // Geht direkt in das AuthViewModel.
        scope.launch {
            try {
                activeSession.subscribeText("/user/topic/user-response").collect { msg ->
                    callback(msg)
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "User response subscription error", e)
                handleDisconnect()
            }
        }

        // Globale Lobby Updates (Empfängt nur noch Lobby created/deleted, keine privaten Daten mehr)
        scope.launch {
            try {
                activeSession.subscribeText("/topic/lobby").collect { msg ->
                    Log.d("LOBBY_DEBUG", "Global lobby update: $msg")
                    lobbyCallback?.invoke(msg)
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "Lobby subscription error", e)
                handleDisconnect()
            }
        }
    }

    private fun handleDisconnect() {
        if (_isConnected.value) {
            _isConnected.value = false
            callback("Connection lost. Reconnecting...")
            connect()
        }
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }

    fun sendUserConnect(nickname: String) {
        val json = JSONObject()
        json.put("nickName", nickname)

        scope.launch {
            try {
                session?.sendText("/app/user/connect", json.toString()) ?: callback(errorMsg)
            } catch (e: Exception) {
                Log.e("MyStomp", "Send UserConnect failed", e)
            }
        }
    }

    fun sendHello() {
        scope.launch {
            try {
                session?.let {
                    it.sendText("/app/hello", "message from client")
                } ?: run {
                    callback(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "Send failed", e)
            }
        }
    }

    fun sendJson() {
        val json = JSONObject()
        json.put("from", "client")
        json.put("text", "from client")

        scope.launch {
            try {
                session?.sendText("/app/object", json.toString()) ?: callback(errorMsg)
            } catch (e: Exception) {
                Log.e("MyStomp", "Send JSON failed", e)
            }
        }
    }

    fun connectToGame(gameId: String) {
        currentGameId = gameId
        scope.launch {
            try {
                if (session == null) {
                    val activeSession = client.connect(WEBSOCKET_URI)
                    session = activeSession
                }

                scope.launch {
                    try {
                        session?.subscribeText("/topic/game/$gameId/movements")?.collect { msg ->
                            callback("movement:$msg")
                        }
                    } catch (e: Exception) {
                        Log.e("MyStomp", "Movement subscription failed", e)
                    }
                }

                scope.launch {
                    try {
                        session?.subscribeText("/topic/game/$gameId/move-response")?.collect { msg ->
                            callback("move-response:$msg")
                        }
                    } catch (e: Exception) {
                        Log.e("MyStomp", "Move response subscription failed", e)
                    }
                }

                scope.launch {
                    try {
                        session?.subscribeText("/topic/game/$gameId/over")?.collect { msg ->
                            callback("game-over:$msg")
                        }
                    } catch (e: Exception) {
                        Log.e("MyStomp", "Game over subscription failed", e)
                    }
                }

                callback("connected to:$gameId")

            } catch (e: Exception) {
                Log.e("MyStomp", "Failed to connect to game", e)
                callback("Connection error")
            }
        }
    }

    fun sendMove(gameId: String, playerId: String, ticket: String, targetPosition: Int) {
        val json = JSONObject().apply {
            put("gameId", gameId)
            put("playerId", playerId)
            put("ticket", ticket)
            put("targetPosition", targetPosition)
            put("timestamp", System.currentTimeMillis())
        }

        scope.launch {
            try {
                session?.sendText("/app/game/$gameId/move", json.toString())
                    ?: callback(errorMsg)
            } catch (e: Exception) {
                Log.e("MyStomp", "Send move failed", e)
            }
        }
    }
}
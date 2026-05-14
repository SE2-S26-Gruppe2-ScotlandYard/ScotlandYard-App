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

private const val WEBSOCKET_URI = "ws://10.233.0.89:8080/scotlandyard"

class MyStomp(val callbacks: Callbacks) {

    private lateinit var client: StompClient
    private var session: StompSession? = null

    fun getSession(): StompSession? = session

    // ── NEU: Lobby-Callback ───────────────────────────────────────────────
    private var lobbyCallback: ((String) -> Unit)? = null

    fun setLobbyCallback(callback: ((String) -> Unit)?) {
        lobbyCallback = callback
    }
    // ─────────────────────────────────────────────────────────────────────

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var currentGameId: String? = null
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
        // Hello
        scope.launch {
            try {
                activeSession.subscribeText("/topic/hello-response").collect { msg ->
                    callback(msg)
                }
            } catch (e: Exception) { handleDisconnect() }
        }

        // JSON
        scope.launch {
            try {
                activeSession.subscribeText("/topic/rcv-object").collect { msg ->
                    val o = JSONObject(msg)
                    callback(o.get("text").toString())
                }
            } catch (e: Exception) { handleDisconnect() }
        }

        // User
        scope.launch {
            try {
                activeSession.subscribeText("/topic/user-response").collect { msg ->
                    callback(msg)
                }
            } catch (e: Exception) { handleDisconnect() }
        }

        // Lobby
        scope.launch {
            try {
                activeSession.subscribeText("/topic/lobby").collect { msg ->
                    Log.d("LOBBY_DEBUG", "MyStomp received: $msg")
                    lobbyCallback?.invoke(msg)
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "Lobby subscription error", e)
                handleDisconnect()
            }
        }
        // ─────────────────────────────────────────────────────────────────
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
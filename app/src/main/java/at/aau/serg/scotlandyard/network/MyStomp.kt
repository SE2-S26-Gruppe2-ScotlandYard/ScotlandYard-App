package at.aau.serg.scotlandyard.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.scotlandyard.Callbacks
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/scotlandyard"

class MyStomp(val callbacks: Callbacks) {

    private var client: StompClient? = null
    private var session: StompSession? = null
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Flows für private Nachrichten
    private val _privateMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val privateMessages: SharedFlow<String> = _privateMessages.asSharedFlow()

    private var privateTopicJob: Job? = null

    /**
     * Aktiviert das private Topic und stellt Nachrichten über privateMessages bereit.
     * Wird vom AuthViewModel nach erfolgreichem Login aufgerufen.
     */
    fun enablePrivateTopic(userId: String) {
        currentUserId = userId
        privateTopicJob?.cancel()
        privateTopicJob = scope.launch {
            session?.subscribeText("/topic/player/$userId")?.collect { msg ->
                _privateMessages.emit(msg)
            }
        }
    }

    fun connect() {
        // Alte Verbindung und Jobs sauber beenden
        disconnect()
        client = StompClient(OkHttpWebSocketClient())
        connectionJob = scope.launch {
            while (isActive) {
                try {
                    Log.d("MyStomp", "Versuche Verbindung zum Server...")
                    val activeSession = client!!.connect(WEBSOCKET_URI)
                    session = activeSession
                    _isConnected.value = true
                    subscribeToServer(activeSession)
                    callback("connected to server")
                    Log.d("MyStomp", "Verbindung erfolgreich.")
                    break
                } catch (e: Exception) {
                    Log.e("MyStomp", "Verbindung fehlgeschlagen, neuer Versuch in 5s", e)
                    _isConnected.value = false
                    delay(5000)
                }
            }
        }
    }

    private fun disconnect() {
        connectionJob?.cancel()
        privateTopicJob?.cancel()
        session = null
        _isConnected.value = false
    }

    private fun subscribeToServer(activeSession: StompSession) {
        scope.launch {
            try {
                activeSession.subscribeText("/topic/hello-response").collect { callback(it) }
            } catch (_: Exception) { handleDisconnect() }
        }
        scope.launch {
            try {
                activeSession.subscribeText("/topic/rcv-object").collect { msg ->
                    val o = JSONObject(msg)
                    callback(o.get("text").toString())
                }
            } catch (_: Exception) { handleDisconnect() }
        }
        scope.launch {
            try {
                activeSession.subscribeText("/user/topic/user-response").collect { msg ->
                    callback(msg)
                }
            } catch (_: Exception) { handleDisconnect() }
        }
        scope.launch {
            try {
                activeSession.subscribeText("/topic/lobby").collect { msg ->
                    Log.d("LOBBY_DEBUG", "Global lobby update: $msg")
                    lobbyCallback?.invoke(msg)
                }
            } catch (_: Exception) { handleDisconnect() }
        }
        // Falls bereits eine userId bekannt ist, privates Topic sofort abonnieren
        currentUserId?.let { enablePrivateTopic(it) }
    }

    private fun handleDisconnect() {
        if (_isConnected.value) {
            _isConnected.value = false
            callback("Connection lost. Reconnecting...")
            disconnect()
            connect()
        }
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }

    fun sendUserConnect(nickname: String) {
        val json = JSONObject().apply { put("nickName", nickname) }
        scope.launch {
            try {
                session?.sendText("/app/user/connect", json.toString()) ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "Send UserConnect failed", e)
            }
        }
    }

    fun sendHello() {
        scope.launch {
            try {
                session?.sendText("/app/hello", "message from client") ?: callback("Error: Not connected")
            } catch (_: Exception) { }
        }
    }

    fun sendJson() {
        val json = JSONObject().apply {
            put("from", "client")
            put("text", "from client")
        }
        scope.launch {
            try {
                session?.sendText("/app/object", json.toString()) ?: callback("Error: Not connected")
            } catch (_: Exception) { }
        }
    }

    fun connectToGame(gameId: String) {
        currentGameId = gameId
        scope.launch {
            if (session == null) {
                client?.let { session = it.connect(WEBSOCKET_URI) }
            }
            session?.let { s ->
                launch { s.subscribeText("/topic/game/$gameId/movements").collect { callback("movement:$it") } }
                launch { s.subscribeText("/topic/game/$gameId/move-response").collect { callback("move-response:$it") } }
                launch { s.subscribeText("/topic/game/$gameId/over").collect { callback("game-over:$it") } }
                callback("connected to:$gameId")
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
                session?.sendText("/app/game/$gameId/move", json.toString()) ?: callback("Error: Not connected")
            } catch (_: Exception) { }
        }
    }
}
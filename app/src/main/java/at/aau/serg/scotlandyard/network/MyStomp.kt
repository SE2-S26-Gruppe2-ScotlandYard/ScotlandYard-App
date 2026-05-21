package at.aau.serg.scotlandyard.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.ServerConfig.DEVICE_URI
import at.aau.serg.scotlandyard.network.ServerConfig.GLOBAL_URI
import at.aau.serg.scotlandyard.network.ServerConfig.LOCAL_URI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

//private const val WEBSOCKET_URI = GLOBAL_URI
//private const val WEBSOCKET_URI = LOCAL_URI   // ← Emulator (10.0.2.2)
private const val WEBSOCKET_URI = DEVICE_URI    // ← Physisches Gerät (143.205.192.169)

class MyStomp(val callbacks: Callbacks) {

    private var client: StompClient? = null
    private var session: StompSession? = null
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getSession(): StompSession? = session

    private var currentUserId: String? = null
    private var currentGameId: String? = null
    private var lobbyCallback: ((String) -> Unit)? = null
    private var gameStateCallback: ((String) -> Unit)? = null
    private var movementCallback: ((String) -> Unit)? = null
    private var gameOverCallback: ((String) -> Unit)? = null

    fun setLobbyCallback(callback: ((String) -> Unit)?) {
        lobbyCallback = callback
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun setGameStateCallback(callback: ((String) -> Unit)?) {
        gameStateCallback = callback
    }
    fun setMovementCallback(callback: ((String) -> Unit)?) {
        movementCallback = callback
    }
    fun setGameOverCallback(callback: ((String) -> Unit)?) {
        gameOverCallback = callback
    }

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _privateMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val privateMessages: SharedFlow<String> = _privateMessages.asSharedFlow()

    private var privateTopicJob: Job? = null
    private var startPositionJob: Job? = null

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

    fun shutdown() {
        disconnect()
        scope.cancel()
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
            // Wait until the primary connection is ready (up to 15 s)
            if (!_isConnected.value) {
                withTimeoutOrNull(15_000L) { _isConnected.first { it } }
            }
            val s = session ?: run {
                Log.e("MyStomp", "connectToGame: no session after waiting")
                return@launch
            }
            launch {
                s.subscribeText("/topic/game/$gameId/movements").collect { msg ->
                    gameStateCallback?.invoke(msg) ?: callback("movement:$msg")
                }
            }
            launch {
                s.subscribeText("/topic/game/$gameId/move-response").collect { msg ->
                    movementCallback?.invoke(msg) ?: callback("move-response:$msg")
                }
            }
            launch {
                s.subscribeText("/topic/game/$gameId/over").collect { msg ->
                    gameOverCallback?.invoke(msg) ?: callback("game-over:$msg")
                }
            }
            callback("connected to:$gameId")
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

    /**
     * Subscribe to start position assignments for a specific player
     * Subscribes to: /topic/game/{gameId}/player/{playerId}/start-position
     * Incoming messages are forwarded with prefix "startPosition:"
     */
    fun subscribeToStartPosition(gameId: String, playerId: String) {
        startPositionJob?.cancel()
        startPositionJob = scope.launch {
            try {
                val topic = "/topic/game/$gameId/player/$playerId/start-position"
                Log.d("MyStomp", "Subscribing to start position topic: $topic")
                session?.subscribeText(topic)?.collect { msg ->
                    callback("startPosition:$msg")
                } ?: run {
                    Log.w("MyStomp", "Cannot subscribe: Session is null")
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "Start position subscription failed", e)
            }
        }
    }

    fun unsubscribeFromStartPosition() {
        startPositionJob?.cancel()
        startPositionJob = null
        Log.d("MyStomp", "Unsubscribed from start position topic")
    }

    /**
     * Request start position from the backend
     * Sends JSON to: /app/game/start-position/request
     * Backend will respond via subscribed topic: /topic/game/{gameId}/player/{playerId}/start-position
     */
    fun requestStartPosition(gameId: String, playerId: String) {
        val json = JSONObject().apply {
            put("gameId", gameId)
            put("playerId", playerId)
        }

        scope.launch {
            try {
                Log.d("MyStomp", "Requesting start position for game=$gameId, player=$playerId")
                session?.sendText("/app/game/start-position/request", json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "Start position request failed", e)
            }
        }
    }

    fun requestGameState(gameId: String) {
        scope.launch {
            try {
                // Wait for connection if not ready yet (up to 15 s)
                if (!_isConnected.value) {
                    withTimeoutOrNull(15_000L) { _isConnected.first { it } }
                }
                session?.sendText("/app/game/$gameId/state", "")
                    ?: Log.w("MyStomp", "Cannot request game state: no session after waiting")
            } catch (e: Exception) {
                Log.e("MyStomp", "requestGameState failed", e)
            }
        }
    }

    /**
     * Send the confirmed start position to the backend.
     * Used for both normal mode (auto-generated) and cheat mode (user-selected).
     * Sends JSON to: /app/game/start-position/confirm
     */
    fun sendConfirmedStartPosition(gameId: String, playerId: String, position: Int) {
        val json = JSONObject().apply {
            put("gameId", gameId)
            put("playerId", playerId)
            put("startPosition", position)
        }
        scope.launch {
            try {
                Log.d("MyStomp", "Confirming start position=$position for game=$gameId, player=$playerId")
                session?.sendText("/app/game/start-position/confirm", json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "sendConfirmedStartPosition failed", e)
            }
        }
    }
}
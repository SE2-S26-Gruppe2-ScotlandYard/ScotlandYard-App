package at.aau.serg.scotlandyard.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.scotlandyard.Callbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/scotlandyard"

class MyStomp(val callbacks: Callbacks) {
    private var topicFlow: Flow<String>? = null
    private var collector: Job? = null
    private var jsonFlow: Flow<String>? = null
    private var jsonCollector: Job? = null

    private lateinit var client: StompClient
    private var session: StompSession? = null

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var currentGameId: String? = null
    private var movementFlow: Flow<String>? = null
    private var movementCollector: Job? = null
    private val errorMsg : String = "Error: Not connected"

    fun connect() {
        client = StompClient(OkHttpWebSocketClient()) // other config can be passed in here
        scope.launch {
            try {
                val activeSession = client.connect(WEBSOCKET_URI)
                session = activeSession

                // connect to topic
                topicFlow = activeSession.subscribeText("/topic/hello-response")
                collector = scope.launch {
                    topicFlow?.collect { msg ->
                        // TODO logic
                        callback(msg)
                    }
                }

                // connect to JSON topic
                jsonFlow = activeSession.subscribeText("/topic/rcv-object")
                jsonCollector = scope.launch {
                    jsonFlow?.collect { msg ->
                        val o = JSONObject(msg)
                        callback(o.get("text").toString())
                    }
                }
                callback("connected to server")

            } catch (e: Exception) {
                Log.e("MyStomp", "Connection failed", e)
                callback("Connection error")
            }
        }
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }

    fun sendHello() {
        scope.launch {
            try {
                session?.let {
                    Log.e("tag", "connecting to topic")
                    it.sendText("/app/hello", "message from client")
                } ?: run {
                    Log.e("MyStomp", "Cannot send: Session is null")
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
        val o = json.toString()

        scope.launch {
            try {
                session?.sendText("/app/object", o) ?: callback(errorMsg)
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

                // subscribe movement topic
                movementFlow = session?.subscribeText("/topic/game/$gameId/movements")
                movementCollector = scope.launch {
                    movementFlow?.collect { msg -> callback("movement:$msg") }
                }

                callback("connected to:$gameId")

            } catch (e: Exception) {
                Log.e("MyStomp", "Failed to connect to game", e)
                callback("Connection error")
            }
        }
    }

    fun sendMove(gameId: String, playerId: String, ticket: String, targetPosition: Int) {
        val json = JSONObject().apply {                                 // use json to transmit the data
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
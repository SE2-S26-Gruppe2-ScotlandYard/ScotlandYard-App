package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.dtos.GameStateDto
import at.aau.serg.scotlandyard.dtos.StartPositionResponse
import at.aau.serg.scotlandyard.model.BoardConnection
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.network.GameStompService
import at.aau.serg.scotlandyard.network.MyStomp
import at.aau.serg.scotlandyard.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel : ViewModel(), Callbacks {

    private val myStomp = MyStomp(this)
    val gameStompService = GameStompService(myStomp)

    // ── Start position ─────────────────────────────────────────────────────────
    private val _startPosition = MutableStateFlow<Int?>(null)
    val startPosition: StateFlow<Int?> = _startPosition.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _gameState = MutableStateFlow<GameStateDto?>(null)
    val gameState: StateFlow<GameStateDto?> = _gameState.asStateFlow()

    private val _myPosition = MutableStateFlow<Int?>(null)
    val myPosition: StateFlow<Int?> = _myPosition.asStateFlow()

    init {
        myStomp.connect()
        viewModelScope.launch {
            gameStompService.latestGameState.collect { state ->
                if (state != null) {
                    Log.d("PLAYER_DEBUG", "GameState received: detectives=${state.detectivePositions}, mrX=${state.mrXPosition}")
                    _gameState.value = state
                } else {
                    Log.d("PLAYER_DEBUG", "GameState is null")
                }
            }
        }
    }

    fun buildPlayerPositions(isMrX: Boolean, detectiveIdOrder: List<String>): Map<Color, Int> {
        val state = _gameState.value
        Log.d("PLAYER_DEBUG", "buildPlayerPositions called: isMrX=$isMrX, state=$state, detectiveIds=$detectiveIdOrder")
        if (state == null) return emptyMap()
        val result = buildMap {
            detectiveIdOrder.forEachIndexed { index, playerId ->
                val position = state.detectivePositions[playerId] ?: return@forEachIndexed
                val color = DETECTIVE_COLORS.getOrElse(index) { Color.Gray }
                put(color, position)
            }
            if (isMrX) {
                state.mrXPosition?.let { put(MRX_COLOR, it) }
            }
        }
        Log.d("PLAYER_DEBUG", "buildPlayerPositions result: $result")
        return result
    }

    fun reachableStations(ticket: TicketType): Set<Int> {
        val pos = _myPosition.value ?: return emptySet()
        return BoardConnection.reachableFrom(pos, ticket)
    }

    fun updateMyPosition(playerId: String, isMrX: Boolean) {
        val state = _gameState.value ?: return
        _myPosition.value = if (isMrX) state.mrXPosition
        else state.detectivePositions[playerId]
    }

    fun sendMove(gameId: String, playerId: String, ticket: TicketType, targetStation: Int) {
        gameStompService.sendMove(gameId, playerId, ticket.name, targetStation)
    }

    fun subscribeToStartPosition(gameId: String, playerId: String) {
        Log.d("GameViewModel", "Subscribing to start position topic for game=$gameId, player=$playerId")
        myStomp.subscribeToStartPosition(gameId, playerId)
    }

    /**
     * Request start position from the backend via shake gesture
     * This sends a request to the backend, which will respond via STOMP subscription
     */
    fun requestStartPosition(gameId: String, playerId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        Log.d("GameViewModel", "Requesting start position for game=$gameId, player=$playerId")
        myStomp.requestStartPosition(gameId, playerId)
    }

    /**
     * Confirm the assigned start position and proceed
     */
    fun confirmStartPosition(gameId: String, playerId: String) {
        val position = _startPosition.value
        if (position != null) {
            Log.d("GameViewModel", "Start position confirmed: $position")
            // In real scenario, send confirmation to backend
            _startPosition.value = null // Reset for next game
        }
    }

    /**
     * Reset error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    fun requestGameState(gameId: String) {
        myStomp.requestGameState(gameId)
    }

    /**
     * Reset all game state
     */
    fun resetGameState() {
        _startPosition.value = null
        _isLoading.value = false
        _errorMessage.value = null
    }

    override fun onResponse(res: String) {
        Log.d("GameViewModel", "Server response: $res")

        // Verbindungsstatus tracken
        when {
            res == "connected to server" -> {
                _isConnected.value = true
                return
            }
            res.startsWith("Connection lost") -> {
                _isConnected.value = false
                return
            }
        }

        // Handle start position responses
        if (res.startsWith("startPosition:")) {
            val jsonString = res.removePrefix("startPosition:")
            try {
                val jsonObject = JSONObject(jsonString)
                val response = StartPositionResponse(
                    type = jsonObject.optString("type", ""),
                    gameId = jsonObject.optString("gameId", ""),
                    playerId = jsonObject.optString("playerId", ""),
                    startPosition = if (jsonObject.has("startPosition")) jsonObject.optInt("startPosition") else null,
                    message = jsonObject.optString("message", null)
                )

                Log.d("GameViewModel", "Parsed start position response: type=${response.type}, position=${response.startPosition}")

                when (response.type) {
                    "START_POSITION_ASSIGNED" -> {
                        _startPosition.value = response.startPosition
                        _isLoading.value = false
                        _errorMessage.value = null
                        Log.d("GameViewModel", "Start position assigned: ${response.startPosition}")
                    }
                    "ERROR" -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message ?: "Unbekannter Fehler bei der Positionsvergabe"
                        Log.e("GameViewModel", "Error assigning start position: ${response.message}")
                    }
                    else -> {
                        Log.w("GameViewModel", "Unknown response type: ${response.type}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error parsing start position response", e)
                _isLoading.value = false
                _errorMessage.value = "Fehler beim Parsen der Serverantwort: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameStompService.unsubscribe()
    }
}

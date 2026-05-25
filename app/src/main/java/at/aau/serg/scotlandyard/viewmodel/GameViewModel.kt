package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.dtos.GameStateDto
import at.aau.serg.scotlandyard.dtos.StartPositionResponse
import at.aau.serg.scotlandyard.model.BoardConnection
import at.aau.serg.scotlandyard.model.StartPositionConstants
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.network.GameStompService
import at.aau.serg.scotlandyard.network.MyStomp
import at.aau.serg.scotlandyard.ui.activity.defaultTicketCounts
import at.aau.serg.scotlandyard.ui.theme.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel : ViewModel(), Callbacks {

    private val myStomp = MyStomp(this)
    val gameStompService = GameStompService(myStomp)

    private val _startPosition = MutableStateFlow<Int?>(null)
    val startPosition: StateFlow<Int?> = _startPosition.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /** True while the cheat/debug mode (manual wheel) is active. */
    private val _cheatModeActive = MutableStateFlow(false)
    val cheatModeActive: StateFlow<Boolean> = _cheatModeActive.asStateFlow()

    private val _gameState = MutableStateFlow<GameStateDto?>(null)
    val gameState: StateFlow<GameStateDto?> = _gameState.asStateFlow()

    private val _myPosition = MutableStateFlow<Int?>(null)
    val myPosition: StateFlow<Int?> = _myPosition.asStateFlow()

    private val _gameOver = MutableSharedFlow<String>()
    val gameOver: SharedFlow<String> = _gameOver.asSharedFlow()

    init {
        myStomp.connect()
        viewModelScope.launch {
            gameStompService.latestGameState.collect { state ->
                if (state != null) {
                    _gameState.value = state
                }
            }
        }
        viewModelScope.launch {
            gameStompService.gameOver.collect { result ->
                _gameOver.emit(result)
            }
        }
    }

    fun buildPlayerPositions(isMrX: Boolean, detectiveIdOrder: List<String>): Map<Color, Int> {
        val state = _gameState.value ?: return emptyMap()
        return buildMap {
            detectiveIdOrder.forEachIndexed { index, playerId ->
                val position = state.detectivePositions[playerId] ?: return@forEachIndexed
                val color = DETECTIVE_COLORS.getOrElse(index) { Color.Gray }
                put(color, position)
            }
            if (isMrX) {
                state.mrXPosition?.let { put(MRX_COLOR, it) }
            }
        }
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
        myStomp.subscribeToStartPosition(gameId, playerId)
    }

    fun unsubscribeFromStartPosition() {
        myStomp.unsubscribeFromStartPosition()
    }

    fun requestStartPosition(gameId: String, playerId: String) {
        if (_isLoading.value) {
            Log.d("GameViewModel", "requestStartPosition skipped – already loading")
            return
        }
        if (_startPosition.value != null) {
            Log.d("GameViewModel", "requestStartPosition skipped – position already assigned: ${_startPosition.value}")
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        myStomp.requestStartPosition(gameId, playerId)
    }

    /**
     * Picks a random valid start position (1–200) locally and stores it.
     * The spinner animation uses this value to know where to decelerate to.
     */
    fun generateLocalStartPosition(): Int {
        val pos = StartPositionConstants.VALID_POSITIONS.random()
        _startPosition.value = pos
        Log.d("GameViewModel", "Generated local start position: $pos")
        return pos
    }

    /**
     * Returns the currently stored start position without clearing it.
     * Useful to initialise the manual-selection position in cheat mode.
     */
    fun peekStartPosition(): Int? = _startPosition.value

    /**
     * Overrides the start position with a player-chosen value (cheat mode).
     * Validates that the position is within the allowed range.
     */
    fun setCheatStartPosition(position: Int) {
        require(StartPositionConstants.isValid(position)) {
            "Invalid cheat position: $position (must be ${StartPositionConstants.MIN_POSITION}–${StartPositionConstants.MAX_POSITION})"
        }
        _startPosition.value = position
        Log.d("GameViewModel", "Cheat start position set: $position")
    }

    /** Activate manual-wheel cheat mode. */
    fun activateCheatMode() {
        _cheatModeActive.value = true
        Log.d("GameViewModel", "Cheat mode activated")
    }

    /** Deactivate cheat mode (called after confirmation or on screen dispose). */
    fun deactivateCheatMode() {
        _cheatModeActive.value = false
    }

    fun confirmStartPosition(gameId: String, playerId: String) {
        val position = _startPosition.value
        if (position != null) {
            Log.d("GameViewModel", "Start position confirmed and sent to server: $position (gameId=$gameId, playerId=$playerId)")
            myStomp.sendConfirmedStartPosition(gameId, playerId, position)
            requestGameState(gameId)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun requestGameState(gameId: String) {
        myStomp.requestGameState(gameId)
    }

    /**
     * Clears the locally stored start position so we can detect when the server
     * responds with the confirmed (possibly different) position after a conflict check.
     */
    fun clearStartPosition() {
        _startPosition.value = null
    }

    fun resetGameState() {
        _startPosition.value = null
        _isLoading.value = false
        _errorMessage.value = null
    }

    fun getTicketCounts(playerId: String, isMrX: Boolean): Map<TicketType, Int> {
        val state = _gameState.value ?: return defaultTicketCounts(isMrX)

        return if (isMrX) {
            // Mr. X: reguläre Tickets sind unendlich, nur BLACK und DOUBLE tracken
            val special = state.mrXSpecialTickets
            mapOf(
                TicketType.WALKING   to Int.MAX_VALUE,
                TicketType.ESCOOTER  to Int.MAX_VALUE,
                TicketType.CARSHARING to Int.MAX_VALUE,
                TicketType.BLACK     to (special["BLACK"] ?: 0),
                TicketType.DOUBLE    to (special["DOUBLE"] ?: 0)
            )
        } else {
            val tickets = state.playerTickets[playerId] ?: return defaultTicketCounts(false)
            mapOf(
                TicketType.WALKING    to (tickets["WALKING"] ?: 0),
                TicketType.ESCOOTER   to (tickets["ESCOOTER"] ?: 0),
                TicketType.CARSHARING to (tickets["CARSHARING"] ?: 0)
            )
        }
    }

    override fun onResponse(res: String) {
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

                when (response.type) {
                    "START_POSITION_ASSIGNED" -> {
                        _startPosition.value = response.startPosition
                        _myPosition.value = response.startPosition
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
                    "ERROR" -> {
                        _isLoading.value = false
                        _errorMessage.value = response.message ?: "Unbekannter Fehler bei der Positionsvergabe"
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
        myStomp.shutdown()   // cancel coroutines + close WebSocket so old VMs don't linger
    }
}
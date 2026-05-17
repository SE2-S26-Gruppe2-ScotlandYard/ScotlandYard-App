package at.aau.serg.scotlandyard.network

import android.util.Log
import at.aau.serg.scotlandyard.dtos.GameStateDto
import at.aau.serg.scotlandyard.dtos.MovementResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "GameStompService"

class GameStompService(private val myStomp: MyStomp) {

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)


    private val _latestGameState = MutableStateFlow<GameStateDto?>(null)
    val latestGameState: StateFlow<GameStateDto?> = _latestGameState.asStateFlow()

    private val _gameStateEvents = MutableSharedFlow<GameStateDto>()
    val gameStateEvents: SharedFlow<GameStateDto> = _gameStateEvents.asSharedFlow()

    private val _movementResponse = MutableSharedFlow<MovementResponse>()
    val movementResponse: SharedFlow<MovementResponse> = _movementResponse.asSharedFlow()

    private val _gameOver = MutableSharedFlow<String>()
    val gameOver: SharedFlow<String> = _gameOver.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private var currentGameId: String? = null
    private var isSubscribed = false

    fun subscribe(gameId: String) {
        if (isSubscribed && currentGameId == gameId) {
            Log.d(TAG, "Already subscribed to game $gameId")
            return
        }
        currentGameId = gameId
        isSubscribed = true
        Log.d(TAG, "Subscribing to game: $gameId")

        myStomp.setGameStateCallback { msg -> onGameStateMessage(msg) }
        myStomp.setMovementCallback  { msg -> onMovementMessage(msg) }
        myStomp.setGameOverCallback  { msg -> onGameOverMessage(msg) }

        myStomp.connectToGame(gameId)
    }

    fun unsubscribe() {
        if (!isSubscribed) return
        Log.d(TAG, "Unsubscribing from game: $currentGameId")
        myStomp.setGameStateCallback(null)
        myStomp.setMovementCallback(null)
        myStomp.setGameOverCallback(null)
        isSubscribed = false
        currentGameId = null
        _latestGameState.value = null
    }

    fun sendMove(gameId: String, playerId: String, ticket: String, targetPosition: Int) {
        myStomp.sendMove(gameId, playerId, ticket, targetPosition)
    }

    fun sendDoubleMove(gameId: String, playerId: String) {
        myStomp.sendMove(gameId, playerId, "DOUBLE", 0)
    }

    private fun onGameStateMessage(msg: String) {
        scope.launch {
            try {
                Log.d("PLAYER_DEBUG", "Raw GameState message: $msg")
                val state = gson.fromJson(msg, GameStateDto::class.java)
                if (state != null) {
                    _latestGameState.value = state
                    _gameStateEvents.emit(state)
                    Log.d(TAG, "GameState: round=${state.currentRound}, phase=${state.currentPhase}")
                } else {
                    _error.emit("Null game state received")
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parse error for GameStateDto", e)
                _error.emit("Invalid game-state format")
            }
        }
    }

    private fun onMovementMessage(msg: String) {
        scope.launch {
            try {
                val response = gson.fromJson(msg, MovementResponse::class.java)
                if (response != null) {
                    _movementResponse.emit(response)
                    Log.d(TAG, "MovementResponse: success=${response.success}")
                } else {
                    _error.emit("Null movement response received")
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parse error for MovementResponse", e)
                _error.emit("Invalid movement-response format")
            }
        }
    }

    private fun onGameOverMessage(msg: String) {
        scope.launch {
            val result = msg.trim().removeSurrounding("\"")
            Log.d(TAG, "GameOver: $result")
            _gameOver.emit(result)
        }
    }
}
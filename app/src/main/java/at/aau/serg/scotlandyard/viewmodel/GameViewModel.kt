package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.dtos.StartPositionResponse
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class GameViewModel : ViewModel(), Callbacks {

    private val myStomp = MyStomp(this)

    // Game State
    private val _startPosition = MutableStateFlow<Int?>(null)
    val startPosition: StateFlow<Int?> = _startPosition.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        myStomp.connect()
    }

    /**
     * Subscribe to start position responses from the backend
     * Must be called before requestStartPosition to ensure subscription is active
     */
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
        // Clean up resources if needed
    }
}


package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
     * Request start position from the backend via shake gesture
     */
    fun requestStartPosition(gameId: String, playerId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        // Simulate API call - in real scenario this would be a network call
        // For now, we'll generate a random position between 1-199 (typical Scotland Yard board)
        try {
            val randomPosition = (1..199).random()
            _startPosition.value = randomPosition
            _isLoading.value = false
            Log.d("GameViewModel", "Start position assigned: $randomPosition")
        } catch (e: Exception) {
            Log.e("GameViewModel", "Error requesting start position", e)
            _errorMessage.value = "Fehler beim Abrufen der Startposition: ${e.message}"
            _isLoading.value = false
        }
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
        // Handle server responses here
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources if needed
    }
}


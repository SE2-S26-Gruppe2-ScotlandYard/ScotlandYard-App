package at.aau.serg.scotlandyard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.network.LobbyStompService
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.hildan.krossbow.stomp.sendText

class LobbyViewModel(
    val userId: String,
    val userName: String,
    private val myStomp: MyStomp
) : ViewModel() {

    private var lobbyService: LobbyStompService? = null

    private val _currentLobby = MutableStateFlow<LobbyData?>(null)
    val currentLobby: StateFlow<LobbyData?> = _currentLobby.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _navigateToRoleSelection = MutableSharedFlow<Unit>()
    val navigateToRoleSelection: SharedFlow<Unit> = _navigateToRoleSelection.asSharedFlow()

    private val _navigateToLobby = MutableSharedFlow<Unit>()
    val navigateToLobby: SharedFlow<Unit> = _navigateToLobby.asSharedFlow()

    // ── Navigation Event: alle Spieler zum Spiel schicken ────────────
    private val _navigateToGame = MutableSharedFlow<String>() // emits gameId
    val navigateToGame: SharedFlow<String> = _navigateToGame.asSharedFlow()

    init {
        // Verbindungsstatus direkt aus MyStomp beziehen
        viewModelScope.launch {
            myStomp.isConnected.collect { connected ->
                _isConnected.value = connected
            }
        }

        val session = myStomp.getSession()
        if (session != null) {
            setupLobbyService(session)
        } else {
            viewModelScope.launch {
                myStomp.isConnected.collect { connected ->
                    if (connected && lobbyService == null) {
                        val s = myStomp.getSession()
                        if (s != null) setupLobbyService(s)
                    }
                }
            }
        }
    }

    private fun setupLobbyService(session: org.hildan.krossbow.stomp.StompSession) {
        lobbyService = LobbyStompService(session, userId, myStomp)
        lobbyService!!.subscribe()

        viewModelScope.launch {
            lobbyService!!.lobbyResponse.collect { response ->
                response ?: return@collect
                _isLoading.value = false

                if (!response.success) {
                    _statusMessage.value = "⚠️ ${response.message}"
                    return@collect
                }

                val incomingLobby = response.lobby
                val currentLobby = _currentLobby.value
                val isBackToLobby = response.message.contains("returned to lobby", ignoreCase = true)
                        || response.message == "BACK_TO_LOBBY"

                if (incomingLobby != null) {
                    val isPlayerInLobby = incomingLobby.users?.any { it.id == userId } == true
                            || incomingLobby.hostId == userId
                    if (isPlayerInLobby) {
                        _currentLobby.value = incomingLobby
                        lobbyService?.subscribeToSpecificLobby(incomingLobby.id)
                    } else if (currentLobby?.id == incomingLobby.id && !isBackToLobby) {
                        // Only remove lobby if this is NOT a back-to-lobby transition.
                        // During BACK_TO_LOBBY the server may send a transient state where
                        // the player isn't yet in the users list – keep current lobby.
                        _currentLobby.value = null
                        _statusMessage.value = "Du wurdest aus der Lobby entfernt"
                        lobbyService?.unsubscribeSpecificLobby()
                    }
                } else if (response.lobbyId != null && currentLobby?.id == response.lobbyId) {
                    _currentLobby.value = null
                    lobbyService?.unsubscribeSpecificLobby()
                }

                // Navigation (kombiniert aus beiden branches + GAME_STARTED)
                when {
                    response.message.contains("started role selection", ignoreCase = true)
                            || response.message == "ROLE_SELECTION_STARTED" ->
                        _navigateToRoleSelection.emit(Unit)
                    response.message.contains("returned to lobby", ignoreCase = true)
                            || response.message == "BACK_TO_LOBBY" ->
                        _navigateToLobby.emit(Unit)
                    response.message == "GAME_STARTED" -> {
                        val gameId = response.gameId
                            ?: response.lobbyId
                            ?: incomingLobby?.id
                            ?: ""
                        _navigateToGame.emit(gameId)
                    }
                }

                if (response.message !in listOf("ROLE_SELECTION_STARTED", "BACK_TO_LOBBY", "GAME_STARTED", "OK", "SUCCESS")
                    && response.message.isNotBlank()
                ) {
                    _statusMessage.value = response.message
                }

            }
        }
    }

    fun createLobby() {
        _isLoading.value = true
        lobbyService?.createLobby("${userName}'s Lobby", userId, userName)
            ?: run { _statusMessage.value = "⚠️ Nicht verbunden" }
    }

    fun joinLobby(lobbyCode: String) {
        if (lobbyCode.length != 5) {
            _statusMessage.value = "⚠️ Bitte einen 5-stelligen Code eingeben"
            return
        }
        _isLoading.value = true
        lobbyService?.joinLobby(lobbyCode.trim(), userId, userName)
            ?: run { _statusMessage.value = "⚠️ Nicht verbunden" }
    }

    fun leaveLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.leaveLobby(lobbyId, userId)
        // _currentLobby wird erst durch die Server-Antwort aktualisiert
    }

    fun deleteLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.deleteLobby(lobbyId, userId)
        // _currentLobby wird erst durch die Server-Antwort aktualisiert
    }

    fun kickPlayer(targetUserId: String) {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.kickPlayer(lobbyId, userId, targetUserId)
    }

    fun setRole(targetUserId: String, role: String) {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.setRole(lobbyId, userId, targetUserId, role)
    }

    fun startRoleSelection() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.startRoleSelection(lobbyId, userId)
    }

    // ── Host drückt "Start Game" in der Rollenwahl ────────────────────────
    fun startGame() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.startGame(lobbyId, userId)
    }

    fun goBackToLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("lobbyId", lobbyId)
                    put("requesterId", userId)
                }
                myStomp.getSession()?.sendText("/app/lobby/backToLobby", payload.toString())
            } catch (e: Exception) {
                _statusMessage.value = "⚠️ Fehler bei der Rückkehr zur Lobby."
            }
        }
    }

    fun isLocalUserHost(): Boolean = _currentLobby.value?.hostId == userId

    override fun onCleared() {
        super.onCleared()
        lobbyService?.unsubscribe()
    }
}

class LobbyViewModelFactory(
    private val userId: String,
    private val userName: String,
    private val myStomp: MyStomp
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
            return LobbyViewModel(userId, userName, myStomp) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
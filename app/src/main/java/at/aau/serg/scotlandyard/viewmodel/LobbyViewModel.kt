package at.aau.serg.scotlandyard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.network.LobbyStompService
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // ── Navigation Event: alle Spieler zur Rollenwahl schicken ────────────
    private val _navigateToRoleSelection = MutableSharedFlow<Unit>()
    val navigateToRoleSelection: SharedFlow<Unit> = _navigateToRoleSelection.asSharedFlow()

    // ── Navigation Event: alle Spieler zurück zur Lobby ────────────
    private val _navigateToLobby = MutableSharedFlow<Unit>()
    val navigateToLobby: SharedFlow<Unit> = _navigateToLobby.asSharedFlow()

    init {
        val session = myStomp.getSession()
        if (session != null) {
            setupLobbyService(session)
        } else {
            viewModelScope.launch {
                myStomp.isConnected.collect { connected ->
                    if (connected && lobbyService == null) {
                        val s = myStomp.getSession()
                        if (s != null) setupLobbyService(s)
                    } else if (!connected) {
                        _isConnected.value = false
                    }
                }
            }
        }
    }

    private fun setupLobbyService(session: org.hildan.krossbow.stomp.StompSession) {
        lobbyService = LobbyStompService(session)
        lobbyService!!.subscribe(myStomp)
        _isConnected.value = true

        viewModelScope.launch {
            lobbyService!!.lobbyResponse.collect { response ->
                response ?: return@collect
                _isLoading.value = false

                if (!response.success) {
                    _statusMessage.value = "⚠️ ${response.message}"
                    return@collect
                }

                // --- Schritt 2: Lobby-Status aktualisieren ---
                val incomingLobby = response.lobby
                val currentLobby = _currentLobby.value

                if (incomingLobby != null) {
                    val isPlayerInLobby = incomingLobby.users.any { it.id == userId }

                    if (isPlayerInLobby) {

                        _currentLobby.value = incomingLobby
                    } else if (currentLobby?.id == incomingLobby.id) {
                        // Wir waren in dieser Lobby, sind es aber nicht mehr (z.B. gekickt).
                        _currentLobby.value = null
                        _statusMessage.value = "Du wurdest aus der Lobby entfernt"
                    }
                    // Eine fremde Lobby wird ignoriert.

                } else if (response.lobbyId != null) {
                    // Lobby wurde vermutlich gelöscht.
                    if (currentLobby?.id == response.lobbyId) {
                        _currentLobby.value = null
                    }
                }

                // Navigations-Events verarbeiten
                when (response.message) {
                    "ROLE_SELECTION_STARTED" -> _navigateToRoleSelection.emit(Unit)
                    "BACK_TO_LOBBY" -> {_navigateToLobby.emit(Unit)
                    }
                }

                // Status-Nachricht anzeigen
                // filtern Nachrichten heraus, die nur für die Navigation gedacht sind.
                if (response.message !in listOf("ROLE_SELECTION_STARTED", "BACK_TO_LOBBY", "OK", "SUCCESS")) {
                    if(response.message.isNotBlank()) {
                        _statusMessage.value = response.message
                        }
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
        _currentLobby.value = null
    }

    fun deleteLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.deleteLobby(lobbyId, userId)
        _currentLobby.value = null
    }

    fun kickPlayer(targetUserId: String) {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.kickPlayer(lobbyId, userId, targetUserId)
    }

    fun setRole(targetUserId: String, role: String) {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.setRole(lobbyId, userId, targetUserId, role)
    }

    // ── Host drückt "Weiter zur Rollenwahl" ───────────────────────────────
    fun startRoleSelection() {
        val lobbyId = _currentLobby.value?.id ?: return
        lobbyService?.startRoleSelection(lobbyId, userId)
    }

    fun goBackToLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        val payload = JSONObject().apply {
            put("lobbyId", lobbyId)
            put("requesterId", userId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                myStomp.getSession()?.sendText("/app/lobby/backToLobby", payload.toString())
            } catch (e: Exception) {
                _statusMessage.value = "⚠️ Fehler bei der Rückkehr zur Lobby."
            }
        }
    }

    fun isLocalUserHost(): Boolean = _currentLobby.value?.hostId == userId

    override fun onCleared() {
        super.onCleared()
        lobbyService?.unsubscribe(myStomp)
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
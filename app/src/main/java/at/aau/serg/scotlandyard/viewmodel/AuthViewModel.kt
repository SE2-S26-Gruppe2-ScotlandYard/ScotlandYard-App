package at.aau.serg.scotlandyard.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.data.getGameId
import at.aau.serg.scotlandyard.data.getLobbyId
import at.aau.serg.scotlandyard.data.getUserNickname
import at.aau.serg.scotlandyard.data.saveUserSession
import at.aau.serg.scotlandyard.dtos.User
import at.aau.serg.scotlandyard.dtos.UserConnectResponse
import at.aau.serg.scotlandyard.network.MyStomp
import at.aau.serg.scotlandyard.network.RejoinStompService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application), Callbacks {

    private val myStomp = MyStomp(this)
    fun getMyStomp(): MyStomp = myStomp
    val isConnected: StateFlow<Boolean> = myStomp.isConnected

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Signalisiert der App dass ein Rejoin stattgefunden hat
    private val _rejoinEvent = MutableStateFlow<RejoinEvent?>(null)
    val rejoinEvent: StateFlow<RejoinEvent?> = _rejoinEvent.asStateFlow()

    private val gson = Gson()
    private val context get() = getApplication<Application>()

    init {
        myStomp.connect()

        viewModelScope.launch {
            myStomp.isConnected.collect { connected ->
                if (!connected) {
                    _currentUser.value = null
                } else {
                    // Verbindung wiederhergestellt – versuche Rejoin
                    tryRejoinIfNeeded()
                }
            }
        }
    }

    private fun tryRejoinIfNeeded() {
        val savedNickname = context.getUserNickname() ?: return
        val savedLobbyId = context.getLobbyId()
        val savedGameId = context.getGameId()

        if (savedLobbyId != null || savedGameId != null) {
            // User neu registrieren damit wir eine userId bekommen
            myStomp.sendUserConnect(savedNickname)
            Log.d("AuthViewModel", "Attempting rejoin after reconnect")
        }
    }

    fun reconnect() {
        myStomp.connect()
    }

    fun connectUser(nickname: String) {
        _errorMessage.value = null
        myStomp.sendUserConnect(nickname)
    }

    override fun onResponse(res: String) {
        Log.d("AuthViewModel", "Server response: $res")

        if (!res.startsWith("{")) return

        try {
            val response = gson.fromJson(res, UserConnectResponse::class.java)
            if (response != null) {
                if (response.success) {
                    _currentUser.value = response.user
                    response.user?.let { user ->
                        myStomp.setCurrentUserId(user.id)
                        myStomp.enablePrivateTopic(user.id)

                        // Session speichern für Reconnect
                        context.saveUserSession(user.id, user.nickName ?: "")

                        // Rejoin falls nötig
                        val session = myStomp.getSession()
                        if (session != null) {
                            val rejoinService = RejoinStompService(session)
                            val savedGameId = context.getGameId()
                            val savedLobbyId = context.getLobbyId()

                            when {
                                savedGameId != null -> {
                                    rejoinService.rejoinGame(user.id, savedGameId)
                                    _rejoinEvent.value = RejoinEvent.GAME
                                }
                                savedLobbyId != null -> {
                                    rejoinService.rejoinLobby(savedLobbyId, user.id, user.nickName ?: "")
                                    _rejoinEvent.value = RejoinEvent.LOBBY
                                }
                            }
                        }
                    }
                } else {
                    _errorMessage.value = response.message
                }
            }
        } catch (e: Exception) {
            Log.d("AuthViewModel", "Ignoriere Response: ${e.message}")
        }
    }

    fun clearRejoinEvent() {
        _rejoinEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        myStomp.shutdown()
    }
}

enum class RejoinEvent {
    LOBBY, GAME
}
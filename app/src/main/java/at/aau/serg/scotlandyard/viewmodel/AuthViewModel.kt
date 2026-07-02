package at.aau.serg.scotlandyard.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.data.clearSession
import at.aau.serg.scotlandyard.data.getGameId
import at.aau.serg.scotlandyard.data.getLobbyId
import at.aau.serg.scotlandyard.data.getUserId
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

    private val _rejoinEvent = MutableStateFlow<RejoinEvent?>(null)
    val rejoinEvent: StateFlow<RejoinEvent?> = _rejoinEvent.asStateFlow()

    private var isAttemptingRejoin = false

    private val gson = Gson()
    private val context get() = getApplication<Application>()

    init {
        myStomp.connect()

        viewModelScope.launch {
            myStomp.isConnected.collect { connected ->
                if (!connected) {
                    _currentUser.value = null
                }
            }
        }
    }

    fun tryAutoConnect(): Boolean {
        val savedNickname = context.getUserNickname()
        val savedUserId = context.getUserId()
        if (!savedNickname.isNullOrBlank() && savedUserId != null) {
            connectUser(savedNickname)
            return true
        }
        return false
    }

    fun reconnect() {
        myStomp.connect()
    }

    fun connectUser(nickname: String) {
        _errorMessage.value = null
        val savedLobbyId = context.getLobbyId()
        val savedGameId = context.getGameId()
        isAttemptingRejoin = savedLobbyId != null || savedGameId != null
        myStomp.sendUserConnect(nickname, context.getUserId())
    }

    fun renameNickname(newNickname: String) {
        val userId = context.getUserId() ?: currentUser.value?.id ?: return
        _errorMessage.value = null
        myStomp.sendRenameUser(userId, newNickname)
    }

    override fun onResponse(res: String) {
        Log.d("AuthViewModel", "Server response: $res")
        if (!res.startsWith("{")) return
        try {
            val response = gson.fromJson(res, UserConnectResponse::class.java) ?: return

            if (!response.success) {
                _errorMessage.value = response.message
                isAttemptingRejoin = false
                return
            }

            val user = response.user ?: return

            _currentUser.value = user
            myStomp.setCurrentUserId(user.id)
            myStomp.enablePrivateTopic(user.id)
            context.saveUserSession(user.id, user.nickName ?: "")

            if (isAttemptingRejoin) {
                val session = myStomp.getSession()
                Log.d("AuthViewModel", "Rejoin: session=$session gameId=${context.getGameId()} lobbyId=${context.getLobbyId()}")
                if (session != null) {
                    val rejoinService = RejoinStompService(session)
                    val savedGameId = context.getGameId()
                    val savedLobbyId = context.getLobbyId()
                    when {
                        savedGameId != null -> {
                            rejoinService.rejoinGame(user.id, savedGameId)
                            _rejoinEvent.value = RejoinEvent.GAME
                            Log.d("AuthViewModel", "RejoinEvent.GAME set")
                        }
                        savedLobbyId != null -> {
                            rejoinService.rejoinLobby(savedLobbyId, user.id, user.nickName ?: "")
                            _rejoinEvent.value = RejoinEvent.LOBBY
                            Log.d("AuthViewModel", "RejoinEvent.LOBBY set")
                        }
                    }
                } else {
                    Log.e("AuthViewModel", "Session is null, cannot rejoin!")
                    context.clearSession()
                }
                isAttemptingRejoin = false
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
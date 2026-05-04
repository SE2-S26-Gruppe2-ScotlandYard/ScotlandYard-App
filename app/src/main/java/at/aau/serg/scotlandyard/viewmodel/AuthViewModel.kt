package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson

import at.aau.serg.scotlandyard.dtos.UserConnectResponse
import at.aau.serg.scotlandyard.dtos.User

class AuthViewModel : ViewModel(), Callbacks {

    private val myStomp = MyStomp(this)
    val isConnected: StateFlow<Boolean> = myStomp.isConnected

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val gson = Gson()

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

            if (response != null && response.message != null) {
                if (response.success) {
                    _currentUser.value = response.user
                } else {
                    _errorMessage.value = response.message
                }
            }
        } catch (e: Exception) {
            Log.d("AuthViewModel", "Ignoriere Response (kein JSON oder falsches Format): ${e.message}")
        }
    }
}
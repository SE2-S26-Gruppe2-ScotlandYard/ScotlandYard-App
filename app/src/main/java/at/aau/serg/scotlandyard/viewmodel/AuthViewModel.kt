package at.aau.serg.scotlandyard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.MyStomp
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel(), Callbacks {
    
    private val myStomp = MyStomp(this)
    val isConnected: StateFlow<Boolean> = myStomp.isConnected
    
    init {
        // Automatically try to connect to the server
        myStomp.connect()
    }
    
    fun reconnect() {
        myStomp.connect()
    }
    
    override fun onResponse(res: String) {
        Log.d("AuthViewModel", "Server response: $res")
    }
}

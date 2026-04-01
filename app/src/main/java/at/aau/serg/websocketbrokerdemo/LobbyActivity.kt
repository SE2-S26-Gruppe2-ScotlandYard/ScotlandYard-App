package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

class LobbyActivity : ComponentActivity(), Callbacks {

    private lateinit var myStomp: MyStomp
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lobby)

        // Initialize Stomp client
        myStomp = MyStomp(this)

        status = findViewById(R.id.status)

        // Set up buttons
        findViewById<Button>(R.id.btn_create_lobby).setOnClickListener {
            // TODO: Implement create lobby logic
            myStomp.sendCreateLobby()
        }

        findViewById<Button>(R.id.btn_join_lobby).setOnClickListener {
            // TODO: Implement join lobby logic
            myStomp.sendJoinLobby()
        }

        findViewById<Button>(R.id.btn_rejoin_lobby).setOnClickListener {
            // TODO: Implement rejoin lobby logic
            myStomp.sendRejoinLobby()
        }

        // Connect to server when activity starts
        myStomp.connect()
    }

    override fun onResponse(res: String) {
        status.text = res
    }

}
package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Initialize Stomp cleint
        myStomp = MyStomp(this)

        findViewById<Button>(R.id.playbtn).setOnClickListener {
            // Start Connection
            myStomp.connect()
        }

        findViewById<Button>(R.id.rulesbtn).setOnClickListener {
            showRules()
        }
    }

    private fun showRules() {
        // Simple toast for now
        Toast.makeText(this,"Spielregeln.",Toast.LENGTH_LONG).show()
        // TODO open a browser or other ruleset/activity
    }

    override fun onResponse(res: String) {
        Handler(Looper.getMainLooper()).post {  // Not required due to existence in Stomp callback, but safer
            when (res) {
                "Connected" -> {
                    // Start LobbyActivity after successful connection
                    val intent = Intent(this, LobbyActivity::class.java)
                    startActivity(intent)
                    finish() // Close MainActivity
                }

                "Connection error" -> {
                    Toast.makeText(this, "Failed to connect to server.", Toast.LENGTH_LONG).show()
                }

                else -> {
                    response.text = res
                }
            }
        }
    }

}


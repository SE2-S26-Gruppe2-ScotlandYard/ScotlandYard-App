package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.MyStomp
import com.example.scotlandyard.R
import org.json.JSONObject

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        myStomp = MyStomp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { myStomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener { myStomp.sendHello() }
        findViewById<Button>(R.id.jsonbtn).setOnClickListener { myStomp.sendJson() }
        response = findViewById(R.id.response_view)
    }

    override fun onResponse(res: String) {
        response.text = res
    }

}
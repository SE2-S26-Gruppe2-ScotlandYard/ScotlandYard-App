package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.aau.serg.websocketbrokerdemo.ui.screens.StartScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // setContent { } startet die Compose-UI — kein XML mehr
        setContent {
            MyApplicationTheme {
                // navController merkt sich welcher Screen gerade angezeigt wird
                val navController = rememberNavController()
                // NavHost definiert alle Screens und ihre Namen ("Routes")
                NavHost(navController = navController, startDestination = "start") {
                    composable("start") {
                        StartScreen(
                            onStartGame = { navController.navigate("lobby") },
                            onRules = { navController.navigate("rules") }
                        )
                    }
                    // Weitere Screens (lobby, rules, ...) kommen hier rein
                }
            }
        }
    }
}

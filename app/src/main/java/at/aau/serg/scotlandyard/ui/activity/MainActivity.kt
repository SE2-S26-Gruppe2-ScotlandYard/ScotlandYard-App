package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.aau.serg.scotlandyard.ui.activity.StartScreen
import at.aau.serg.scotlandyard.ui.activity.RulesScreen
import at.aau.serg.scotlandyard.ui.activity.LobbyScreen
import at.aau.serg.scotlandyard.ui.activity.SettingsScreen
import at.aau.serg.scotlandyard.ui.activity.LoginScreen
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // setContent { } startet die Compose-UI — kein XML mehr
        setContent {
            ScotlandYardTheme {
                // Initialize the ViewModel which automatically attempts to connect
                val authViewModel: AuthViewModel = viewModel()
                // Observe the connection state from the ViewModel
                val isConnected by authViewModel.isConnected.collectAsState()

                // navController remembers the screen
                val navController = rememberNavController()
                // NavHost defines all Screens and Names
                NavHost(navController = navController, startDestination = "start") {
                    composable("start") {
                        StartScreen(
                            onStartGame = { navController.navigate("login") },
                            onRules = { navController.navigate("rules") },
                            onSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("lobby") },
                            onBackClick = { navController.popBackStack() },
                            onRefreshClick = { authViewModel.reconnect() },
                            isConnectedToServer = isConnected
                        )
                    }
                    composable("rules") {
                        RulesScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable("lobby") {
                        LobbyScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable("settings") {
                        SettingsScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable("mrxwin") {
                        MrXWinScreen(
                            onBackClick = { navController.navigate("start") },
                            onQuit = { (navController.context as? android.app.Activity)?.finish() }
                        )
                    }
                }
            }
        }
    }
}

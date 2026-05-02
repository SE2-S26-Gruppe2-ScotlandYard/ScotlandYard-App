package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScotlandYardTheme {
                val authViewModel: AuthViewModel = viewModel()
                val isConnected by authViewModel.isConnected.collectAsState()

                val currentUser by authViewModel.currentUser.collectAsState()
                val errorMessage by authViewModel.errorMessage.collectAsState()

                val navController = rememberNavController()
                val context = LocalContext.current

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }

                NavHost(navController = navController, startDestination = "start") {
                    composable("start") {
                        StartScreen(
                            onStartGame = { navController.navigate("login") },
                            onRules = { navController.navigate("rules") },
                            onSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("login") {
                        LaunchedEffect(currentUser) {
                            if (currentUser != null) {
                                navController.navigate("lobby") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        LoginScreen(
                            onConnectClick = { nickname ->
                                authViewModel.connectUser(nickname)
                            },
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
                    composable("detectiveswin") {
                        DetectivesWinScreen(
                            onBackClick = { navController.navigate("start") },
                            onQuit = { (navController.context as? android.app.Activity)?.finish() }
                        )
                    }
                }
            }
        }
    }
}
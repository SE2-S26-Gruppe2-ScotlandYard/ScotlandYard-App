package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import at.aau.serg.scotlandyard.data.getDisplayModePreference
import androidx.navigation.navArgument
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import androidx.compose.runtime.collectAsState

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

                var sharedLobbyViewModel by remember { mutableStateOf<LobbyViewModel?>(null) }

                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                LaunchedEffect(isConnected) {
                    if (!isConnected && currentRoute != null
                        && currentRoute != "start" && currentRoute != "login") {
                        Toast.makeText(context, "Verbindung verloren!", Toast.LENGTH_LONG).show()
                        navController.navigate("start") { popUpTo(0) }
                    }
                }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                }

                NavHost(navController = navController, startDestination = "start") {
                    composable("start") {
                        StartScreen(
                            onStartGame = { navController.navigate("login") },
                            onRules     = { navController.navigate("rules") },
                            onSettings  = { navController.navigate("settings") }
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
                            onConnectClick      = { nickname -> authViewModel.connectUser(nickname) },
                            onBackClick         = { navController.popBackStack() },
                            onRefreshClick      = { authViewModel.reconnect() },
                            isConnectedToServer = isConnected
                        )
                    }

                    composable("rules") {
                        RulesScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("lobby") {
                        val user = currentUser
                        if (user != null) {
                            LobbyScreen(
                                authViewModel = authViewModel,
                                onBackClick   = { navController.popBackStack() },
                                // Host: Signal senden + sofort navigieren
                                onProceedToRoles = { lobbyViewModel ->
                                    sharedLobbyViewModel = lobbyViewModel
                                    lobbyViewModel.startRoleSelection()
                                    navController.navigate("roleSelection")
                                },
                                // Gäste: Server-Signal empfangen + navigieren
                                onNavigateToRoleSelection = { lobbyViewModel ->
                                    sharedLobbyViewModel = lobbyViewModel
                                    if (currentRoute != "roleSelection") {
                                        navController.navigate("roleSelection")
                                    }
                                },
                                userId   = user.id,
                                userName = user.nickName
                            )
                        }
                    }

                    composable("roleSelection") {
                        val lobbyVm = sharedLobbyViewModel
                        if (lobbyVm != null) {
                            val lobby by lobbyVm.currentLobby.collectAsState()

                            // Wenn Backend "GAME_STARTED" sendet → alle Spieler navigieren
                            LaunchedEffect(lobbyVm) {
                                lobbyVm.navigateToGame.collect { gameId ->
                                    val playerId = lobbyVm.userId
                                    navController.navigate("assignstartposition/$gameId/$playerId") {
                                        popUpTo("roleSelection") { inclusive = true }
                                    }
                                }
                            }

                            if (lobby != null) {
                                RoleSelectionScreen(
                                    viewModel   = lobbyVm,
                                    lobby       = lobby!!,
                                    onBackClick = { navController.popBackStack() },
                                    onGameStart = {
                                        // Host sendet startGame ans Backend
                                        // Backend antwortet mit GAME_STARTED → navigateToGame Event
                                        lobbyVm.startGame()
                                    }
                                )
                            }
                        }
                    }

                    composable("settings") {
                        SettingsScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable(
                        route = "assignstartposition/{gameId}/{playerId}",
                        arguments = listOf(
                            navArgument("gameId") { type = NavType.StringType },
                            navArgument("playerId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                        val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                        AssignStartPositionScreen(
                            gameId = gameId,
                            playerId = playerId,
                            onBackClick = { navController.popBackStack() },
                            onPositionConfirmed = { navController.navigate("lobby") }
                        )
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
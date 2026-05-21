package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
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
import at.aau.serg.scotlandyard.model.BoardConnection
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    /** Forward all key events to CheatKeyEventRegistry so composables can react to volume buttons. */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        CheatKeyEventRegistry.notify(event)
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BoardConnection.init(this)
        enableEdgeToEdge()

        window.insetsController?.let { controller ->        // to show system bars again remove/comment this block
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

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
                                    onGameStart = { lobbyVm.startGame() }
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
                        val lobbyVm = sharedLobbyViewModel
                        AssignStartPositionScreen(
                            gameId = gameId,
                            playerId = playerId,
                            onBackClick = { navController.popBackStack() },
                            onPositionConfirmed = {
                                // Determine role: MRX or DETECTIVE
                                val selectedRoles = lobbyVm?.currentLobby?.value?.selectedRoles
                                val isMrX = selectedRoles?.get(playerId) == "MRX"
                                navController.navigate("gameboard/$gameId/$playerId/$isMrX") {
                                    popUpTo("assignstartposition/$gameId/$playerId") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "gameboard/{gameId}/{playerId}/{isMrX}",
                        arguments = listOf(
                            navArgument("gameId") { type = NavType.StringType },
                            navArgument("playerId") { type = NavType.StringType },
                            navArgument("isMrX") { type = NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                        val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                        val isMrX = backStackEntry.arguments?.getBoolean("isMrX") ?: false
                        val context = LocalContext.current
                        val displayMode = remember { context.getDisplayModePreference() }

                        val gameViewModel: GameViewModel = viewModel()

                        LaunchedEffect(gameId) {
                            gameViewModel.gameStompService.subscribe(gameId)

                            // Wait until the WebSocket connection is actually established.
                            // On a fresh ViewModel the connection attempt is still in progress, so
                            // the old fixed 300ms delay was too short and the STOMP SUBSCRIBE frames
                            // hadn't reached the server yet when requestGameState was sent.
                            gameViewModel.isConnected.first { it }

                            // Additional buffer so the STOMP SUBSCRIBE handshake completes
                            // on the server side before we fire the state request.
                            delay(600)
                            gameViewModel.requestGameState(gameId)

                            // Fallback: if the response was still lost (e.g. slow network),
                            // retry once after 3 s.
                            delay(3_000)
                            if (gameViewModel.gameState.value == null) {
                                Log.d("MainActivity", "GameState still null after 3 s – retrying requestGameState")
                                gameViewModel.requestGameState(gameId)
                            }
                        }

                        // subscribe to GameState
                        val gameState by gameViewModel.gameState.collectAsState()

                        LaunchedEffect(gameState) {
                            gameViewModel.updateMyPosition(playerId, isMrX)
                        }

                        val isMyTurn = remember(gameState) {
                            gameState?.let { if (isMrX) it.isMrXPhase else it.isDetectivesPhase } ?: false
                        }

                        val detectiveIdOrder = gameState?.detectivePositions?.keys?.sorted() ?: emptyList()
                        val playerPositions = gameViewModel.buildPlayerPositions(isMrX, detectiveIdOrder)

                        var selectedTicket by remember { mutableStateOf<TicketType?>(null) }

                        LaunchedEffect(!isMyTurn && selectedTicket != null) {
                            selectedTicket = null
                        }

                        val myPosition by gameViewModel.myPosition.collectAsState()

                        LaunchedEffect(Unit) {
                            gameViewModel.gameOver.collect { result ->
                                when (result) {
                                    "DETECTIVES_WIN" -> navController.navigate("detectiveswin") {
                                        popUpTo("gameboard/$gameId/$playerId/$isMrX") { inclusive = true }
                                    }
                                    "MRX_WINS" -> navController.navigate("mrxwin") {
                                        popUpTo("gameboard/$gameId/$playerId/$isMrX") { inclusive = true }
                                    }
                                }
                            }
                        }

                        val highlightedNodes = remember(selectedTicket, myPosition) {
                            val ticket = selectedTicket
                            val pos = myPosition
                            if (ticket != null && pos != null) {
                                gameViewModel.reachableStations(ticket)
                            } else emptySet()
                        }

                        val ticketCounts = remember(gameState) {
                            gameViewModel.getTicketCounts(playerId, isMrX)
                        }

                        val isDoubleActive = gameState?.doubleMoveActive ?: false

                        GameBoardScreen(
                            isMrX = isMrX,
                            mrXRevealedPositions = if (!isMrX) gameState?.mrXRevealedPositions ?: emptyMap() else emptyMap(),
                            currentRound = gameState?.currentRound ?: 1,
                            displayMode = displayMode,
                            playerPositions = playerPositions,
                            highlightedNodes = highlightedNodes,
                            isMyTurn = isMyTurn,
                            selectedTicket = selectedTicket,
                            mrXMoveHistory = if (!isMrX) gameState?.mrXMoveHistory ?: emptyList() else emptyList(),
                            ticketCounts = ticketCounts.toMutableMap().apply {
                                if (isDoubleActive) put(TicketType.DOUBLE, 0)   // GameState from server contains real count of DOUBLE tickets, this only disables the button
                            },
                            onTicketSelect = { ticket ->
                                if (isMyTurn) {
                                    selectedTicket = if (selectedTicket == ticket) null else ticket
                                }
                            },
                            onNodeClick = { stationId ->
                                val ticket = selectedTicket
                                if (ticket != null) {
                                    gameViewModel.sendMove(gameId, playerId, ticket, stationId)
                                    selectedTicket = null
                                }
                            },
                            onNavigateToSettings = { navController.navigate("settings") }
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
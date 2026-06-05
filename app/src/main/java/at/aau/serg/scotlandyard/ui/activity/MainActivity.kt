package at.aau.serg.scotlandyard.ui.activity

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.aau.serg.scotlandyard.data.getDisplayModePreference
import at.aau.serg.scotlandyard.dtos.User
import at.aau.serg.scotlandyard.model.BoardConnection
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import androidx.compose.runtime.collectAsState
import android.util.Log
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BoardConnection.init(this)
        enableEdgeToEdge()

        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            ScotlandYardTheme { ScotlandYardApp() }
        }
    }
}

@Composable
private fun ScotlandYardApp() {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    CheatKeyEventRegistry.notify(event.nativeKeyEvent)
                }
                false
            }
    ) {
        val authViewModel: AuthViewModel = viewModel()
        val isConnected by authViewModel.isConnected.collectAsState()
        val currentUser by authViewModel.currentUser.collectAsState()
        val errorMessage by authViewModel.errorMessage.collectAsState()

        val navController = rememberNavController()
        val context = LocalContext.current

        var sharedLobbyViewModel by remember { mutableStateOf<LobbyViewModel?>(null) }

        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        // Handle hardware back button based on current route
        DisposableEffect(context, currentRoute) {
            val activity = context as? ComponentActivity
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Block back button for game-critical screens
                    val isBlockedRoute = currentRoute in listOf(
                        "gameboard",
                        "roleSelection",
                        "assignstartposition"
                    )

                    if (!isBlockedRoute) {
                        isEnabled = false
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                        isEnabled = true
                    }
                }
            }
            activity?.onBackPressedDispatcher?.addCallback(callback)
            onDispose { callback.remove() }
        }

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

        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            isConnected = isConnected,
            currentUser = currentUser,
            currentRoute = currentRoute,
            sharedLobbyViewModel = sharedLobbyViewModel,
            onSharedLobbyViewModelChange = { sharedLobbyViewModel = it }
        )
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isConnected: Boolean,
    currentUser: User?,
    currentRoute: String?,
    sharedLobbyViewModel: LobbyViewModel?,
    onSharedLobbyViewModelChange: (LobbyViewModel) -> Unit
) {
    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(
                onStartGame = { navController.navigate("login") },
                onRules     = { navController.navigate("rules") },
                onSettings  = { navController.navigate("settings") }
            )
        }

        composable("login") {
            val isNavigating = remember { mutableStateOf(false) }

            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate("lobby") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LaunchedEffect(isNavigating.value) {
                if (isNavigating.value) {
                    delay(500.milliseconds)
                    isNavigating.value = false
                }
            }

            LoginScreen(
                onConnectClick      = { nickname -> authViewModel.connectUser(nickname) },
                onBackClick         = {
                    if (!isNavigating.value && navController.previousBackStackEntry != null) {
                        isNavigating.value = true
                        navController.popBackStack()
                    }
                },
                onRefreshClick      = { authViewModel.reconnect() },
                isConnectedToServer = isConnected
            )
        }

        composable("rules") {
            val isNavigating = remember { mutableStateOf(false) }

            LaunchedEffect(isNavigating.value) {
                if (isNavigating.value) {
                    delay(500.milliseconds)
                    isNavigating.value = false
                }
            }

            RulesScreen(onBackClick = {
                if (!isNavigating.value && navController.previousBackStackEntry != null) {
                    isNavigating.value = true
                    navController.popBackStack()
                }
            })
        }

        composable("lobby") {
            val isNavigating = remember { mutableStateOf(false) }
            val user = currentUser

            LaunchedEffect(isNavigating.value) {
                if (isNavigating.value) {
                    delay(500.milliseconds)
                    isNavigating.value = false
                }
            }

            if (user != null) {
                LobbyScreen(
                    authViewModel = authViewModel,
                    onBackClick   = {
                        if (!isNavigating.value && navController.previousBackStackEntry != null) {
                            isNavigating.value = true
                            navController.popBackStack()
                        }
                    },
                    onProceedToRoles = { lobbyViewModel ->
                        onSharedLobbyViewModelChange(lobbyViewModel)
                        lobbyViewModel.startRoleSelection()
                        navController.navigate("roleSelection")
                    },
                    onNavigateToRoleSelection = { lobbyViewModel ->
                        onSharedLobbyViewModelChange(lobbyViewModel)
                        if (currentRoute != "roleSelection") navController.navigate("roleSelection")
                    },
                    userId   = user.id,
                    userName = user.nickName
                )
            }
        }

        composable("roleSelection") {
            val lobbyVm = sharedLobbyViewModel
            if (lobbyVm != null) {
                RoleSelectionRoute(lobbyVm = lobbyVm, navController = navController)
            }
        }

        composable("settings") {
            val isNavigating = remember { mutableStateOf(false) }

            LaunchedEffect(isNavigating.value) {
                if (isNavigating.value) {
                    delay(500.milliseconds)
                    isNavigating.value = false
                }
            }

            SettingsScreen(onBackClick = {
                if (!isNavigating.value && navController.previousBackStackEntry != null) {
                    isNavigating.value = true
                    navController.popBackStack()
                }
            })
        }

        composable(
            route = "assignstartposition/{gameId}/{playerId}",
            arguments = listOf(
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isNavigating = remember { mutableStateOf(false) }
            val gameId   = backStackEntry.arguments?.getString("gameId")   ?: ""
            val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
            val lobbyVm  = sharedLobbyViewModel

            LaunchedEffect(isNavigating.value) {
                if (isNavigating.value) {
                    delay(500.milliseconds)
                    isNavigating.value = false
                }
            }

            AssignStartPositionScreen(
                gameId   = gameId,
                playerId = playerId,
                onBackClick = {
                    if (!isNavigating.value && navController.previousBackStackEntry != null) {
                        isNavigating.value = true
                        navController.popBackStack()
                    }
                },
                onPositionConfirmed = {
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
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType },
                navArgument("isMrX")   { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val gameId   = backStackEntry.arguments?.getString("gameId")   ?: ""
            val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
            val isMrX    = backStackEntry.arguments?.getBoolean("isMrX")  ?: false
            GameBoardRoute(
                gameId       = gameId,
                playerId     = playerId,
                isMrX        = isMrX,
                navController = navController
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

@Composable
private fun GameBoardRoute(
    gameId: String,
    playerId: String,
    isMrX: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val displayMode = remember { context.getDisplayModePreference() }
    val gameViewModel: GameViewModel = viewModel()

    LaunchedEffect(gameId) {
        gameViewModel.gameStompService.subscribe(gameId)
        gameViewModel.isConnected.first { it }
        delay(600.milliseconds)
        gameViewModel.requestGameState(gameId)
        delay(3.seconds)
        if (gameViewModel.gameState.value == null) {
            Log.d("MainActivity", "GameState still null after 3 s – retrying requestGameState")
            gameViewModel.requestGameState(gameId)
        }
    }

    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(gameState) {
        gameViewModel.updateMyPosition(playerId, isMrX)
    }

    val isMyTurn = remember(gameState) {
        gameState?.let { if (isMrX) it.isMrXPhase else it.isDetectivesPhase } ?: false
    }

    val detectiveIdOrder = gameState?.detectivePositions?.keys?.sorted() ?: emptyList()
    val playerPositions  = gameViewModel.buildPlayerPositions(isMrX, detectiveIdOrder)

    var selectedTicket by remember { mutableStateOf<TicketType?>(null) }

    LaunchedEffect(!isMyTurn && selectedTicket != null) {
        selectedTicket = null
    }

    val myPosition by gameViewModel.myPosition.collectAsState()

    GameOverEffect(gameId, playerId, isMrX, navController, gameViewModel)

    val highlightedNodes = remember(selectedTicket, myPosition) {
        val ticket = selectedTicket
        val pos    = myPosition
        if (ticket != null && pos != null) gameViewModel.reachableStations(ticket) else emptySet()
    }

    val ticketCounts   = remember(gameState) { gameViewModel.getTicketCounts(playerId, isMrX) }
    val isDoubleActive = gameState?.doubleMoveActive ?: false
    val mrXRevealed    = if (!isMrX) gameState?.mrXRevealedPositions ?: emptyMap() else emptyMap()
    val mrXHistory     = if (!isMrX) gameState?.mrXMoveHistory ?: emptyList() else emptyList()

    GameBoardScreen(
        isMrX                = isMrX,
        mrXRevealedPositions = mrXRevealed,
        currentRound         = gameState?.currentRound ?: 1,
        displayMode          = displayMode,
        playerPositions      = playerPositions,
        highlightedNodes     = highlightedNodes,
        isMyTurn             = isMyTurn,
        selectedTicket       = selectedTicket,
        mrXMoveHistory       = mrXHistory,
        ticketCounts         = ticketCounts.toMutableMap().apply {
            if (isDoubleActive) put(TicketType.DOUBLE, 0)
        },
        onTicketSelect = { ticket ->
            if (isMyTurn) selectedTicket = if (selectedTicket == ticket) null else ticket
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

@Composable
private fun RoleSelectionRoute(lobbyVm: LobbyViewModel, navController: NavHostController) {
    val isNavigating = remember { mutableStateOf(false) }
    val lobby by lobbyVm.currentLobby.collectAsState()

    LaunchedEffect(isNavigating.value) {
        if (isNavigating.value) {
            delay(500.milliseconds)
            isNavigating.value = false
        }
    }

    LaunchedEffect(lobbyVm) {
        lobbyVm.navigateToGame.collect { gameId ->
            val playerId = lobbyVm.userId
            navController.navigate("assignstartposition/$gameId/$playerId") {
                popUpTo("roleSelection") { inclusive = true }
            }
        }
    }

    // Gäste: navigieren zurück wenn Host "Back to Lobby" auslöst.
    // Route-Guard verhindert doppeltes popBackStack beim Host, der bereits
    // sofort über den Button navigiert hat.
    LaunchedEffect("backToLobby", lobbyVm) {
        lobbyVm.navigateToLobby.collect {
            if (navController.currentBackStackEntry?.destination?.route == "roleSelection") {
                navController.popBackStack()
            }
        }
    }

    if (lobby != null) {
        RoleSelectionScreen(
            viewModel   = lobbyVm,
            lobby       = lobby!!,
            onBackClick = {
                if (!isNavigating.value && navController.previousBackStackEntry != null) {
                    isNavigating.value = true
                    navController.popBackStack()
                }
            },
            onGameStart = { lobbyVm.startGame() }
        )
    }
}

@Composable
private fun GameOverEffect(
    gameId: String,
    playerId: String,
    isMrX: Boolean,
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
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
}



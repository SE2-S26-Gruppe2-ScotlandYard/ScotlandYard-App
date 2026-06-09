package at.aau.serg.scotlandyard.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import at.aau.serg.scotlandyard.model.BoardDisplayMode // TEST (LÖSCHEN)
import at.aau.serg.scotlandyard.model.LobbyData // TEST (LÖSCHEN)
import at.aau.serg.scotlandyard.model.LobbyUserData // TEST (LÖSCHEN)
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import androidx.compose.runtime.collectAsState
import android.util.Log
import at.aau.serg.scotlandyard.data.*
import at.aau.serg.scotlandyard.network.ServerConfig
import com.example.scotlandyard.R
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BoardConnection.init(this)
        ServerConfig.init(this)
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

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun ScotlandYardApp() {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val context = LocalContext.current
    var currentLang by remember { mutableStateOf(context.getLanguagePreference()) }
    val localizedContext = remember(currentLang) { context.applyLanguage(currentLang) }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                        return@onPreviewKeyEvent true
                    }
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

            var sharedLobbyViewModel by remember { mutableStateOf<LobbyViewModel?>(null) }

            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            LaunchedEffect(isConnected) {
                if (!isConnected && currentRoute != null
                    && currentRoute != "start" && currentRoute != "login"
                    && currentRoute != "settings") {
                    Toast.makeText(context,
                        context.getString(R.string.toast_connection_lost), Toast.LENGTH_LONG).show()
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
                onSharedLobbyViewModelChange = { sharedLobbyViewModel = it },
                onLanguageChange = { lang ->
                    context.saveLanguagePreference(lang)
                    currentLang = lang
                }
            )
            // Registered after NavHost so it wins in LIFO order — blocks all hardware back presses.
            BackHandler(enabled = true) {}
        }
    }
}

@Composable
private fun rememberIsNavigating(): androidx.compose.runtime.MutableState<Boolean> {
    val state = remember { mutableStateOf(false) }
    LaunchedEffect(state.value) {
        if (state.value) { delay(500.milliseconds); state.value = false }
    }
    return state
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isConnected: Boolean,
    currentUser: User?,
    currentRoute: String?,
    sharedLobbyViewModel: LobbyViewModel?,
    onSharedLobbyViewModelChange: (LobbyViewModel) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartRoute(navController, currentUser) }
        composable("login") { LoginRoute(navController, currentUser, authViewModel, isConnected) }
        composable("rules") { RulesRoute(navController) }
        composable("lobby") {
            LobbyRoute(navController, currentUser, authViewModel, currentRoute, sharedLobbyViewModel, onSharedLobbyViewModelChange)
        }
        composable("roleSelection") {
            sharedLobbyViewModel?.let { RoleSelectionRoute(lobbyVm = it, navController = navController) }
        }
        composable("settings") { SettingsRoute(navController, authViewModel, onLanguageChange) }
        composable(
            route = "assignstartposition/{gameId}/{playerId}",
            arguments = listOf(
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            AssignStartPositionRoute(
                navController        = navController,
                sharedLobbyViewModel = sharedLobbyViewModel,
                gameId               = backStackEntry.arguments?.getString("gameId")   ?: "",
                playerId             = backStackEntry.arguments?.getString("playerId") ?: ""
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
            GameBoardRoute(
                gameId        = backStackEntry.arguments?.getString("gameId")   ?: "",
                playerId      = backStackEntry.arguments?.getString("playerId") ?: "",
                isMrX         = backStackEntry.arguments?.getBoolean("isMrX")  ?: false,
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

        // =================== TEST (LÖSCHEN) ===================
        composable("test_roleselection") {
            var testRoles by remember { mutableStateOf<Map<String, String>>(mapOf("1" to "DETECTIVE", "3" to "DETECTIVE")) }
            val dummyLobby = LobbyData(
                id = "test-id", name = "Test Lobby", hostId = "1", isStarted = false,
                readyStatus = emptyMap(),
                users = listOf(LobbyUserData("1", "Alice"), LobbyUserData("3", "Charlie"), LobbyUserData("2", "Bob")),
                selectedRoles = testRoles.toMutableMap()
            )
            RoleSelectionContent(
                localUserId = "2", isHost = true, lobby = dummyLobby,
                onRoleSelect = { role -> testRoles = testRoles.toMutableMap().also { it["2"] = role } },
                onBackClick = { navController.popBackStack() },
                onGameStart = {}
            )
        }
        composable("test_roleselection_mrx") {
            var testRoles by remember { mutableStateOf<Map<String, String>>(mapOf("1" to "DETECTIVE", "3" to "MRX")) }
            val dummyLobby = LobbyData(
                id = "test-id-2", name = "Test Lobby MrX", hostId = "1", isStarted = false,
                readyStatus = emptyMap(),
                users = listOf(LobbyUserData("1", "Alice"), LobbyUserData("3", "Charlie"), LobbyUserData("2", "Bob")),
                selectedRoles = testRoles.toMutableMap()
            )
            RoleSelectionContent(
                localUserId = "2", isHost = true, lobby = dummyLobby,
                onRoleSelect = { role -> testRoles = testRoles.toMutableMap().also { it["2"] = role } },
                onBackClick = { navController.popBackStack() },
                onGameStart = {}
            )
        }
        composable("test_positionselection") {
            AssignStartPositionScreen(
                gameId = "test-game", playerId = "test-player",
                onBackClick = { navController.popBackStack() },
                onPositionConfirmed = {}
            )
        }
        composable("test_gameboard") {
            GameBoardScreen(
                displayMode = BoardDisplayMode.MAP,
                isMrX = false,
                isMyTurn = true,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        // =================== END TEST (LÖSCHEN) ===================
    }
}

@Composable
private fun StartRoute(navController: NavHostController, currentUser: User?) {
    val isNavigating = rememberIsNavigating()
    StartScreen(
        onStartGame = {
            if (!isNavigating.value) {
                isNavigating.value = true
                if (currentUser != null) navController.navigate("lobby") else navController.navigate("login")
            }
        },
        onRules     = { if (!isNavigating.value) { isNavigating.value = true; navController.navigate("rules") } },
        onSettings  = { if (!isNavigating.value) { isNavigating.value = true; navController.navigate("settings") } },
        // =================== TEST (LÖSCHEN) ===================
        onTestNavigate = { route -> navController.navigate(route) }
        // =================== END TEST (LÖSCHEN) ===================
    )
}

@Composable
private fun LoginRoute(
    navController: NavHostController,
    currentUser: User?,
    authViewModel: AuthViewModel,
    isConnected: Boolean
) {
    val isNavigating = rememberIsNavigating()
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("lobby") { popUpTo("login") { inclusive = true } }
        }
    }
    LoginScreen(
        onConnectClick      = { nickname -> authViewModel.connectUser(nickname) },
        onBackClick         = {
            if (!isNavigating.value && navController.previousBackStackEntry != null) {
                isNavigating.value = true; navController.popBackStack()
            }
        },
        onRefreshClick      = { authViewModel.reconnect() },
        isConnectedToServer = isConnected
    )
}

@Composable
private fun RulesRoute(navController: NavHostController) {
    val isNavigating = rememberIsNavigating()
    RulesScreen(onBackClick = {
        if (!isNavigating.value && navController.previousBackStackEntry != null) {
            isNavigating.value = true; navController.popBackStack()
        }
    })
}

@Composable
private fun LobbyRoute(
    navController: NavHostController,
    currentUser: User?,
    authViewModel: AuthViewModel,
    currentRoute: String?,
    sharedLobbyViewModel: LobbyViewModel?,
    onSharedLobbyViewModelChange: (LobbyViewModel) -> Unit
) {
    val isNavigating = rememberIsNavigating()
    val user = currentUser ?: return
    LobbyScreen(
        authViewModel = authViewModel,
        onBackClick   = {
            if (!isNavigating.value && navController.previousBackStackEntry != null) {
                isNavigating.value = true; navController.popBackStack()
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

@Composable
private fun SettingsRoute(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLanguageChange: (String) -> Unit
) {
    val isNavigating = rememberIsNavigating()
    SettingsScreen(
        onBackClick = {
            if (!isNavigating.value && navController.previousBackStackEntry != null) {
                isNavigating.value = true; navController.popBackStack()
            }
        },
        onLanguageChange = onLanguageChange,
        onServerChange   = { authViewModel.reconnect() }
    )
}

@Composable
private fun AssignStartPositionRoute(
    navController: NavHostController,
    sharedLobbyViewModel: LobbyViewModel?,
    gameId: String,
    playerId: String
) {
    val isNavigating = rememberIsNavigating()
    AssignStartPositionScreen(
        gameId   = gameId,
        playerId = playerId,
        onBackClick = {
            if (!isNavigating.value && navController.previousBackStackEntry != null) {
                isNavigating.value = true; navController.popBackStack()
            }
        },
        onPositionConfirmed = {
            val selectedRoles = sharedLobbyViewModel?.currentLobby?.value?.selectedRoles
            val isMrX = selectedRoles?.get(playerId) == "MRX"
            navController.navigate("gameboard/$gameId/$playerId/$isMrX") {
                popUpTo("assignstartposition/$gameId/$playerId") { inclusive = true }
            }
        }
    )
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
        delay(600)
        gameViewModel.requestGameState(gameId)
        delay(3_000)
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
    val lobby by lobbyVm.currentLobby.collectAsState()

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

    val isNavigating = remember { mutableStateOf(false) }
    LaunchedEffect(isNavigating.value) {
        if (isNavigating.value) { delay(500.milliseconds); isNavigating.value = false }
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



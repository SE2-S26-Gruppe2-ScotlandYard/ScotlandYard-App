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
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.ui.theme.MRX_COLOR
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.RejoinEvent
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import androidx.compose.runtime.collectAsState
import android.util.Log
import at.aau.serg.scotlandyard.data.*
import at.aau.serg.scotlandyard.network.ServerConfig
import at.aau.serg.scotlandyard.R
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
            val rejoinEvent by authViewModel.rejoinEvent.collectAsState()
            LaunchedEffect(rejoinEvent, currentRoute) {
                if (currentRoute != null && currentRoute != "lobby" && rejoinEvent != null) {
                    navController.navigate("lobby") { popUpTo(0) }
                    authViewModel.clearRejoinEvent()
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
private fun rememberIsNavigating(): MutableState<Boolean> {
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
    val context = LocalContext.current
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val savedGameId = context.getGameId()
            val savedPlayerId = context.getPlayerId()
            if (savedGameId != null && savedPlayerId != null && currentRoute != null && !currentRoute.startsWith("gameboard")) {
                val isMrX = context.getIsMrX()
                navController.navigate("gameboard/$savedGameId/$savedPlayerId/$isMrX") { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartRoute(navController, currentUser, authViewModel) }
        composable("login") { LoginRoute(navController, currentUser, authViewModel, isConnected) }
        composable("rules") { RulesRoute(navController) }
        composable("lobby") {
            LobbyRoute(navController, currentUser, authViewModel, currentRoute, onSharedLobbyViewModelChange)
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
        composable(
            route = "mrxwin/{isMrX}",
            arguments = listOf(navArgument("isMrX") { type = NavType.BoolType })
        ) { backStackEntry ->
            MrXWinScreen(
                isMrX = backStackEntry.arguments?.getBoolean("isMrX") ?: false,
                onMainMenu = { context.clearSession(); navController.navigate("start") { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(
            route = "detectiveswin/{isMrX}",
            arguments = listOf(navArgument("isMrX") { type = NavType.BoolType })
        ) { backStackEntry ->
            DetectivesWinScreen(
                isMrX = backStackEntry.arguments?.getBoolean("isMrX") ?: false,
                onMainMenu = { context.clearSession(); navController.navigate("start") { popUpTo(0) { inclusive = true } } }
            )
        }
    }
}

@Composable
private fun StartRoute(
    navController: NavHostController,
    currentUser: User?,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var awaitingAutoConnect by remember { mutableStateOf(false) }
    LaunchedEffect(currentUser, awaitingAutoConnect) {
        if (currentUser != null && awaitingAutoConnect) {
            awaitingAutoConnect = false
            val savedGameId = context.getGameId()
            val savedPlayerId = context.getPlayerId()
            if (savedGameId != null && savedPlayerId != null) {
                val isMrX = context.getIsMrX()
                navController.navigate("gameboard/$savedGameId/$savedPlayerId/$isMrX") {
                    popUpTo(0)
                }
            } else {
                navController.navigate("lobby") { popUpTo("start") }
            }
        }
    }
    val isNavigating = rememberIsNavigating()
    StartScreen(
        onStartGame = {
            if (!isNavigating.value) {
                when {
                    currentUser != null -> {
                        isNavigating.value = true
                        navController.navigate("lobby")
                    }
                    authViewModel.tryAutoConnect() -> {
                        isNavigating.value = true
                        awaitingAutoConnect = true
                    }
                    else -> {
                        isNavigating.value = true
                        navController.navigate("login")
                    }
                }
            }
        },
        onRules     = { if (!isNavigating.value) { isNavigating.value = true; navController.navigate("rules") } },
        onSettings  = { if (!isNavigating.value) { isNavigating.value = true; navController.navigate("settings") } }
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
    val context = LocalContext.current
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val savedGameId = context.getGameId()
            val savedPlayerId = context.getPlayerId()
            if (savedGameId != null && savedPlayerId != null) {
                val isMrX = context.getIsMrX()
                navController.navigate("gameboard/$savedGameId/$savedPlayerId/$isMrX") {
                    popUpTo(0)
                }
            } else {
                navController.navigate("lobby") { popUpTo("login") { inclusive = true } }
            }
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
    onSharedLobbyViewModelChange: (LobbyViewModel) -> Unit
) {
    val isNavigating = rememberIsNavigating()
    val user = currentUser ?: return
    val context = LocalContext.current

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
    val isInGame = remember {
        navController.previousBackStackEntry?.destination?.route?.contains("gameboard") == true
    }
    val currentUser by authViewModel.currentUser.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    SettingsScreen(
        onBackClick = {
            if (!isNavigating.value && navController.previousBackStackEntry != null) {
                isNavigating.value = true; navController.popBackStack()
            }
        },
        isInGame = isInGame,
        onLanguageChange = onLanguageChange,
        onServerChange   = { authViewModel.reconnect() },
        onNicknameChange = { newNickname -> authViewModel.renameNickname(newNickname) },
        currentNickname = currentUser?.nickName ?: "",
        nicknameError = errorMessage,
        onAccountTabSelected = {
            if (currentUser == null) {
                authViewModel.tryAutoConnect()
            }
        }
    )
}

@Composable
private fun AssignStartPositionRoute(
    navController: NavHostController,
    sharedLobbyViewModel: LobbyViewModel?,
    gameId: String,
    playerId: String
) {
    AssignStartPositionScreen(
        gameId   = gameId,
        playerId = playerId,
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
        delay(600.milliseconds)
        gameViewModel.requestGameState(gameId)
        delay(3_000.milliseconds)
        if (gameViewModel.gameState.value == null) {
            Log.d("MainActivity", "GameState still null after 3 s – retrying requestGameState")
            gameViewModel.requestGameState(gameId)
            delay(2_000.milliseconds)
        }

        if (gameViewModel.gameState.value == null) {
            Log.w("MainActivity", "GameState is null. Game '$gameId' does not exist. Returning to Lobby.")
            context.clearSession()
            Toast.makeText(context, context.getString(R.string.toast_game_not_found), Toast.LENGTH_LONG).show()
            navController.navigate("start") { popUpTo(0) }
        } else {
            context.saveGameId(gameId)
            context.savePlayerInfo(playerId, isMrX)
            context.saveLobbyId(null)
        }
    }

    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(gameState) {
        gameViewModel.updateMyPosition(playerId, isMrX)
    }

    val isMyTurn = remember(gameState) {
        gameState?.let {
            if (isMrX) it.isMrXPhase || it.doubleMoveActive
            else it.isDetectivesPhase
        } ?: false
    }

    val lastDetectiveMoveRound by gameViewModel.lastDetectiveMoveRound.collectAsState()
    val allPlayersReady = gameState?.allPlayersReady ?: false
    val movedThisTurn = !isMrX && lastDetectiveMoveRound == (gameState?.currentRound ?: -2)
    val effectiveIsMyTurn = isMyTurn && !movedThisTurn && allPlayersReady

    val detectiveIdOrder = gameState?.detectivePositions?.keys?.sorted() ?: emptyList()
    val playerPositions  = gameViewModel.buildPlayerPositions(isMrX, detectiveIdOrder)

    var selectedTicket by remember { mutableStateOf<TicketType?>(null) }

    LaunchedEffect(!effectiveIsMyTurn && selectedTicket != null) {
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

    val ticketReachable = remember(myPosition) {
        if (myPosition == null) emptyMap()
        else mapOf(
            TicketType.WALKING    to gameViewModel.reachableStations(TicketType.WALKING).isNotEmpty(),
            TicketType.ESCOOTER   to gameViewModel.reachableStations(TicketType.ESCOOTER).isNotEmpty(),
            TicketType.CARSHARING to gameViewModel.reachableStations(TicketType.CARSHARING).isNotEmpty(),
            TicketType.BLACK      to true,
            TicketType.DOUBLE     to true
        )
    }
    val isDoubleActive = gameState?.doubleMoveActive ?: false
    val mrXRevealed    = if (!isMrX) gameState?.mrXRevealedPositions ?: emptyMap() else emptyMap()
    val mrXHistory     = if (!isMrX) gameState?.mrXMoveHistory ?: emptyList() else emptyList()
    val revealHistoryIndices by gameViewModel.revealHistoryIndices.collectAsState()

    val currentPlayerColor = remember(myPosition, playerPositions, isMrX) {
        if (isMrX) {
            MRX_COLOR
        } else if (myPosition != null) {
            playerPositions.entries.find { it.value == myPosition }?.key
        } else {
            null
        }
    }

    val hostId = gameState?.hostId
    val playerNames = gameState?.playerNames ?: emptyMap()
    val isHost = hostId != null && hostId == playerId
    var showKickDialog by remember { mutableStateOf(false) }

    if (showKickDialog && playerNames.isNotEmpty()) {
        KickPlayerDialog(
            playerNames = playerNames,
            hostId = hostId ?: "",
            mrXId = null,
            disconnectedPlayers = gameState?.disconnectedPlayers ?: emptySet(),
            onKick = { targetId ->
                gameViewModel.kickPlayer(gameId, playerId, targetId)
                showKickDialog = false
            },
            onOpenSettings = {
                showKickDialog = false
                navController.navigate("settings")
            },
            onDismiss = { showKickDialog = false },
            onDeleteGame = {
                gameViewModel.deleteGame(gameId, playerId)
                showKickDialog = false
            }
        )
    }

    GameBoardScreen(
        isMrX                = isMrX,
        mrXRevealedPositions = mrXRevealed,
        currentRound         = gameState?.currentRound ?: 1,
        displayMode          = displayMode,
        playerPositions      = playerPositions,
        highlightedNodes     = highlightedNodes,
        isMyTurn             = effectiveIsMyTurn,
        isDoubleActive       = isDoubleActive,
        selectedTicket       = selectedTicket,
        currentPlayerColor   = currentPlayerColor,
        ticketReachable      = ticketReachable,
        allPlayersReady      = allPlayersReady,
        mrXMoveHistory       = mrXHistory,
        revealHistoryIndices = revealHistoryIndices,
        ticketCounts         = ticketCounts.toMutableMap().apply {
            if (isDoubleActive) put(TicketType.DOUBLE, 0)
        },
        onTicketSelect = { ticket ->
            if (effectiveIsMyTurn) {
                if (ticket == TicketType.DOUBLE) {
                    gameViewModel.activateDoubleMove(gameId, playerId)
                } else {
                    selectedTicket = if (selectedTicket == ticket) null else ticket
                }
            }
        },
        onNodeClick = { stationId ->
            val ticket = selectedTicket
            if (ticket != null && stationId in highlightedNodes) {
                gameViewModel.sendMove(gameId, playerId, ticket, stationId)
                selectedTicket = null
                if (!isMrX) gameViewModel.recordDetectiveMove()
            }
        },
        onNavigateToSettings = {
            if (isHost && playerNames.isNotEmpty()) {
                showKickDialog = true
            } else {
                navController.navigate("settings")
            }
        }
    )
}

@Composable
private fun RoleSelectionRoute(lobbyVm: LobbyViewModel, navController: NavHostController) {
    val lobby       by lobbyVm.currentLobby.collectAsState()
    val isConnected by lobbyVm.isConnected.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(lobbyVm) {
        lobbyVm.navigateToGame.collect { gameId ->
            val playerId = lobbyVm.userId
            navController.navigate("assignstartposition/$gameId/$playerId") {
                popUpTo("roleSelection") { inclusive = true }
            }
        }
    }

    LaunchedEffect("backToLobby", lobbyVm) {
        lobbyVm.navigateToLobby.collect {
            if (navController.currentBackStackEntry?.destination?.route == "roleSelection") {
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(lobbyVm) {
        lobbyVm.errorEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(lobbyVm) {
        var hadLobby = false
        lobbyVm.currentLobby.collect { l ->
            if (l != null) {
                hadLobby = true
            } else if (hadLobby) {
                if (navController.currentBackStackEntry?.destination?.route == "roleSelection") {
                    navController.popBackStack()
                }
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
            isConnected = isConnected,
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
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        gameViewModel.gameOver.collect { result ->
            context.clearSession()
            when (result) {
                "DETECTIVES_WIN" -> navController.navigate("detectiveswin/$isMrX") {
                    popUpTo("gameboard/$gameId/$playerId/$isMrX") { inclusive = true }
                }
                "MRX_WINS" -> navController.navigate("mrxwin/$isMrX") {
                    popUpTo("gameboard/$gameId/$playerId/$isMrX") { inclusive = true }
                }
                "GAME_DELETED" -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_game_deleted_by_host),
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate("start") { popUpTo(0) }
                }
            }
        }
    }
}
package at.aau.serg.scotlandyard.ui.activity

import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
// import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.scotlandyard.model.StartPositionConstants
import at.aau.serg.scotlandyard.ui.components.SpinnerWheelPicker
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import at.aau.serg.scotlandyard.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

// Screen state machine

private enum class SpinnerScreenState {
    CONNECTING,         // WebSocket not ready yet
    WAITING_TO_SPIN,    // Connected – waiting for shake gesture to start spin
    SPINNER_ANIMATING,  // Auto-spin in progress
    SPINNER_DONE,       // Spin complete – result visible, awaiting user confirmation
    CHEAT_ACTIVE,       // Cheat/debug mode: user turns wheel manually
    WAITING_SERVER,     // Position sent to server – waiting for conflict-free confirmation
}

// Main composable

/**
 * AssignStartPositionScreen – shows a spinning wheel that selects the player's start position.
 *
 * **Normal flow**
 *  1. Screen waits for a WebSocket connection.
 *  2. Player shakes the device (or taps the emulator button) → wheel auto-spins (~3.5 s).
 *  3. Player taps *Bestätigen* → position is sent to backend and the next screen opens.
 *
 * **Cheat/debug flow**
 *  1. While this screen is visible the player holds **volume-down** and **shakes** the device.
 *  2. The wheel enters manual mode (orange highlight). The player scrolls to any position.
 *  3. Confirming the manual selection sends that exact position.
 */
@Composable
fun AssignStartPositionScreen(
    gameId: String = "",
    playerId: String = "",
    onPositionConfirmed: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameViewModel: GameViewModel = viewModel()

    val isConnected by gameViewModel.isConnected.collectAsState()
    val isCheatModeActive by gameViewModel.cheatModeActive.collectAsState()
    val startPosition by gameViewModel.startPosition.collectAsState()
    val errorMessage by gameViewModel.errorMessage.collectAsState()

    var screenState by remember { mutableStateOf(SpinnerScreenState.CONNECTING) }
    var triggerSpin by remember { mutableStateOf(false) }
    var manualPosition by remember { mutableIntStateOf(startPosition ?: StartPositionConstants.MIN_POSITION) }
    // Stores the position we sent to the server so we can detect a conflict-resolution response
    var sentPosition by remember { mutableIntStateOf(0) }
    // Set when the server assigned a different position than requested (conflict)
    var conflictPosition by remember { mutableStateOf<Int?>(null) }

    // Normal shake detector – triggers the auto-spin
    val shakeDetector = remember(context) { ShakeDetector(context) }


    // Once connected: subscribe and generate position, then WAIT for shake.
    // Keyed on (gameId, playerId, isConnected) so it only re-runs when these
    // stable values actually change – not on every unrelated recomposition.
    LaunchedEffect(gameId, playerId, isConnected) {
        if (isConnected && screenState == SpinnerScreenState.CONNECTING) {
            gameViewModel.subscribeToStartPosition(gameId, playerId)
            delay(300.milliseconds)
            gameViewModel.generateLocalStartPosition()
            manualPosition = gameViewModel.peekStartPosition() ?: StartPositionConstants.MIN_POSITION
            screenState = SpinnerScreenState.WAITING_TO_SPIN
        }
    }

    // Respond to cheat-mode flag
    LaunchedEffect(isCheatModeActive) {
        if (isCheatModeActive && screenState != SpinnerScreenState.CHEAT_ACTIVE) {
            manualPosition = startPosition ?: manualPosition
            screenState = SpinnerScreenState.CHEAT_ACTIVE
        }
    }

    // Server confirmed the start position – detect conflict (different position returned).
    LaunchedEffect(startPosition) {
        if (screenState == SpinnerScreenState.WAITING_SERVER && startPosition != null) {
            if (sentPosition > 0 && startPosition != sentPosition) {
                // Server assigned a different position → show info banner, then navigate
                conflictPosition = startPosition
                delay(3000.milliseconds)
            }
            onPositionConfirmed()
        }
    }

    // Timeout: if server doesn't respond within 10 s in WAITING_SERVER, surface error
    LaunchedEffect(screenState) {
        if (screenState == SpinnerScreenState.WAITING_SERVER) {
            delay(10_000.milliseconds)
            if (screenState == SpinnerScreenState.WAITING_SERVER) {
                gameViewModel.setError("Server hat nicht geantwortet. Bitte erneut versuchen.")
                screenState = SpinnerScreenState.SPINNER_DONE
            }
        }
    }

    // Disconnect handling after the connecting phase
    LaunchedEffect(isConnected) {
        if (!isConnected && screenState != SpinnerScreenState.CONNECTING) {
            gameViewModel.setError("Verbindung unterbrochen – verbinde erneut…")
        }
        if (isConnected && screenState != SpinnerScreenState.CONNECTING) {
            // Re-subscribe after reconnect so messages still arrive
            gameViewModel.subscribeToStartPosition(gameId, playerId)
        }
    }

    // Lifecycle-safe: register shake sensor
    DisposableEffect(shakeDetector) {
        // Normal shake → start the auto-spin
        shakeDetector.setOnShakeListener {
            Handler(Looper.getMainLooper()).post {
                if (screenState == SpinnerScreenState.WAITING_TO_SPIN) {
                    screenState = SpinnerScreenState.SPINNER_ANIMATING
                    triggerSpin = true
                }
            }
        }
        shakeDetector.start()

        onDispose {
            shakeDetector.stop()
            gameViewModel.deactivateCheatMode()
            gameViewModel.unsubscribeFromStartPosition()
        }
    }

    // Render
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF060810))) {
        Image(
            painter = painterResource(id = R.drawable.map_bw),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.18f)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1428).copy(alpha = 0.45f))
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (screenState) {
                SpinnerScreenState.CONNECTING -> ConnectingState()

                SpinnerScreenState.WAITING_TO_SPIN -> WaitingToSpinState(
                    onSimulateShake = {
                        screenState = SpinnerScreenState.SPINNER_ANIMATING
                        triggerSpin = true
                    },
                    onSimulateCheat = {
                        manualPosition = startPosition ?: StartPositionConstants.MIN_POSITION
                        gameViewModel.activateCheatMode()
                    }
                )

                SpinnerScreenState.SPINNER_ANIMATING,
                SpinnerScreenState.SPINNER_DONE -> SpinnerAutoState(
                    positions = StartPositionConstants.VALID_POSITIONS,
                    targetPosition = startPosition ?: sentPosition.takeIf { it > 0 } ?: StartPositionConstants.MIN_POSITION,
                    triggerSpin = triggerSpin,
                    isSpinComplete = screenState == SpinnerScreenState.SPINNER_DONE,
                    onSpinComplete = {
                        screenState = SpinnerScreenState.SPINNER_DONE
                        triggerSpin = false
                    },
                    onDoubleClick = {
                        manualPosition = startPosition ?: StartPositionConstants.MIN_POSITION
                        gameViewModel.activateCheatMode()
                    },
                    onConfirm = {
                        sentPosition = startPosition ?: StartPositionConstants.MIN_POSITION
                        gameViewModel.confirmStartPosition(gameId, playerId)
                        gameViewModel.clearStartPosition()
                        screenState = SpinnerScreenState.WAITING_SERVER
                    }
                )

                SpinnerScreenState.CHEAT_ACTIVE -> CheatModeSpinnerState(
                    positions = StartPositionConstants.VALID_POSITIONS,
                    selectedPosition = manualPosition,
                    onSelectionChanged = { manualPosition = it },
                    onConfirm = {
                        gameViewModel.setCheatStartPosition(manualPosition)
                        sentPosition = manualPosition
                        gameViewModel.confirmStartPosition(gameId, playerId)
                        gameViewModel.clearStartPosition()
                        gameViewModel.deactivateCheatMode()
                        screenState = SpinnerScreenState.WAITING_SERVER
                    }
                )

                SpinnerScreenState.WAITING_SERVER -> WaitingServerState()
            }

            // Conflict banner – shown at top when server assigned a different position
            AnimatedVisibility(
                visible = conflictPosition != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .background(Color(0xFF1A3A5C), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF4A90D9).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.start_position_taken, conflictPosition ?: 0),
                        color = Color(0xFFB3D9FF),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Non-blocking error snack
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0xFF3D1F1F), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFFB3B3),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Sub-composables

@Composable
private fun ConnectingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.status_connecting_to_server), color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun WaitingServerState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.status_confirming_position), color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.text_please_wait), color = TextLight, fontSize = 12.sp)
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun WaitingToSpinState(
    onSimulateShake: () -> Unit,
    onSimulateCheat: () -> Unit = {}
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 12.dp)) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📳", fontSize = 144.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.assign_start_position_text_shake_device),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onSimulateShake,
                    modifier = Modifier.alpha(0.65f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text(stringResource(R.string.button_simulate_shaking), fontSize = 14.sp, color = TextLight)
                }
            }
            // Bottom-right: barely visible cheat
//            Box(
//                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 8.dp).alpha(0.13f)
//            ) {
//                TextButton(onClick = onSimulateCheat) {
//                    Text(stringResource(R.string.button_cheat), fontSize = 11.sp, color = DetectiveBlue)
//                }
//            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Shake instruction — centered
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📳",
                    fontSize = 88.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.assign_start_position_text_shake_device),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            // Simulate shaking — below center, slightly transparent
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
                    .alpha(0.65f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSimulateShake,
                    modifier = Modifier.fillMaxWidth(0.65f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text(stringResource(R.string.button_simulate_shaking), fontSize = 15.sp, color = TextLight)
                }
            }
            // Bottom-right: barely visible cheat
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(end = 12.dp, bottom = 12.dp)
//                    .alpha(0.13f)
//            ) {
//                TextButton(onClick = onSimulateCheat) {
//                    Text(stringResource(R.string.button_cheat), fontSize = 12.sp, color = DetectiveBlue)
//                }
//            }
        }
    }
}

/**
 * Normal auto-spin state: shows the spinning wheel with deceleration animation, then a
 * confirmation button once the wheel has stopped.
 */
@Composable
private fun SpinnerAutoState(
    positions: List<Int>,
    targetPosition: Int,
    triggerSpin: Boolean,
    isSpinComplete: Boolean,
    onSpinComplete: () -> Unit,
    onDoubleClick: () -> Unit = {},
    onConfirm: () -> Unit
) {
    var localTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(triggerSpin) {
        if (triggerSpin) { delay(50.milliseconds); localTrigger = true } else { localTrigger = false }
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape: wheel left, info + button right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: spinner wheel
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                SpinnerWheelPicker(
                    positions = positions,
                    targetPosition = targetPosition,
                    isCheatMode = false,
                    triggerSpin = localTrigger,
                    onSpinComplete = { localTrigger = false; onSpinComplete() },
                    onSelectionChanged = {},
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onDoubleClick() }) }
                )
            }
            // Right: title, result, button
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(start = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSpinComplete) stringResource(R.string.title_your_start_position) else stringResource(
                        R.string.status_rolling_start_position
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                if (isSpinComplete) {
                    Text(
                        text = stringResource(R.string.text_station, targetPosition),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(52.dp)
                            .border(1.5.dp, AccentGlow.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .alpha(0.75f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text(stringResource(R.string.button_start_game), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 2.dp)
                }
            }
        }
    } else {
        // Portrait: content scrollable, button pinned at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isSpinComplete) stringResource(R.string.title_your_start_position) else stringResource(
                        R.string.status_rolling_start_position
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                SpinnerWheelPicker(
                    positions = positions,
                    targetPosition = targetPosition,
                    isCheatMode = false,
                    triggerSpin = localTrigger,
                    onSpinComplete = { localTrigger = false; onSpinComplete() },
                    onSelectionChanged = {},
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onDoubleClick() }) }
                )
                Spacer(Modifier.height(24.dp))
                if (isSpinComplete) {
                    Text(text = stringResource(R.string.text_station, targetPosition), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                }
                Spacer(Modifier.height(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(visible = isSpinComplete) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(52.dp)
                        .border(1.5.dp, AccentGlow.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .alpha(0.75f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text(stringResource(R.string.button_start_game), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Cheat-mode state: manual wheel (orange accent), shows selected station, confirm button.
 */
@Composable
private fun CheatModeSpinnerState(
    positions: List<Int>,
    selectedPosition: Int,
    onSelectionChanged: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape: wheel left, controls right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: wheel with cheat border
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .border(2.dp, CheatOrange, RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    SpinnerWheelPicker(
                        positions = positions,
                        targetPosition = selectedPosition,
                        isCheatMode = true,
                        triggerSpin = false,
                        onSpinComplete = {},
                        onSelectionChanged = onSelectionChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            // Right: badge, title, station info, button
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(start = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(CheatOrange, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = stringResource(R.string.status_cheat_active), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(R.string.title_choose_start_position), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                Text(text = stringResource(R.string.text_spin_and_confirm), fontSize = 12.sp, color = TextLight, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.text_station, selectedPosition),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .border(1.5.dp, CheatOrange.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .alpha(0.75f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CheatOrange)
                ) {
                    Text(stringResource(R.string.button_start_game), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
                }
            }
        }
    } else {
        // Portrait: content scrollable, button pinned at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .background(CheatOrange, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = stringResource(R.string.status_cheat_active), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(20.dp))
                Text(text = stringResource(R.string.title_choose_start_position), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                Text(text = stringResource(R.string.text_spin_and_confirm), fontSize = 13.sp, color = TextLight, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .border(2.dp, CheatOrange, RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    SpinnerWheelPicker(
                        positions = positions,
                        targetPosition = selectedPosition,
                        isCheatMode = true,
                        triggerSpin = false,
                        onSpinComplete = {},
                        onSelectionChanged = onSelectionChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.text_station, selectedPosition),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp)
                    .border(1.5.dp, CheatOrange.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .alpha(0.75f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CheatOrange)
            ) {
                Text(stringResource(R.string.button_start_game), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextLight)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Previews

@Preview(showBackground = true, widthDp = 400, heightDp = 750)
@Composable
fun AssignStartPositionScreenPreview() {
    ScotlandYardTheme { AssignStartPositionScreen() }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 750, name = "Cheat Mode")
@Composable
fun AssignStartPositionCheatModePreview() {
    ScotlandYardTheme {
        BaseScreen(onBackClick = {}) { modifier ->
            Box(modifier = modifier.fillMaxSize()) {
                CheatModeSpinnerState(
                    positions = (1..200).toList(),
                    selectedPosition = 42,
                    onSelectionChanged = {},
                    onConfirm = {}
                )
            }
        }
    }
}

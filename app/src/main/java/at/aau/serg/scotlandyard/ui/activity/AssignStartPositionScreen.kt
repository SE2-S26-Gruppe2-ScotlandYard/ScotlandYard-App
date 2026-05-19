package at.aau.serg.scotlandyard.ui.activity

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.scotlandyard.model.StartPositionConstants
import at.aau.serg.scotlandyard.ui.components.SpinnerWheelPicker
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.GameViewModel
import kotlinx.coroutines.delay

// ── Screen state machine ─────────────────────────────────────────────────────────────────────

private enum class SpinnerScreenState {
    CONNECTING,         // WebSocket not ready yet
    WAITING_TO_SPIN,    // Connected – waiting for shake gesture to start spin
    SPINNER_ANIMATING,  // Auto-spin in progress
    SPINNER_DONE,       // Spin complete – result visible, awaiting user confirmation
    CHEAT_ACTIVE,       // Cheat/debug mode: user turns wheel manually
}

// ── Main composable ──────────────────────────────────────────────────────────────────────────

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
    onBackClick: () -> Unit = {},
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

    // Normal shake detector – triggers the auto-spin (no volume-down needed)
    val shakeDetector = remember(context) { ShakeDetector(context) }

    // CheatModeDetector – shake + volume-down activates cheat mode
    val cheatDetector = remember(context) {
        CheatModeDetector(context).apply {
            setOnCheatListener { gameViewModel.activateCheatMode() }
        }
    }

    // Once connected: subscribe and generate position, then WAIT for shake
    LaunchedEffect(isConnected) {
        if (isConnected && screenState == SpinnerScreenState.CONNECTING) {
            gameViewModel.subscribeToStartPosition(gameId, playerId)
            delay(300L)
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

    // Lifecycle-safe: register volume-key listener + both sensors
    DisposableEffect(shakeDetector, cheatDetector) {
        val keyListener: (KeyEvent) -> Unit = { event ->
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                cheatDetector.isVolumeDownHeld = (event.action == KeyEvent.ACTION_DOWN)
            }
        }
        CheatKeyEventRegistry.addListener(keyListener)

        // Normal shake (no volume-down) → start the auto-spin
        shakeDetector.setOnShakeListener {
            Handler(Looper.getMainLooper()).post {
                // Skip if volume-down is held – cheat mode detector handles that combo
                if (!cheatDetector.isVolumeDownHeld && screenState == SpinnerScreenState.WAITING_TO_SPIN) {
                    screenState = SpinnerScreenState.SPINNER_ANIMATING
                    triggerSpin = true
                }
            }
        }
        shakeDetector.start()
        cheatDetector.start()

        onDispose {
            CheatKeyEventRegistry.removeListener(keyListener)
            shakeDetector.stop()
            cheatDetector.stop()
            gameViewModel.deactivateCheatMode()
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────────────────────
    BaseScreen(onBackClick = onBackClick) { modifier ->
        Box(
            modifier = modifier.fillMaxSize(),
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
                    targetPosition = startPosition ?: StartPositionConstants.MIN_POSITION,
                    triggerSpin = triggerSpin,
                    isSpinComplete = screenState == SpinnerScreenState.SPINNER_DONE,
                    onSpinComplete = {
                        screenState = SpinnerScreenState.SPINNER_DONE
                        triggerSpin = false
                    },
                    onConfirm = {
                        gameViewModel.confirmStartPosition(gameId, playerId)
                        onPositionConfirmed()
                    }
                )

                SpinnerScreenState.CHEAT_ACTIVE -> CheatModeSpinnerState(
                    positions = StartPositionConstants.VALID_POSITIONS,
                    selectedPosition = manualPosition,
                    onSelectionChanged = { manualPosition = it },
                    onConfirm = {
                        gameViewModel.setCheatStartPosition(manualPosition)
                        gameViewModel.confirmStartPosition(gameId, playerId)
                        gameViewModel.deactivateCheatMode()
                        onPositionConfirmed()
                    }
                )
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

// ── Private sub-composables ──────────────────────────────────────────────────────────────────

@Composable
private fun ConnectingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(16.dp))
        Text("Verbinde mit Server…", color = Color.White, fontSize = 14.sp)
    }
}

/**
 * Waiting state: wheel is visible but static. Player must shake to start the spin.
 * A button is provided to simulate a shake (useful for emulator testing).
 * A second debug button activates cheat mode (for testing the volume-key combo on emulators).
 */
@Composable
private fun WaitingToSpinState(
    onSimulateShake: () -> Unit,
    onSimulateCheat: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Text(
            text = "📳",
            fontSize = 56.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Schüttle das Gerät\num deine Startposition\nzu bestimmen!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onSimulateShake,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4A3A))
        ) {
            Text(
                "Schütteln simulieren",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onSimulateCheat,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1A00))
        ) {
            Text(
                "🔧 Schummelmodus simulieren",
                fontSize = 13.sp,
                color = Color(0xFFFF9944)
            )
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
    onConfirm: () -> Unit
) {
    var localTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(triggerSpin) {
        if (triggerSpin) {
            delay(50)
            localTrigger = true
        } else {
            localTrigger = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = if (isSpinComplete) "🎯 Deine Startposition!" else "Startposition wird gewürfelt…",
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
            onSpinComplete = {
                localTrigger = false
                onSpinComplete()
            },
            onSelectionChanged = {},
            modifier = Modifier.fillMaxWidth(0.55f)
        )

        Spacer(Modifier.height(24.dp))

        if (isSpinComplete) {
            Text(
                text = "Station  $targetPosition",
                fontSize = 20.sp,
                color = Color(0xFFCCCCCC),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4A3A))
            ) {
                Text(
                    "Bestätigen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }

        Spacer(Modifier.height(20.dp))
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Cheat badge
        Box(
            modifier = Modifier
                .background(Color(0xFFFF6B00), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🔧  SCHUMMELMODUS AKTIV",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Wähle deine Startposition",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Drehe das Rad und bestätige deine Wahl",
            fontSize = 13.sp,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .border(2.dp, Color(0xFFFF6B00), RoundedCornerShape(12.dp))
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

        Box(
            modifier = Modifier
                .background(Color(0x33FF6B00), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Station  $selectedPosition  ausgewählt",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
        ) {
            Text(
                "Station $selectedPosition bestätigen",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────────────────────

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

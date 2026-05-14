package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.viewmodel.GameViewModel

/**
 * AssignStartPositionScreen displays a shake detection screen that:
 * 1. Shows a message to shake device
 * 2. Displays an animated shake icon
 * 3. Shows loading spinner during API call
 * 4. Displays assigned position with confirm button on success
 * 5. Shows error message with retry button on failure
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

    // Wenn gameId+playerId bereits übergeben → kein Test-Modus
    val testMode = gameId.isBlank() || playerId.isBlank()

    var inputGameId by rememberSaveable { mutableStateOf(gameId) }
    var inputPlayerId by rememberSaveable { mutableStateOf(playerId) }
    // Im normalen Spielflow: sofort als "subscribed" markieren
    var subscribed by rememberSaveable { mutableStateOf(!testMode) }

    // Observe states from ViewModel
    val isLoading by gameViewModel.isLoading.collectAsState()
    val startPosition by gameViewModel.startPosition.collectAsState()
    val errorMessage by gameViewModel.errorMessage.collectAsState()
    val isConnected by gameViewModel.isConnected.collectAsState()

    // Shake detector state
    var shakeDetector by remember { mutableStateOf<ShakeDetector?>(null) }

    // Sobald Verbindung steht UND subscribed = true → Topic abonnieren + ShakeDetector starten
    LaunchedEffect(isConnected, subscribed) {
        if (isConnected && subscribed) {
            gameViewModel.subscribeToStartPosition(inputGameId, inputPlayerId)
            if (shakeDetector == null) {
                shakeDetector = ShakeDetector(context).apply {
                    setOnShakeListener(object : ShakeDetector.OnShakeListener {
                        override fun onShake() {
                            gameViewModel.requestStartPosition(inputGameId, inputPlayerId)
                        }
                    })
                    start()
                }
            }
        }
    }

    // Clean up shake detector when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            shakeDetector?.stop()
        }
    }

    BaseScreen(onBackClick = onBackClick) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── TEST-EINGABEFELDER (nur im Test-Modus, wenn keine IDs übergeben) ──
            if (testMode && !subscribed) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "🧪 TEST-MODUS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Verbindungsstatus anzeigen
                Text(
                    text = if (isConnected) "🟢 Mit Server verbunden" else "🔴 Verbinde mit Server...",
                    fontSize = 12.sp,
                    color = if (isConnected) Color(0xFF90EE90) else Color(0xFFFF6B6B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputGameId,
                    onValueChange = { inputGameId = it },
                    label = { Text("Game ID", color = Color.White) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputPlayerId,
                    onValueChange = { inputPlayerId = it },
                    label = { Text("Player ID", color = Color.White) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (inputGameId.isNotBlank() && inputPlayerId.isNotBlank()) {
                            // Shake-Detector starten
                            shakeDetector = ShakeDetector(context).apply {
                                setOnShakeListener(object : ShakeDetector.OnShakeListener {
                                    override fun onShake() {
                                        gameViewModel.requestStartPosition(inputGameId, inputPlayerId)
                                    }
                                })
                                start()
                            }
                            subscribed = true
                            // Wenn bereits verbunden: sofort subscriben
                            // Wenn nicht: LaunchedEffect(isConnected) übernimmt das
                        }
                    },
                    enabled = inputGameId.isNotBlank() && inputPlayerId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text("Verbinden & Starten", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── STATUS-LEISTE (wenn subscribed, nur im Test-Modus sichtbar) ──
            if (testMode && subscribed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isConnected) "🟢 Verbunden | Topic aktiv" else "🔴 Verbindung unterbrochen",
                    fontSize = 11.sp,
                    color = if (isConnected) Color(0xFF90EE90) else Color(0xFFFF6B6B)
                )
                // 🔧 Simulate-Button: testet ob Frontend-Handling funktioniert
                if (isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            gameViewModel.onResponse(
                                "startPosition:{\"type\":\"START_POSITION_ASSIGNED\",\"gameId\":\"$inputGameId\",\"playerId\":\"$inputPlayerId\",\"startPosition\":42}"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🔧 Simulate Backend Response", fontSize = 11.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── HAUPT-CONTENT ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    testMode && !subscribed -> {
                        Text(
                            text = "Bitte Game ID und Player ID eingeben und auf \"Verbinden\" tippen.",
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                    !isConnected && subscribed -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Verbinde mit Server...", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    isLoading -> LoadingState()
                    errorMessage != null -> ErrorState(
                        errorMessage = errorMessage ?: "Unbekannter Fehler",
                        onRetry = { gameViewModel.requestStartPosition(inputGameId, inputPlayerId) }
                    )
                    startPosition != null -> SuccessState(
                        position = startPosition ?: 0,
                        onConfirm = {
                            gameViewModel.confirmStartPosition(inputGameId, inputPlayerId)
                            onPositionConfirmed()
                        }
                    )
                    else -> ShakeAwaitingState(
                        onSimulateShake = { gameViewModel.requestStartPosition(inputGameId, inputPlayerId) }
                    )
                }
            }
        }
    }
}

/**
 * Displays the shake awaiting UI with animated shake icon
 */
@Composable
private fun ShakeAwaitingState(onSimulateShake: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated Shake Icon
        AnimatedShakeIcon()

        Spacer(modifier = Modifier.height(32.dp))

        // Instructions
        Text(
            text = "Schütteln um Startposition zu erhalten!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Schüttele dein Handy, um deine Startposition zu erhalten",
            fontSize = 14.sp,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Emulator-Button: Schütteln simulieren
        Button(
            onClick = onSimulateShake,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4A3A)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Text("📳 Schütteln simulieren", fontSize = 16.sp, color = Color.White)
        }
    }
}

/**
 * Animated shake icon that indicates the device can be shaken
 */
@Composable
private fun AnimatedShakeIcon() {
    Box(
        modifier = Modifier
            .size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Custom shake icon using text emoji or icon with simple pulsing animation
        Text(
            text = "📱",
            fontSize = 80.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Shows loading spinner during API call
 */
@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color(0xFF1A4A3A),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Startposition wird ermittelt...",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bitte warte während deine Position von der Backend-API abgerufen wird",
            fontSize = 12.sp,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/**
 * Shows assigned position with confirm button
 */
@Composable
private fun SuccessState(position: Int, onConfirm: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "✓ Startposition zugewiesen",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Position display
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF1A4A3A),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 48.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Station",
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = position.toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Du startest auf Station $position",
            fontSize = 16.sp,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A4A3A))
        ) {
            Text(
                text = "Bestätigen",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Shows error message with retry button
 */
@Composable
private fun ErrorState(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Error icon
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Fehler",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFF6B6B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fehler beim Abrufen der Startposition",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message details
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF3D1F1F),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFFFB3B3),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Retry button
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxSize(0.8f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4A3A)
            )
        ) {
            Text(
                text = "Erneut versuchen",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AssignStartPositionScreenPreview() {
    ScotlandYardTheme {
        AssignStartPositionScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AssignStartPositionScreenLoadingPreview() {
    ScotlandYardTheme {
        BaseScreen(onBackClick = {}) { modifier ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingState()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AssignStartPositionScreenSuccessPreview() {
    ScotlandYardTheme {
        BaseScreen(onBackClick = {}) { modifier ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SuccessState(position = 50, onConfirm = {})
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AssignStartPositionScreenErrorPreview() {
    ScotlandYardTheme {
        BaseScreen(onBackClick = {}) { modifier ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ErrorState(
                    errorMessage = "Die Verbindung zum Server konnte nicht hergestellt werden",
                    onRetry = {}
                )
            }
        }
    }
}





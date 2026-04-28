package at.aau.serg.scotlandyard.ui.activity

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    gameId: String = "game123",
    playerId: String = "player456",
    onBackClick: () -> Unit = {},
    onPositionConfirmed: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameViewModel: GameViewModel = viewModel()

    // Observe states from ViewModel
    val isLoading by gameViewModel.isLoading.collectAsState()
    val startPosition by gameViewModel.startPosition.collectAsState()
    val errorMessage by gameViewModel.errorMessage.collectAsState()

    // Shake detector state
    var shakeDetector by remember { mutableStateOf<ShakeDetector?>(null) }

    // Initialize shake detector
    LaunchedEffect(Unit) {
        shakeDetector = ShakeDetector(context).apply {
            setOnShakeListener(object : ShakeDetector.OnShakeListener {
                override fun onShake() {
                    gameViewModel.requestStartPosition(gameId, playerId)
                }
            })
            start()
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> {
                    // Loading state
                    LoadingState()
                }
                errorMessage != null -> {
                    // Error state
                    ErrorState(
                        errorMessage = errorMessage ?: "Unbekannter Fehler",
                        onRetry = { gameViewModel.requestStartPosition(gameId, playerId) }
                    )
                }
                startPosition != null -> {
                    // Success state
                    SuccessState(
                        position = startPosition ?: 0,
                        onConfirm = {
                            gameViewModel.confirmStartPosition(gameId, playerId)
                            onPositionConfirmed()
                        }
                    )
                }
                else -> {
                    // Initial state - waiting for shake
                    ShakeAwaitingState()
                }
            }
        }
    }
}

/**
 * Displays the shake awaiting UI with animated shake icon
 */
@Composable
private fun ShakeAwaitingState() {
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
            .padding(24.dp)
    ) {
        // Success icon/message
        Text(
            text = "✓",
            fontSize = 64.sp,
            color = Color(0xFF1A4A3A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Startposition zugewiesen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Position display
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF1A4A3A),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Station",
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC),
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = position.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Du startest auf Station $position",
            fontSize = 16.sp,
            color = Color(0xFFCCCCCC),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxSize(0.8f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4A3A)
            )
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





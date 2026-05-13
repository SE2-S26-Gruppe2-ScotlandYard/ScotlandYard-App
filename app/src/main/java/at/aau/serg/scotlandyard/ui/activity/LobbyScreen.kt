package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.model.LobbyUserData
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModelFactory

private val BgDark        = Color(0xFF0D1B2A)
private val GreenButton   = Color(0xFF1A4A3A)
private val DarkButton    = Color(0xFF102920)
private val CardBg        = Color(0xFF152535)
private val BorderColor   = Color(0x44FFFFFF)
private val TextPrimary   = Color.White
private val TextSecondary = Color(0xFFCCCCCC)
private val AccentGold    = Color(0xFFFFD700)
private val AccentRed     = Color(0xFFE53935)

@Composable
fun LobbyScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onProceedToRoles: (LobbyViewModel) -> Unit,
    onNavigateToRoleSelection: (LobbyViewModel) -> Unit,
    userId: String,
    userName: String
) {
    val factory = LobbyViewModelFactory(userId, userName, authViewModel.getMyStomp())
    val viewModel: LobbyViewModel = viewModel(factory = factory)

    val currentLobby  by viewModel.currentLobby.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val isConnected   by viewModel.isConnected.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.navigateToRoleSelection.collect {
            onNavigateToRoleSelection(viewModel)
        }
    }

    BaseScreen(onBackClick = onBackClick) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "LOBBY",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 16.dp)
            )

            ConnectionBadge(isConnected = isConnected)
            Spacer(modifier = Modifier.height(16.dp))

            if (currentLobby == null) {
                LobbyBrowserView(
                    isLoading     = isLoading,
                    onCreateLobby = { viewModel.createLobby() },
                    onJoinLobby   = { code -> viewModel.joinLobby(code) }
                )
            } else {
                InLobbyView(
                    lobby            = currentLobby!!,
                    localUserId      = viewModel.userId,
                    isHost           = viewModel.isLocalUserHost(),
                    isLoading        = isLoading,
                    onLeave          = { viewModel.leaveLobby() },
                    onDelete         = { viewModel.deleteLobby() },
                    onKickPlayer     = { targetId -> viewModel.kickPlayer(targetId) },
                    onStartRoleSelection = { viewModel.startRoleSelection() }
                )
            }

            if (statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBar(message = statusMessage)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectionBadge(isConnected: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(modifier = Modifier
                .size(8.dp)
                .background(if (isConnected) Color.Green else AccentRed, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isConnected) "Verbunden" else "Getrennt",
                color = TextPrimary, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LobbyBrowserView(
    isLoading: Boolean,
    onCreateLobby: () -> Unit,
    onJoinLobby: (String) -> Unit
) {
    var lobbyCode by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LobbyCard(title = "Neue Lobby erstellen") {
            LobbyActionButton(
                text    = if (isLoading) "Wird erstellt..." else "Lobby erstellen",
                enabled = !isLoading, color = GreenButton, onClick = onCreateLobby
            )
        }
        LobbyCard(title = "Lobby beitreten") {
            OutlinedTextField(
                value         = lobbyCode,
                onValueChange = { if (it.length <= 5) lobbyCode = it.uppercase() },
                label         = { Text("5-stelliger Code", color = TextSecondary) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                colors        = lobbyTextFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LobbyActionButton(
                text    = if (isLoading) "Wird beigetreten..." else "Beitreten",
                enabled = !isLoading && lobbyCode.length == 5,
                color   = DarkButton,
                onClick = { onJoinLobby(lobbyCode) }
            )
        }
    }
}

@Composable
private fun InLobbyView(
    lobby: LobbyData,
    localUserId: String,
    isHost: Boolean,
    isLoading: Boolean,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
    onKickPlayer: (String) -> Unit,
    onStartRoleSelection: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        LobbyCard(title = lobby.name) {
            Text(
                text = lobby.id, color = AccentGold, fontSize = 36.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            Text(text = "${lobby.users.size}/6 Spieler", color = TextSecondary, fontSize = 12.sp)
        }

        LobbyCard(title = "Spieler") {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lobby.users.forEach { user ->
                    PlayerRow(
                        user           = user,
                        isLobbyHost    = user.id == lobby.hostId,
                        isLocalUser    = user.id == localUserId,
                        showKickButton = isHost && user.id != localUserId,
                        onKick         = { onKickPlayer(user.id) }
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LobbyActionButton(
                text = "Verlassen", enabled = !isLoading, color = AccentRed,
                onClick = onLeave, modifier = Modifier.weight(1f)
            )
            if (isHost) {
                LobbyActionButton(
                    text = "Lobby loeschen", enabled = !isLoading,
                    color = Color(0xFF7B1FA2), onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isHost) {
            val canProceed = lobby.users.size >= 3
            LobbyActionButton(
                text    = if (canProceed) "Weiter zur Rollenwahl →" else "Mind. 3 Spieler erforderlich",
                enabled = canProceed && !isLoading,
                color   = GreenButton,
                onClick = onStartRoleSelection
            )
        }
    }
}

@Composable
private fun PlayerRow(
    user: LobbyUserData,
    isLobbyHost: Boolean,
    isLocalUser: Boolean,
    showKickButton: Boolean,
    onKick: () -> Unit
) {
    var showKickConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Default.Person, contentDescription = null,
                tint = if (isLocalUser) AccentGold else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            if (isLobbyHost) {
                Icon(
                    imageVector = Icons.Default.Star, contentDescription = "Host",
                    tint = AccentGold, modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = user.name + if (isLocalUser) " (Du)" else "",
            color = TextPrimary,
            fontWeight = if (isLocalUser) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        if (showKickButton) {
            IconButton(onClick = { showKickConfirm = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = "Kicken",
                    tint = AccentRed, modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Box(modifier = Modifier
                .size(10.dp)
                .background(Color(0x44FFFFFF), CircleShape))
        }
    }

    if (showKickConfirm) {
        AlertDialog(
            onDismissRequest = { showKickConfirm = false },
            containerColor   = CardBg,
            title  = { Text("Spieler kicken?", color = TextPrimary) },
            text   = { Text("${user.name} entfernen?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onKick(); showKickConfirm = false }) {
                    Text("Kicken", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickConfirm = false }) {
                    Text("Abbrechen", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StatusBar(message: String) {
    val isError = message.startsWith("⚠️")
    Text(
        text = message,
        color = if (isError) Color(0xFFFF8A80) else Color(0xFF80CBC4),
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isError) Color(0x22FF0000) else Color(0x2200BCD4),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    )
}

@Composable
private fun LobbyCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))
        content()
    }
}

@Composable
private fun LobbyActionButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, color: Color = GreenButton
) {
    Button(
        onClick = onClick, enabled = enabled, shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
    ) {
        Text(text = text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun lobbyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White, unfocusedBorderColor = Color(0x66FFFFFF),
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = TextPrimary
)
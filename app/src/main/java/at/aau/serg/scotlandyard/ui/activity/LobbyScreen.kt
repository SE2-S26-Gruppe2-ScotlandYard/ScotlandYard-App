package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.model.LobbyUserData
import at.aau.serg.scotlandyard.ui.components.SectionHeader
import at.aau.serg.scotlandyard.ui.theme.AccentGlow
import at.aau.serg.scotlandyard.ui.theme.AccentTeal
import at.aau.serg.scotlandyard.ui.theme.DETECTIVE_COLORS
import at.aau.serg.scotlandyard.ui.theme.SidebarBg
import at.aau.serg.scotlandyard.ui.theme.SidebarBorder
import at.aau.serg.scotlandyard.ui.theme.TextMuted
import at.aau.serg.scotlandyard.ui.theme.TextPrimary
import at.aau.serg.scotlandyard.viewmodel.AuthViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModelFactory
import com.example.scotlandyard.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccentRed       = Color(0xFFE53935)
private val AccentPurple    = Color(0xFF7B1FA2)
private val AccentGold      = Color(0xFFFFD700)
private val StartButtonText = Color(0xFF071A0E)

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

    val statusLog = remember { mutableStateListOf<String>() }
    LaunchedEffect(statusMessage) {
        if (statusMessage.isNotBlank()) {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            statusLog.add(0, "[$time] $statusMessage")
            if (statusLog.size > 30) statusLog.removeLast()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToRoleSelection.collect {
            onNavigateToRoleSelection(viewModel)
        }
    }

    val titleText = if (currentLobby == null) {
        stringResource(R.string.title_lobby_selection)
    } else {
        val host = currentLobby!!.users.find { it.id == currentLobby!!.hostId }
        if (host != null) stringResource(R.string.title_personalised_lobby, host.name.uppercase())
        else stringResource(R.string.title_lobby)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentLobby == null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.button_back),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(AccentGlow.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (currentLobby != null) Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = titleText,
                    fontSize = if (currentLobby == null) 30.sp else 25.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = if (currentLobby == null) 2.sp else 0.5.sp,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(AccentGlow.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                HeaderConnectedBadge(isConnected = isConnected)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, bottom = 24.dp)
            ) {
                if (currentLobby == null) {
                    LobbyBrowserView(
                        isLoading     = isLoading,
                        statusLog     = statusLog,
                        userName      = userName,
                        onCreateLobby = { viewModel.createLobby() },
                        onJoinLobby   = { code -> viewModel.joinLobby(code) }
                    )
                } else {
                    InLobbyView(
                        lobby                = currentLobby!!,
                        localUserId          = viewModel.userId,
                        isHost               = viewModel.isLocalUserHost(),
                        isLoading            = isLoading,
                        statusLog            = statusLog,
                        userName             = userName,
                        onLeave              = { viewModel.leaveLobby() },
                        onDelete             = { viewModel.deleteLobby() },
                        onKickPlayer         = { targetId -> viewModel.kickPlayer(targetId) },
                        onStartRoleSelection = { onProceedToRoles(viewModel) }
                    )
                }
            }
        }
    }
}

// ── Header connected badge ────────────────────────────────────────────────────

@Composable
private fun HeaderConnectedBadge(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(SidebarBg.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (isConnected) AccentGlow else AccentRed, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isConnected) stringResource(R.string.status_connected)
                   else stringResource(R.string.status_disconnected),
            color = if (isConnected) AccentGlow else AccentRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Activity console (scrolls with content) ──────────────────────────────────

@Composable
private fun LobbyConsole(messages: List<String>, userName: String) {
    val scrollState = rememberScrollState()
    LaunchedEffect(messages.size) { scrollState.animateScrollTo(0) }

    Column(
        modifier = Modifier.width(172.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SidebarBg.copy(alpha = 0.70f))
                .border(1.dp, SidebarBorder.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
        ) {
            if (messages.isEmpty()) {
                Text(
                    text = "No activity yet",
                    color = TextMuted.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    messages.forEach { msg ->
                        val isError = msg.contains("⚠️")
                        Text(
                            text = msg,
                            color = if (isError) Color(0xFFFF8A80) else AccentGlow.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = "nickname: $userName",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 10.sp,
            maxLines = 1,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}

// ── Lobby browser (no lobby yet) ──────────────────────────────────────────────

@Composable
private fun LobbyBrowserView(
    isLoading: Boolean,
    statusLog: List<String>,
    userName: String,
    onCreateLobby: () -> Unit,
    onJoinLobby: (String) -> Unit
) {
    var lobbyCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(AccentTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .border(1.dp, AccentGlow.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = AccentGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.lobby_button_create_lobby),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Start a new game room for your friends",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AccentGlow.copy(alpha = 0.25f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                LobbyConsole(messages = statusLog, userName = userName)
            }
            LobbyPrimaryButton(
                text    = if (isLoading) stringResource(R.string.lobby_status_creating)
                          else stringResource(R.string.lobby_button_create_lobby),
                enabled = !isLoading,
                onClick = onCreateLobby
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(
                icon     = Icons.Default.Search,
                title    = "Join Lobby",
                subtitle = "Enter a 5-character code to join",
                bottomSpacing = 12.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SidebarBg)
                    .border(1.dp, SidebarBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = lobbyCode,
                    onValueChange = { if (it.length <= 5) lobbyCode = it.uppercase() },
                    label         = { Text(stringResource(R.string.lobby_code_description), color = TextMuted) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    textStyle = TextStyle(
                        fontFamily    = FontFamily.Monospace,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 22.sp,
                        letterSpacing = 6.sp,
                        textAlign     = TextAlign.Center,
                        color         = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AccentGlow,
                        unfocusedBorderColor = SidebarBorder,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = AccentGlow,
                        focusedLabelColor    = AccentGlow,
                        unfocusedLabelColor  = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                LobbyPrimaryButton(
                    text    = if (isLoading) stringResource(R.string.lobby_status_joining)
                              else stringResource(R.string.lobby_button_join_lobby),
                    enabled = !isLoading && lobbyCode.length == 5,
                    onClick = { onJoinLobby(lobbyCode) }
                )
            }
        }
    }
}

// ── In-lobby view ─────────────────────────────────────────────────────────────

@Composable
private fun InLobbyView(
    lobby: LobbyData,
    localUserId: String,
    isHost: Boolean,
    isLoading: Boolean,
    statusLog: List<String>,
    userName: String,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
    onKickPlayer: (String) -> Unit,
    onStartRoleSelection: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            LobbyCodeCard(lobby = lobby, modifier = Modifier.weight(1f))
            LobbyConsole(messages = statusLog, userName = userName)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(
                icon     = Icons.Default.Groups,
                title    = stringResource(R.string.title_player),
                subtitle = stringResource(R.string.lobby_status_players, lobby.users.size),
                bottomSpacing = 12.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SidebarBg)
                    .border(1.dp, SidebarBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                lobby.users.forEachIndexed { index, user ->
                    PlayerRow(
                        user           = user,
                        playerColor    = DETECTIVE_COLORS.getOrElse(index) { TextMuted },
                        isLobbyHost    = user.id == lobby.hostId,
                        isLocalUser    = user.id == localUserId,
                        showKickButton = isHost && user.id != localUserId,
                        onKick         = { onKickPlayer(user.id) }
                    )
                }
                repeat(maxOf(0, 6 - lobby.users.size)) { EmptyPlayerSlot() }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LobbyDestructiveButton(
                text     = stringResource(R.string.button_leave),
                enabled  = !isLoading,
                color    = AccentRed,
                onClick  = onLeave,
                modifier = Modifier.weight(1f)
            )
            if (isHost) {
                LobbyDestructiveButton(
                    text     = stringResource(R.string.lobby_button_delete),
                    enabled  = !isLoading,
                    color    = AccentPurple,
                    onClick  = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isHost) {
            val canProceed = lobby.users.size >= 3
            LobbyStartButton(
                text    = if (canProceed) stringResource(R.string.lobby_button_continue_role_selection)
                          else stringResource(R.string.lobby_status_min_players),
                enabled = canProceed && !isLoading,
                onClick = onStartRoleSelection
            )
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun LobbyCodeCard(lobby: LobbyData, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SidebarBg)
            .border(1.dp, AccentGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text          = "LOBBY CODE",
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp,
            color         = TextMuted
        )
        Text(
            text          = lobby.id,
            fontSize      = 42.sp,
            fontWeight    = FontWeight.ExtraBold,
            fontFamily    = FontFamily.Monospace,
            letterSpacing = 8.sp,
            color         = AccentGold,
            textAlign     = TextAlign.Center,
            modifier      = Modifier.fillMaxWidth()
        )
        HorizontalDivider(
            color     = AccentGlow.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier  = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        Text(
            text      = "Share this code with your friends",
            fontSize  = 12.sp,
            color     = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlayerRow(
    user: LobbyUserData,
    playerColor: Color,
    isLobbyHost: Boolean,
    isLocalUser: Boolean,
    showKickButton: Boolean,
    onKick: () -> Unit
) {
    var showKickConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(playerColor.copy(alpha = 0.08f))
            .border(1.dp, playerColor.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(playerColor, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text       = user.name + if (isLocalUser) stringResource(R.string.lobby_indicator_you) else "",
            color      = if (isLocalUser) TextPrimary else TextPrimary.copy(alpha = 0.85f),
            fontWeight = if (isLocalUser) FontWeight.SemiBold else FontWeight.Normal,
            fontSize   = 14.sp,
            modifier   = Modifier.weight(1f)
        )
        if (isLobbyHost) {
            Icon(
                imageVector        = Icons.Default.Star,
                contentDescription = stringResource(R.string.description_host),
                tint               = AccentGold,
                modifier           = Modifier.size(15.dp)
            )
            if (showKickButton) Spacer(modifier = Modifier.width(4.dp))
        }
        if (showKickButton) {
            IconButton(onClick = { showKickConfirm = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = stringResource(R.string.button_kick),
                    tint               = AccentRed.copy(alpha = 0.8f),
                    modifier           = Modifier.size(15.dp)
                )
            }
        }
    }

    if (showKickConfirm) {
        AlertDialog(
            onDismissRequest = { showKickConfirm = false },
            containerColor   = SidebarBg,
            shape            = RoundedCornerShape(16.dp),
            title            = { Text(stringResource(R.string.kick_player_confirm), color = TextPrimary) },
            text             = { Text(stringResource(R.string.lobby_button_remove_player, user.name), color = TextMuted) },
            confirmButton    = {
                TextButton(onClick = { onKick(); showKickConfirm = false }) {
                    Text(stringResource(R.string.button_kick), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKickConfirm = false }) {
                    Text(stringResource(R.string.button_cancel), color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun EmptyPlayerSlot() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, SidebarBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .border(1.dp, SidebarBorder, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = "Waiting…", color = TextMuted.copy(alpha = 0.4f), fontSize = 13.sp)
    }
}

// ── Buttons ───────────────────────────────────────────────────────────────────

@Composable
private fun LobbyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = AccentTeal,
            disabledContainerColor = AccentTeal.copy(alpha = 0.25f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = if (enabled) AccentGlow.copy(alpha = 0.55f) else SidebarBorder,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Text(
            text       = text,
            color      = if (enabled) AccentGlow else TextMuted,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LobbyStartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = AccentGlow,
            disabledContainerColor = SidebarBg.copy(alpha = 0.55f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = if (enabled) Color.Transparent else SidebarBorder,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(
            text       = text,
            color      = if (enabled) StartButtonText else TextMuted.copy(alpha = 0.65f),
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LobbyDestructiveButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = color.copy(alpha = 0.38f),
            disabledContainerColor = color.copy(alpha = 0.10f)
        ),
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = if (enabled) color.copy(alpha = 0.75f) else SidebarBorder,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Text(
            text       = text,
            color      = if (enabled) Color.White else TextMuted,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

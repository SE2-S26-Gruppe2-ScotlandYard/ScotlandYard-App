package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.model.LobbyUserData
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import com.example.scotlandyard.R

private val MrXPink = Color(0xFFEF9A9A)

private fun LobbyData.playersInRole(role: String): List<LobbyUserData> =
    users.filter { (selectedRoles[it.id] ?: "NONE") == role }

private fun LobbyData.allRolesAssigned(): Boolean =
    users.isNotEmpty() &&
    users.all { (selectedRoles[it.id] ?: "NONE") != "NONE" } &&
    selectedRoles.values.contains("MRX")

@Composable
fun RoleSelectionScreen(
    viewModel: LobbyViewModel,
    lobby: LobbyData,
    isConnected: Boolean = true,
    onBackClick: () -> Unit,
    onGameStart: () -> Unit
) {
    RoleSelectionContent(
        localUserId = viewModel.userId,
        isHost = viewModel.isLocalUserHost(),
        isConnected = isConnected,
        lobby = lobby,
        onRoleSelect = { role -> viewModel.setRole(viewModel.userId, role) },
        onBackClick = {
            viewModel.goBackToLobby()
            onBackClick()
        },
        onGameStart = onGameStart
    )
}

@Composable
fun RoleSelectionContent(
    localUserId: String,
    isHost: Boolean,
    isConnected: Boolean = true,
    lobby: LobbyData,
    onRoleSelect: (String) -> Unit,
    onBackClick: () -> Unit,
    onGameStart: () -> Unit
) {
    val myRole = lobby.selectedRoles[localUserId] ?: "NONE"
    val mrXTaken = lobby.selectedRoles.values.contains("MRX")
    val mrXSelectable = !mrXTaken || myRole == "MRX"
    val allRolesSet = lobby.allRolesAssigned()

    val detectiveWeight by animateFloatAsState(
        targetValue = when (myRole) {
            "DETECTIVE" -> 0.58f
            "MRX" -> 0.42f
            else -> 0.5f
        },
        animationSpec = tween(durationMillis = 400),
        label = "detectiveWeight"
    )

    val detectiveDimAlpha by animateFloatAsState(
        targetValue = if (myRole == "MRX") 0.55f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "detectiveDim"
    )
    val mrxDimAlpha by animateFloatAsState(
        targetValue = when {
            !mrXSelectable -> 0.70f
            myRole == "DETECTIVE" -> 0.55f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 400),
        label = "mrxDim"
    )

    // Text alpha for the non-selected side (player names are excluded from fading)
    val detectiveTextAlpha = 1f - detectiveDimAlpha * 0.6f
    val mrxTextAlpha       = 1f - mrxDimAlpha * 0.6f

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {

            // === Detective-Hälfte ===
            Box(
                modifier = Modifier
                    .weight(detectiveWeight)
                    .fillMaxHeight()
                    .clickable(enabled = isConnected) { onRoleSelect("DETECTIVE") }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chooserole_bg_detective_side),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dimm-Gradient: außen (links) dunkel → zur Mitte (rechts) transparent
                if (detectiveDimAlpha > 0f) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0f   to Color(0f, 0f, 0f, detectiveDimAlpha),
                                0.7f to Color(0f, 0f, 0f, detectiveDimAlpha),
                                1f   to Color.Transparent
                            )
                        )
                    )
                }
                RoleHalfContent(
                    title = stringResource(R.string.role_selection_title_detective),
                    subtitle = stringResource(R.string.role_selection_description_detective),
                    isSelected = myRole == "DETECTIVE",
                    isDisabled = false,
                    players = lobby.playersInRole("DETECTIVE"),
                    hostId = lobby.hostId,
                    isMrXSide = false,
                    textAlpha = detectiveTextAlpha
                )
            }

            // === MrX-Hälfte ===
            Box(
                modifier = Modifier
                    .weight(1f - detectiveWeight)
                    .fillMaxHeight()
                    .clickable(enabled = mrXSelectable && isConnected) { onRoleSelect("MRX") }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chooserole_bg_mrx_side),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dimm-Gradient: zur Mitte (links) transparent → außen (rechts) dunkel
                if (mrxDimAlpha > 0f) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                0f   to Color.Transparent,
                                0.3f to Color(0f, 0f, 0f, mrxDimAlpha),
                                1f   to Color(0f, 0f, 0f, mrxDimAlpha)
                            )
                        )
                    )
                }
                RoleHalfContent(
                    title = stringResource(R.string.role_selection_title_mrx),
                    subtitle = stringResource(R.string.role_selection_description_mrx),
                    isSelected = myRole == "MRX",
                    isDisabled = !mrXSelectable,
                    players = lobby.playersInRole("MRX"),
                    hostId = lobby.hostId,
                    isMrXSide = true,
                    textAlpha = mrxTextAlpha
                )
            }
        }

        // Center gradient stripe that bleeds into both halves, follows the animated split
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        (detectiveWeight - 0.18f).coerceAtLeast(0f) to Color.Transparent,
                        (detectiveWeight - 0.04f)                   to Color(0x88000000),
                        detectiveWeight                             to Color(0xBB000000),
                        (detectiveWeight + 0.04f)                   to Color(0x88000000),
                        (detectiveWeight + 0.18f).coerceAtMost(1f) to Color.Transparent
                    )
                )
            )
        )

        RoleSelectionTopBar(
            isHost = isHost,
            allRolesSet = allRolesSet,
            onBackClick = onBackClick,
            onGameStart = onGameStart
        )

        // Disconnected-Banner
        if (!isConnected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0xCC991111))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.status_role_screen_disconnected),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Players who haven't selected a role yet, shown at top-center
        val pendingPlayers = lobby.users.filter { (lobby.selectedRoles[it.id] ?: "NONE") == "NONE" }
        if (pendingPlayers.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.status_no_selection_header),
                    color = Color(0xFF999999),
                    fontSize = 11.sp
                )
                Text(
                    text = pendingPlayers.joinToString(" · ") { it.name },
                    color = TextLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RoleHalfContent(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    players: List<LobbyUserData>,
    hostId: String,
    isMrXSide: Boolean,
    textAlpha: Float = 1f
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = if (isSelected) 34.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color(0xFFBBBBBB).copy(alpha = textAlpha),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        PlayerList(
            players = players,
            hostId = hostId,
            playerColor = if (isMrXSide) MrXPink else DetectiveBlue
        )
        RoleStatusLabel(isDisabled = isDisabled, isSelected = isSelected, textAlpha = textAlpha)
    }
}

@Composable
private fun RoleSelectionTopBar(
    isHost: Boolean,
    allRolesSet: Boolean,
    onBackClick: () -> Unit,
    onGameStart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (isHost) { BackToLobbyButton(onBackClick) }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (isHost) {
                StartGameButton(allRolesSet, onGameStart)
            } else {
                Text(
                    text = stringResource(R.string.status_waiting_for_host),
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BackToLobbyButton(onBackClick: () -> Unit) {
    val enabled = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    TextButton(
        onClick = {
            if (enabled.value) {
                enabled.value = false
                onBackClick()
                scope.launch { delay(500.milliseconds); enabled.value = true }
            }
        },
        enabled = enabled.value,
        colors = ButtonDefaults.textButtonColors(contentColor = TextLight),
        modifier = Modifier.wrapContentSize()
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.button_back),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            stringResource(R.string.button_back_to_lobby),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StartGameButton(allRolesSet: Boolean, onGameStart: () -> Unit) {
    val enabled = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    TextButton(
        onClick = {
            if (enabled.value) {
                enabled.value = false
                onGameStart()
                scope.launch { delay(500.milliseconds); enabled.value = true }
            }
        },
        enabled = allRolesSet && enabled.value,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.White,
            disabledContentColor = Color(0x44FFFFFF)
        ),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = stringResource(R.string.button_start_position),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = if (allRolesSet) TextStyle(
                shadow = Shadow(
                    color = AccentGlow,
                    offset = Offset.Zero,
                    blurRadius = 20f
                )
            ) else TextStyle.Default
        )
    }
}

@Composable
private fun PlayerList(
    players: List<LobbyUserData>,
    hostId: String,
    playerColor: Color
) {
    if (players.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEachIndexed { index, user ->
            Text(
                text = "● ${user.name}",
                color = if (user.id == hostId) RoleGold else playerColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (index < players.size - 1) Spacer(modifier = Modifier.width(12.dp))
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun RoleStatusLabel(isDisabled: Boolean, isSelected: Boolean, textAlpha: Float = 1f) {
    when {
        isDisabled  -> Text(
            text = stringResource(R.string.status_already_taken),
            color = RoleRed.copy(alpha = textAlpha),
            fontSize = 12.sp
        )
        !isSelected -> Text(
            text = stringResource(R.string.button_click_to_select),
            color = Color(0xFFAAAAAA).copy(alpha = textAlpha),
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true, device = "spec:width=800dp,height=400dp,dpi=480,orientation=landscape")
@Composable
fun RoleSelectionPreview() {
    val dummyLobby = LobbyData(
        id = "test-id",
        name = "Preview Lobby",
        hostId = "1",
        isStarted = false,
        readyStatus = emptyMap(),
        users = listOf(
            LobbyUserData("1", "Alice"),
            LobbyUserData("2", "Bob"),
            LobbyUserData("3", "Max")
        ),
        selectedRoles = mutableMapOf(
            "1" to "DETECTIVE",
            "2" to "DETECTIVE",
            "3" to "NONE"
        )
    )
    RoleSelectionContent(
        localUserId = "3",
        isHost = false,
        lobby = dummyLobby,
        onRoleSelect = {},
        onBackClick = {},
        onGameStart = {}
    )
}

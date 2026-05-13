package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.model.LobbyData
import at.aau.serg.scotlandyard.model.LobbyUserData
import at.aau.serg.scotlandyard.viewmodel.LobbyViewModel
import com.example.scotlandyard.R

/**
 * Rollenauswahl-Screen
 *
 * Jeder Spieler kann seine Rolle selbst wählen (MR X oder Detektiv).
 * Mr. X kann nur einmal vergeben werden.
 * Der Host kann das Spiel starten wenn alle Rollen vergeben sind.
 */
@Composable
fun RoleSelectionScreen(
    viewModel: LobbyViewModel,
    lobby: LobbyData,
    onBackClick: () -> Unit,
    onGameStart: () -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.navigateToLobby.collect {
            onBackClick() // Führt die eigentliche Navigation aus (z.B. popBackStack)
        }
    }

    RoleSelectionContent(
        localUserId = viewModel.userId,
        isHost = viewModel.isLocalUserHost(),
        lobby = lobby,
        onRoleSelect = { role -> viewModel.setRole(viewModel.userId, role) },
        onBackClick = { viewModel.goBackToLobby() }, // Klick löst jetzt Backend-Call aus
        onGameStart = onGameStart
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionContent(
    localUserId: String,
    isHost: Boolean,
    lobby: LobbyData,
    onRoleSelect: (String) -> Unit,
    onBackClick: () -> Unit,
    onGameStart: () -> Unit
) {
    val myRole = lobby.selectedRoles[localUserId] ?: "NONE"
    val mrXTaken = lobby.selectedRoles.values.contains("MRX")

    val allRolesSet = lobby.users.isNotEmpty() && lobby.users.all { user ->
        (lobby.selectedRoles[user.id] ?: "NONE") != "NONE"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.chooserole_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x44000000))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                // Der "Back to Lobby"-Button ist nur für den Host sichtbar.
                if (isHost) {
                    TextButton(
                        onClick = onBackClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Lobby", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                if (isHost) {
                    TextButton(
                        onClick = onGameStart,
                        enabled = allRolesSet,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text("Start Game", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                    }
                } else {
                    Text(
                        text = "Waiting for host...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }

        Text(
            text = "Choose your side:",
            fontSize = 36.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoleSelectionColumn(
                modifier = Modifier.weight(1f),
                title = "Play as Detective",
                subtitle = "Hunt Mr. X together",
                backgroundColor = Color(0xFF142B20),
                isSelected = myRole == "DETECTIVE",
                isDisabled = false,
                players = lobby.users.filter { (lobby.selectedRoles[it.id] ?: "NONE") == "DETECTIVE" },
                hostId = lobby.hostId,
                onClick = { onRoleSelect("DETECTIVE") }
            )

            RoleSelectionColumn(
                modifier = Modifier.weight(1f),
                title = "Play as Mr. X",
                subtitle = "Outsmart the detectives",
                backgroundColor = if (mrXTaken) Color(0xFF1D1D1D) else Color(0xFF142B20),
                isSelected = myRole == "MRX",
                isDisabled = mrXTaken && myRole != "MRX",
                players = lobby.users.filter { (lobby.selectedRoles[it.id] ?: "NONE") == "MRX" },
                hostId = lobby.hostId,
                onClick = {
                    if (!mrXTaken || myRole == "MRX") {
                        onRoleSelect("MRX")
                    }
                }
            )
        }
    }
}

@Composable
private fun RoleSelectionColumn(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    isSelected: Boolean,
    isDisabled: Boolean,
    players: List<LobbyUserData>,
    hostId: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = !isDisabled,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0x55FFFFFF)),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = Color.White,
                disabledContainerColor = backgroundColor,
                disabledContentColor = Color(0x44FFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (players.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                players.forEachIndexed { index, user ->
                    val isHost = user.id == hostId
                    Text(
                        text = "● ${user.name}",
                        color = if (isHost) Color(0xFF4CAF50) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    if (index < players.size - 1) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (isDisabled) {
            Text(
                text = "Already taken",
                color = Color(0x88FFFFFF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif
            )
        } else if (isSelected) {
            Text(
                text = "✓ Selected",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        } else {
            Text(
                text = "Click to select",
                color = Color(0x88FFFFFF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif
            )
        }
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
            LobbyUserData("1", "h"),
            LobbyUserData("2", "i"),
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
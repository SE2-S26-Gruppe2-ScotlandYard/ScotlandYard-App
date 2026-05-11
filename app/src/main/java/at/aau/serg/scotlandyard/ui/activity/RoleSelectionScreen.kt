package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val localUserId = viewModel.userId
    val isHost = viewModel.isLocalUserHost()

    val myRole = lobby.selectedRoles[localUserId] ?: "NONE"
    val mrXTaken = lobby.selectedRoles.values.contains("MRX")
    val mrXOwner = lobby.selectedRoles.entries.find { it.value == "MRX" }?.key
    val allRolesSet = lobby.users.all { user ->
        (lobby.selectedRoles[user.id] ?: "NONE") != "NONE"
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Hintergrundbild
        Image(
            painter = painterResource(id = R.drawable.chooserole_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dunkler Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
        )

        // Hauptinhalt
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Titel
            Text(
                text = "Choose your side:",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Zwei Karten nebeneinander
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Detektiv Karte ─────────────────────────────────────────
                RoleCard(
                    modifier = Modifier.weight(1f),
                    role = "DETECTIVE",
                    title = "Play as Detective",
                    subtitle = "Hunt Mr. X together",
                    backgroundColor = Color(0xFF0D2A1A),
                    borderColor = if (myRole == "DETECTIVE") Color(0xFF4CAF50) else Color(0x44FFFFFF),
                    isSelected = myRole == "DETECTIVE",
                    isDisabled = false,
                    playerNames = lobby.users
                        .filter { (lobby.selectedRoles[it.id] ?: "NONE") == "DETECTIVE" }
                        .map { it.name },
                    onClick = {
                        viewModel.setRole(localUserId, "DETECTIVE")
                    }

                )

                // ── Mr. X Karte ────────────────────────────────────────────
                RoleCard(
                    modifier = Modifier.weight(1f),
                    role = "MRX",
                    title = "Play as Mr. X",
                    subtitle = "Outsmart the detectives",
                    backgroundColor = Color(0xFF1A0D0D),
                    borderColor = when {
                        myRole == "MRX" -> Color(0xFFE53935)
                        mrXTaken -> Color(0x22FFFFFF)
                        else -> Color(0x44FFFFFF)
                    },
                    isSelected = myRole == "MRX",
                    isDisabled = mrXTaken && myRole != "MRX",
                    playerNames = if (mrXOwner != null) {
                        val name = lobby.users.find { it.id == mrXOwner }?.name ?: ""
                        if (name.isNotBlank()) listOf(name) else emptyList()
                    } else emptyList(),
                    onClick = {
                        if (!mrXTaken || myRole == "MRX") {
                            viewModel.setRole(localUserId, "MRX")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spieler ohne Rolle anzeigen
            val playersWithoutRole = lobby.users.filter {
                (lobby.selectedRoles[it.id] ?: "NONE") == "NONE"
            }
            if (playersWithoutRole.isNotEmpty()) {
                Text(
                    text = "Noch ohne Rolle: ${playersWithoutRole.joinToString(", ") { it.name }}",
                    color = Color(0xFFFFCC80),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Zurück Button
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("← Zurück zur Lobby", color = Color.White)
            }

            // Spiel starten (nur Host, nur wenn alle Rollen vergeben)
            if (isHost) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGameStart,
                    enabled = allRolesSet,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A3A),
                        disabledContainerColor = Color(0x441A4A3A)
                    ),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (allRolesSet) "Spiel starten ▶" else "Alle müssen eine Rolle wählen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Rollen-Karte ──────────────────────────────────────────────────────────────

@Composable
private fun RoleCard(
    modifier: Modifier = Modifier,
    role: String,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    borderColor: Color,
    isSelected: Boolean,
    isDisabled: Boolean,
    playerNames: List<String>,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp

    Button(
        onClick = onClick,
        enabled = !isDisabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(220.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Rolle Icon
                Text(
                    text = if (role == "MRX") "🎩" else "🔍",
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    color = Color(0xAAFFFFFF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Spieler die diese Rolle haben
            if (playerNames.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    playerNames.forEach { name ->
                        Text(
                            text = "● $name",
                            color = if (role == "MRX") Color(0xFFFF8A80) else Color(0xFF80CBC4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (isDisabled) {
                Text(
                    text = "Bereits vergeben",
                    color = Color(0x88FFFFFF),
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = if (isSelected) "✓ Ausgewählt" else "Klicken zum Wählen",
                    color = if (isSelected) Color(0xFF80CBC4) else Color(0x88FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}
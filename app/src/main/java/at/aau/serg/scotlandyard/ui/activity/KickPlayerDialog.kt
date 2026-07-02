package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import at.aau.serg.scotlandyard.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.theme.*

@Composable
fun KickPlayerDialog(
    playerNames: Map<String, String>,
    hostId: String,
    mrXId: String?,
    disconnectedPlayers: Set<String> = emptySet(),
    onKick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteGame: () -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = SidebarBg,
            shape = RoundedCornerShape(16.dp),
            title = { Text(stringResource(R.string.title_delete_game), color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    stringResource(R.string.description_delete_game),
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoleRed)
                ) {
                    Text(stringResource(R.string.button_confirm), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.button_cancel), color = TextMuted)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBg,
        shape = RoundedCornerShape(16.dp),
        title = { Text(stringResource(R.string.title_menu), color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.kick_player_confirm), color = TextMuted, fontSize = 13.sp)
                playerNames.forEach { (playerId, name) ->
                    if (playerId != hostId) {
                        val isDisconnected = playerId in disconnectedPlayers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    SidebarBorder.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, SidebarBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name +
                                        (if (playerId == mrXId) "  " + stringResource(R.string.status_mrx) else "") +
                                        (if (isDisconnected) "  " + stringResource(R.string.status_disconnected) else ""),
                                color = if (isDisconnected) RoleRed else TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { onKick(playerId) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoleRed.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(stringResource(R.string.button_kick), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.title_settings), color = Color.White)
                }
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoleRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.title_delete_game), color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_close), color = TextMuted)
            }
        }
    )
}
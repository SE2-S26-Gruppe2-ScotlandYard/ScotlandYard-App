package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.theme.*

@Composable
fun KickPlayerDialog(
    playerNames: Map<String, String>,
    hostId: String,
    mrXId: String?,
    onKick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SidebarBg,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Host Menü", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Spieler kicken:", color = TextMuted, fontSize = 13.sp)
                playerNames.forEach { (playerId, name) ->
                    if (playerId != hostId) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SidebarBorder.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, SidebarBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name + if (playerId == mrXId) " (Mr. X)" else "",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { onKick(playerId) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoleRed.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Kick", color = Color.White, fontSize = 12.sp)
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
                    Text("Einstellungen öffnen", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = TextMuted)
            }
        }
    )
}
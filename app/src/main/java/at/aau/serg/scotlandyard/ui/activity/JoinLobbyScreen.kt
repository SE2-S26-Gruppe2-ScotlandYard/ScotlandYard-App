package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.Callbacks
import at.aau.serg.scotlandyard.network.MyStomp
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

@Composable
fun JoinLobbyScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    var lobbyCode by remember { mutableStateOf("") }
    var serverResponse by remember { mutableStateOf("No response yet") }

    val stomp = remember {
        MyStomp(object : Callbacks {
            override fun onResponse(res: String) {
                serverResponse = res
            }
        })
    }

    DisposableEffect(Unit) {
        stomp.connect()
        stomp.subscribeLobby()
        onDispose { }
    }

    BaseScreen(onBackClick = onBackClick) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Join Lobby",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )

            Text(
                text = "Player: $userName",
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = lobbyCode,
                onValueChange = { lobbyCode = it },
                label = { Text("Lobby Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            Button(
                onClick = {
                    stomp.joinLobby(
                        lobbyId = lobbyCode,
                        userId = userName,
                        userName = userName,
                        password = ""
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text("Join Lobby")
            }

            Text(
                text = "Server response:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Text(
                text = serverResponse,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun JoinLobbyScreenPreview() {
    ScotlandYardTheme {
        JoinLobbyScreen(
            userName = "Stefan",
            onBackClick = {}
        )
    }
}
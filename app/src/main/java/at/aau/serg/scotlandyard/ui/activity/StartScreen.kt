package at.aau.serg.websocketbrokerdemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import at.aau.serg.websocketbrokerdemo.ui.theme.MyApplicationTheme
import com.example.myapplication.R

@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    onRules: () -> Unit
) {
    // Box stapelt alles übereinander: Hintergrundbild → Inhalt
    Box(modifier = Modifier.fillMaxSize()) {

        // 1) Hintergrundbild — füllt den ganzen Screen
        Image(
            painter = painterResource(id = R.drawable.startscreen_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2) Leichter dunkler Overlay damit Text lesbar bleibt
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x40000000)) // 25% schwarz
        )

        // 3) Zahnrad-Icon oben rechts
        IconButton(
            onClick = { /* TODO: Settings öffnen */ },
            modifier = Modifier
                .align(Alignment.TopEnd)     // oben rechts positionieren
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFFAAAAAA),    // dezentes Grau
                modifier = Modifier.size(28.dp)
            )
        }

        // 4) Hauptinhalt: Titel links + Buttons rechts, vertikal zentriert
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp), // Seitenabstand
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // === LINKE SEITE: Titel mit Linien ===
            Column(
                modifier = Modifier.weight(1f) // nimmt die linke Hälfte ein
            ) {
                // Linie über dem Titel
                Divider(
                    color = Color(0xAAFFFFFF), // halbtransparentes Weiß
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Großer Titel "SCOTLAND YARD"
                Text(
                    text = "SCOTLAND\nYARD",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,   // Serifenschrift wie im Bild
                    color = Color.White,
                    lineHeight = 60.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Linie unter dem Titel
                Divider(
                    color = Color(0xAAFFFFFF),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Untertitel
                Text(
                    text = "Hunt Mr. X ecofriendly",
                    fontSize = 16.sp,
                    color = Color(0xFFCCCCCC) // helles Grau
                )
            }

            // === RECHTE SEITE: Buttons ===
            Column(
                modifier = Modifier.width(240.dp).padding(top = 150.dp), // feste Breite für die Buttons
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Start Game" Button — teal/dunkelgrün mit hellem Rand
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xAAFFFFFF),         // heller Rand
                            shape = RoundedCornerShape(6.dp)
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A3A)     // teal/dunkelgrün
                    )
                ) {
                    Text(
                        text = "Start Game",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Rules" Button — dunkel mit hellem Rand
                Button(
                    onClick = onRules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xAAFFFFFF),         // heller Rand
                            shape = RoundedCornerShape(6.dp)
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A)     // fast schwarz
                    )
                ) {
                    Text(
                        text = "Rules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun StartScreenPreview() {
    MyApplicationTheme {
        StartScreen(onStartGame = {}, onRules = {})
    }
}

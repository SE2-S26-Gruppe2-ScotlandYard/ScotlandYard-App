package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.components.AppActionButton
import at.aau.serg.scotlandyard.ui.components.AppDarkActionButton
import at.aau.serg.scotlandyard.ui.components.AppSettingsButton
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
    startGameButtonText: String = "Start Game",
    rulesButtonText: String = "Rules"
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
        AppSettingsButton(
            onClick = onSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)     // oben rechts positionieren
                .padding(16.dp)
        )

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
                HorizontalDivider(
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
                HorizontalDivider(
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
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxSize(), // Buttons verteilung über ganze Höhe
                verticalArrangement = Arrangement.Center, // Zentriert die Buttons vertikal
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Start Game" Button — teal/dunkelgrün mit hellem Rand
                AppActionButton(
                    text = startGameButtonText,
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "Rules" Button — dunkel mit hellem Rand
                AppDarkActionButton(
                    text = rulesButtonText,
                    onClick = onRules,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun StartScreenPreview() {
    ScotlandYardTheme {
        StartScreen(onStartGame = {}, onRules = {}, onSettings = {})
    }
}

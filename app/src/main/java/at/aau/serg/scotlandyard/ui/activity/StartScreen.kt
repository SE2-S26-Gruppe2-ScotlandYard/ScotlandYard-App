package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.components.AppActionButton
import at.aau.serg.scotlandyard.ui.components.AppDarkActionButton
import at.aau.serg.scotlandyard.ui.components.AppSettingsButton
import at.aau.serg.scotlandyard.ui.theme.*
import com.example.scotlandyard.R

@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
    startGameButtonText: String = stringResource(R.string.button_start_game),
    rulesButtonText: String = stringResource(R.string.title_rules)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "startfx")

    // fog center drifts as fraction of screen width (0.2 → 0.8)
    val fog1CX by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "f1x"
    )
    val fog1A by infiniteTransition.animateFloat(
        initialValue = 0.06f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "f1a"
    )
    val fog2CX by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(11000), RepeatMode.Reverse), label = "f2x"
    )
    val fog2A by infiniteTransition.animateFloat(
        initialValue = 0.08f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(8500), RepeatMode.Reverse), label = "f2a"
    )
    val titleGlow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "glow"
    )
    val subtitleGlow by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "sglow"
    )

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

        // Nebel-Blobs — center driftet innerhalb des Screens, kein Clipping
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1A1030).copy(alpha = fog1A), Color.Transparent),
                        center = Offset(w * fog1CX, h * 0.5f),
                        radius = w * 0.7f
                    )
                )
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0A1525).copy(alpha = fog2A), Color.Transparent),
                        center = Offset(w * fog2CX, h * 0.55f),
                        radius = w * 0.8f
                    )
                )
            )
        }

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
                    color = ButtonBorder, // halbtransparentes Weiß
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Großer Titel "SCOTLAND YARD"
                Text(
                    text = stringResource(R.string.title_main_scotland_yard),
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color.White,
                        lineHeight = 60.sp,
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.55f),
                            offset = Offset.Zero,
                            blurRadius = titleGlow
                        )
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Linie unter dem Titel
                HorizontalDivider(
                    color = ButtonBorder,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Untertitel
                Text(
                    text = stringResource(R.string.subtitle_hunt_mrx),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF3DBF82),
                        shadow = Shadow(
                            color = Color(0xFF3DBF82).copy(alpha = 0.9f),
                            offset = Offset.Zero,
                            blurRadius = subtitleGlow
                        )
                    )
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

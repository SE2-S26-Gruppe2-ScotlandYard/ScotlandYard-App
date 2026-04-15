package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

@Composable
fun RulesScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // dunkler Hintergrund
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Zurück-Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Zurück",
                    tint = Color.White,
                    modifier = Modifier
                        .height(28.dp)
                )
            }

            // Titel
            Text(
                text = "Spielregeln",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Regeln
            Text(
                text = "Ziel des Spiels",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Scotland Yard ist ein Verfolgungsspiel für 2-6 Spieler. Ein Spieler verkörpert den flüchtigen Mr. X, während die anderen Detektive sind, die versuchen, ihn zu fangen.",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "Rollen",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Mr. X: Versucht zu entkommen und bleibt den Detektiven verborgen.\n\nDetektive: Arbeiten zusammen, um Mr. X zu finden und zu fangen.",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                text = "Bewegung",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Spieler bewegen sich auf einem Netzwerk von Positionen. Verschiedene Verkehrsmittel (Bus, Taxi, U-Bahn) ermöglichen verschiedene Verbindungen.",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun RulesScreenPreview() {
    ScotlandYardTheme {
        RulesScreen(onBackClick = {})
    }
}


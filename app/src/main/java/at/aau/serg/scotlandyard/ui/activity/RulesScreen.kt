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
                text = "Scotland Yard – Spielregeln",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 🎯 Ziel des Spiels
            Text(
                text = "🎯 Ziel des Spiels",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "• Mr. X versucht, unerkannt zu entkommen\n• Die Detektive versuchen, Mr. X zu finden und zu fangen\n👉 Die Detektive gewinnen, wenn sie auf demselben Feld wie Mr. X landen\n👉 Mr. X gewinnt, wenn er bis zum Ende nicht gefangen wird",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 👥 Rollen
            Text(
                text = "👥 Rollen",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "🕵️ Detektive\n• Spielen im Team\n• Bewegen sich sichtbar auf der Karte\n• Versuchen, Mr. X einzukreisen\n\n🎩 Mr. X\n• Spielt alleine\n• Seine Position ist meist unsichtbar\n• Nutzt Täuschung und Strategie",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🗺️ Fortbewegung
            Text(
                text = "🗺️ Fortbewegung",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Spieler bewegen sich über die Karte mit:\n🚶 Walking → kurze Strecken, flexibel\n🛴 E-Scooter → mittlere Reichweite\n🚗 Car-Sharing → lange Strecken, schnell\n👉 Jede Verbindung auf der Karte ist einem dieser Transportmittel zugeordnet",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🎮 Spielablauf
            Text(
                text = "🎮 Spielablauf",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "1. Mr. X zieht\n• Wählt ein Ziel\n• Verwendet ein Transportmittel\n• Seine Position bleibt geheim\n• Nur das verwendete Ticket wird angezeigt\n👉 In bestimmten Runden muss Mr. X seine Position offenlegen\n\n2. Detektive ziehen\n• Ziehen nacheinander\n• Nutzen ihre Tickets (Walking, E-Scooter, Car-Sharing)\n• Versuchen, Mr. X strategisch einzukreisen",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🎫 Tickets
            Text(
                text = "🎫 Tickets & Spezialfähigkeiten",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Jeder Spieler hat begrenzte Tickets:\n• Walking\n• E-Scooter\n• Car-Sharing\n\n🎩 Spezialfähigkeiten von Mr. X:\n• 🎭 Black Ticket → Transportmittel bleibt geheim\n• 🔄 Double Move → zwei Züge in einer Runde",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🚫 Wichtige Regeln
            Text(
                text = "🚫 Wichtige Regeln",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "• Zwei Detektive dürfen nicht auf demselben Feld stehen\n• Detektive haben keine Spezialfähigkeiten\n• Ein Zug ist nur möglich, wenn ein passendes Ticket vorhanden ist\n• Spieler dürfen nicht stehen bleiben (außer sie können sich nicht bewegen)",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 🏁 Spielende
            Text(
                text = "🏁 Spielende",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Das Spiel endet, wenn:\n🕵️ Ein Detektiv Mr. X fängt → Detektive gewinnen\n🎩 Mr. X überlebt alle Runden → Mr. X gewinnt\n❌ Kein Detektiv kann sich mehr bewegen → Mr. X gewinnt",
                fontSize = 14.sp,
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


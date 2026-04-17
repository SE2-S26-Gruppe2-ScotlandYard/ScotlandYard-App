package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

/**
 * Reusable composable for a rules section (title + description).
 */
@Composable
private fun RulesSection(title: String, description: String) {
    // Section Title
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF1A4A3A),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Section Description
    Text(
        text = description,
        fontSize = 14.sp,
        color = Color(0xFFCCCCCC),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun RulesScreen(onBackClick: () -> Unit) {
    BaseScreen(onBackClick = onBackClick) { modifier ->
        val scrollModifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())

        androidx.compose.foundation.layout.Column(modifier = scrollModifier) {
            // Titel
            Text(
                text = "Scotland Yard – Spielregeln",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            RulesSection(
                title = "Ziel des Spiels",
                description = "• Mr. X versucht, unerkannt zu entkommen\n• Die Detektive versuchen, Mr. X zu finden und zu fangen\nDie Detektive gewinnen, wenn sie auf demselben Feld wie Mr. X landen\nMr. X gewinnt, wenn er bis zum Ende nicht gefangen wird"
            )

            RulesSection(
                title = "Rollen",
                description = "️Detektive\n• Spielen im Team\n• Bewegen sich sichtbar auf der Karte\n• Versuchen, Mr. X einzukreisen\n\n Mr. X\n• Spielt alleine\n• Seine Position ist meist unsichtbar\n• Nutzt Täuschung und Strategie"
            )

            RulesSection(
                title = "Fortbewegung",
                description = "Spieler bewegen sich über die Karte mit:\n Walking → kurze Strecken, flexibel\nE-Scooter → mittlere Reichweite\nCar-Sharing → lange Strecken, schnell\nJede Verbindung auf der Karte ist einem dieser Transportmittel zugeordnet"
            )

            RulesSection(
                title = "Spielablauf",
                description = "1. Mr. X zieht\n• Wählt ein Ziel\n• Verwendet ein Transportmittel\n• Seine Position bleibt geheim\n• Nur das verwendete Ticket wird angezeigt\nIn bestimmten Runden muss Mr. X seine Position offenlegen\n\n2. Detektive ziehen\n• Ziehen nacheinander\n• Nutzen ihre Tickets (Walking, E-Scooter, Car-Sharing)\n• Versuchen, Mr. X strategisch einzukreisen"
            )

            RulesSection(
                title = "Tickets & Spezialfähigkeiten",
                description = "Jeder Spieler hat begrenzte Tickets:\n• Walking\n• E-Scooter\n• Car-Sharing\n\n Spezialfähigkeiten von Mr. X:\n• Black Ticket → Transportmittel bleibt geheim\n• Double Move → zwei Züge in einer Runde"
            )

            RulesSection(
                title = "Wichtige Regeln",
                description = "• Zwei Detektive dürfen nicht auf demselben Feld stehen\n• Detektive haben keine Spezialfähigkeiten\n• Ein Zug ist nur möglich, wenn ein passendes Ticket vorhanden ist\n• Spieler dürfen nicht stehen bleiben (außer sie können sich nicht bewegen)"
            )

            // 🏁 Spielende
            Text(
                text = "Spielende",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Das Spiel endet, wenn:\n Ein Detektiv Mr. X fängt → Detektive gewinnen\n Mr. X überlebt alle Runden → Mr. X gewinnt\n Kein Detektiv kann sich mehr bewegen → Mr. X gewinnt",
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


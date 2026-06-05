package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

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
                text = stringResource(R.string.title_scotland_yard_rules),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_objective),
                description = stringResource(R.string.rules_description_objective)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_roles),
                description = stringResource(R.string.rules_description_roles)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_movement),
                description = stringResource(R.string.rules_description_movement)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_gameplay),
                description = stringResource(R.string.rules_description_gameplay)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_tickets),
                description = stringResource(R.string.rules_description_tickets)
            )

            RulesSection(
                title = stringResource(R.string.rules_title_important_rules),
                description = stringResource(R.string.rules_description_important_rules)
            )

            Text(
                text = stringResource(R.string.rules_title_game_over),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A4A3A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.rules_description_game_over),
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


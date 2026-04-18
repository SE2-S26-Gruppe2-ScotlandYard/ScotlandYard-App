package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable base screen composable with common layout (back button + title area + content).
 *
 * @param onBackClick Callback when back button is pressed
 * @param content Composable lambda for screen-specific content
 */
@Composable
fun BaseScreen(
    onBackClick: () -> Unit,
    content: @Composable (modifier: Modifier) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // dunkler Hintergrund
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            AppBackButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            // Screen-specific content
            content(Modifier)
        }
    }
}

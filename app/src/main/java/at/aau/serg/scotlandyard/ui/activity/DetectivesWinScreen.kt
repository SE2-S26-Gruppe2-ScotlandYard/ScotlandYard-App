package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

/**
 * Screen displayed when Detectives win the game.
 * Shows victory message and provides Restart and Quit buttons.
 *
 * Uses BaseWinScreen to eliminate code duplication with MrXWinScreen.
 *
 * @param onBackClick Callback for Restart button
 * @param onQuit Callback for Quit button
 */
@Composable
fun DetectivesWinScreen(
    onBackClick: () -> Unit,
    onQuit: () -> Unit
) {
    BaseWinScreen(
        messageText = stringResource(R.string.text_detectives_victory),
        backgroundImageRes = R.drawable.detectives_win_bg,
        onBackClick = onBackClick,
        onQuit = onQuit
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun DetectivesWinScreenPreview() {
    ScotlandYardTheme {
        DetectivesWinScreen(onBackClick = {}, onQuit = {})
    }
}

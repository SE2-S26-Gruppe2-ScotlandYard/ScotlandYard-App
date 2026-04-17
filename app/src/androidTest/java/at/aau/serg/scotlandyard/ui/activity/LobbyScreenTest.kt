package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for LobbyScreen.
 * Verifies that the screen renders without crashing and displays the title.
 */
@RunWith(AndroidJUnit4::class)
class LobbyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lobbyScreen_renders_successfully() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                LobbyScreen(onBackClick = {})
            }
        }

        // Verify that the title is displayed
        composeTestRule.onNodeWithText("Lobby").assertExists()
    }
}


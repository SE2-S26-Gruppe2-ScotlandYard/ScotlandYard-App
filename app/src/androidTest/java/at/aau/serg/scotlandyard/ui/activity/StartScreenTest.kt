package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for StartScreen.
 * Verifies that the screen renders without crashing and displays the main title.
 */
@RunWith(AndroidJUnit4::class)
class StartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startScreen_renders_successfully() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                StartScreen(
                    onStartGame = {},
                    onRules = {},
                    onSettings = {}
                )
            }
        }

        // Verify that the main title is displayed
        composeTestRule.onNodeWithText("SCOTLAND\nYARD").assertExists()
    }

    @Test
    fun startScreen_displays_buttons() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                StartScreen(
                    onStartGame = {},
                    onRules = {},
                    onSettings = {}
                )
            }
        }

        // Verify that both buttons are displayed
        composeTestRule.onNodeWithText("Start Game").assertExists()
        composeTestRule.onNodeWithText("Rules").assertExists()
    }

    @Test
    fun startScreen_displays_subtitle() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                StartScreen(
                    onStartGame = {},
                    onRules = {},
                    onSettings = {}
                )
            }
        }

        // Verify that the subtitle is displayed
        composeTestRule.onNodeWithText("Hunt Mr. X ecofriendly").assertExists()
    }
}

package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for RulesScreen.
 * Verifies that the screen renders without crashing and displays the main title and content.
 */
@RunWith(AndroidJUnit4::class)
class RulesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rulesScreen_renders_successfully() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                RulesScreen(onBackClick = {})
            }
        }

        // Verify that the main title is displayed
        composeTestRule.onNodeWithText("Scotland Yard – Spielregeln").assertExists()
    }

    @Test
    fun rulesScreen_displays_content_sections() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                RulesScreen(onBackClick = {})
            }
        }

        // Verify that main content sections are displayed
        composeTestRule.onNodeWithText("🎯 Ziel des Spiels").assertExists()
        composeTestRule.onNodeWithText("👥 Rollen").assertExists()
    }
}

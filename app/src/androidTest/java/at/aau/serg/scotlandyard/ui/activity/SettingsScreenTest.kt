package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for SettingsScreen.
 * Verifies that the screen renders without crashing and displays the title.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_renders_successfully() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                SettingsScreen(onBackClick = {})
            }
        }

        // Verify that the title is displayed
        composeTestRule.onNodeWithText("Einstellungen").assertExists()
    }
}

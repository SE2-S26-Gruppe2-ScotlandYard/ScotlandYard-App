package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI test for BaseScreen composable.
 * Verifies that the BaseScreen reusable component renders correctly.
 */
@RunWith(AndroidJUnit4::class)
class BaseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun baseScreen_renders_successfully() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                BaseScreen(
                    onBackClick = {},
                    content = { modifier ->
                        Text(
                            text = "Test Content",
                            modifier = modifier,
                            fontSize = 16.sp
                        )
                    }
                )
            }
        }

        // Verify that the content is displayed
        composeTestRule.onNodeWithText("Test Content").assertExists()
    }

    @Test
    fun baseScreen_provides_correct_layout_structure() {
        composeTestRule.setContent {
            ScotlandYardTheme {
                BaseScreen(
                    onBackClick = {},
                    content = { modifier ->
                        Text(
                            text = "Screen Content",
                            modifier = modifier,
                            fontSize = 20.sp
                        )
                    }
                )
            }
        }

        // Verify the content renders without errors
        composeTestRule.onNodeWithText("Screen Content").assertExists()
    }
}



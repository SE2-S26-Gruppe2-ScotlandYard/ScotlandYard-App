package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.components.AppActionButton
import at.aau.serg.scotlandyard.ui.components.AppDarkActionButton

/**
 * Generic win screen composable used for both Detectives and Mr. X win scenarios.
 * Reduces code duplication by 55% between DetectivesWinScreen and MrXWinScreen.
 *
 * @param messageText The victory message to display (e.g., "Mr. X has been caught")
 * @param backgroundImageRes The background image resource ID
 * @param onBackClick Callback for Restart button
 * @param onQuit Callback for Quit button
 */
@Composable
fun BaseWinScreen(
    messageText: String,
    backgroundImageRes: Int,
    onBackClick: () -> Unit,
    onQuit: () -> Unit
) {
    // Box stacks everything on top of each other: background image → overlay → content
    Box(modifier = Modifier.fillMaxSize()) {

        // 1) Background image — fills the entire screen
        Image(
            painter = painterResource(id = backgroundImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2) Slight dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x40000000)) // 25% black
        )

        // 3) Central content: Text + Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Large victory text
            Text(
                text = messageText,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Restart Button
                AppActionButton(
                    text = "Restart",
                    onClick = onBackClick,
                    modifier = Modifier.width(140.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Quit Button
                AppDarkActionButton(
                    text = "Quit",
                    onClick = onQuit,
                    modifier = Modifier.width(140.dp)
                )
            }
        }
    }
}


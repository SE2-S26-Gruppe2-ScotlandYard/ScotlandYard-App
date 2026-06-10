package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.components.AppActionButton
import at.aau.serg.scotlandyard.ui.theme.*
import com.example.scotlandyard.R

@Composable
fun DetectivesWinScreen(isMrX: Boolean, onMainMenu: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "police")

    // Blue siren flash: on during first half of 900 ms cycle
    val blueAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f    at 0
                0.55f at 60
                0.55f at 300
                0f    at 420
                0f    at 900
            }
        ),
        label = "blue"
    )

    // Red siren flash: on during second half of 900 ms cycle
    val redAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f    at 0
                0f    at 450
                0.55f at 510
                0.55f at 740
                0f    at 860
                0f    at 900
            }
        ),
        label = "red"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Background image
        Image(
            painter = painterResource(id = R.drawable.detectives_win_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Blue siren beam – radiates from the left edge
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SirenBlue.copy(alpha = blueAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Red siren beam – radiates from the right edge
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SirenRed.copy(alpha = redAlpha)
                        )
                    )
                )
        )

        // Central content — bottom padding reserves space for the button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            val lineColor = Color.White.copy(alpha = 0.35f)
            val labelColor = if (isMrX) LoseRed else WinGreen

            // YOU WON / YOU LOST label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(lineColor))
                Text(
                    text = "  ${stringResource(if (isMrX) R.string.text_you_lost else R.string.text_you_won)}  ",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        color = labelColor,
                        shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(0f, 2f), 8f)
                    )
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(lineColor))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main title
            Text(
                text = stringResource(R.string.text_detectives_victory),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 80.sp,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        offset = Offset(0f, 3f),
                        blurRadius = 14f
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom decorative line
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(lineColor))
        }

        // Single button at the bottom
        AppActionButton(
            text = stringResource(R.string.button_return_main_menu),
            onClick = onMainMenu,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(280.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun DetectivesWinScreenPreview() {
    ScotlandYardTheme {
        DetectivesWinScreen(isMrX = false, onMainMenu = {})
    }
}

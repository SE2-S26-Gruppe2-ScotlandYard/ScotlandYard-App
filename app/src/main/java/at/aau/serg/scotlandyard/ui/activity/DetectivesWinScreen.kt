package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
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
                            Color(0xFF1144EE).copy(alpha = blueAlpha),
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
                            Color(0xFFEE1133).copy(alpha = redAlpha)
                        )
                    )
                )
        )

        // Central content — bottom padding reserves space for the button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp, vertical = 0.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // YOU WON / YOU LOST — personal outcome label
            Text(
                text = stringResource(if (isMrX) R.string.text_you_lost else R.string.text_you_won),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = if (isMrX) Color(0xFFCC4444) else Color(0xFF44DD77),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 2f),
                        blurRadius = 10f
                    )
                )
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = stringResource(R.string.text_detectives_victory),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 80.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF7788BB),
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.9f),
                        offset = Offset(0f, 3f),
                        blurRadius = 14f
                    )
                )
            )
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

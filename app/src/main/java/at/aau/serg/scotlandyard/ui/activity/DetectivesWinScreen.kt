package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.components.WinScreenScaffold
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import at.aau.serg.scotlandyard.ui.theme.SirenBlue
import at.aau.serg.scotlandyard.ui.theme.SirenRed
import com.example.scotlandyard.R

@Composable
fun DetectivesWinScreen(isMrX: Boolean, onMainMenu: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "police")

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

    WinScreenScaffold(
        iWon = !isMrX,
        titleRes = R.string.text_detectives_victory,
        onMainMenu = onMainMenu
    ) {
        Image(
            painter = painterResource(id = R.drawable.detectives_win_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SirenBlue.copy(alpha = blueAlpha), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, SirenRed.copy(alpha = redAlpha))
                    )
                )
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

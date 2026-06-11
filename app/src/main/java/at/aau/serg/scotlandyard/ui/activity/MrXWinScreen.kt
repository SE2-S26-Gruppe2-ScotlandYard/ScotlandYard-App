package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import at.aau.serg.scotlandyard.ui.components.WinScreenScaffold
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

@Composable
fun MrXWinScreen(isMrX: Boolean, onMainMenu: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "mrxbreath")

    val nightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "night"
    )

    WinScreenScaffold(
        iWon = isMrX,
        titleRes = R.string.text_mrx_victory,
        onMainMenu = onMainMenu
    ) {
        Image(
            painter = painterResource(id = R.drawable.mrx_win_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = nightAlpha))
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun MrXWinScreenPreview() {
    ScotlandYardTheme {
        MrXWinScreen(isMrX = true, onMainMenu = {})
    }
}

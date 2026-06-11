package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.theme.LoseRed
import at.aau.serg.scotlandyard.ui.theme.WinGreen
import com.example.scotlandyard.R

@Composable
fun WinScreenScaffold(
    iWon: Boolean,
    titleRes: Int,
    onMainMenu: () -> Unit,
    backgroundContent: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        backgroundContent()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp)
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val lineColor = Color.White.copy(alpha = 0.35f)
            val labelColor = if (iWon) WinGreen else LoseRed

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(lineColor))
                Text(
                    text = "  ${stringResource(if (iWon) R.string.text_you_won else R.string.text_you_lost)}  ",
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

            Text(
                text = stringResource(titleRes),
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

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(lineColor))
        }

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

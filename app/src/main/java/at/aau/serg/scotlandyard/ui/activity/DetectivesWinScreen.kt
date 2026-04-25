package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

@Composable
fun DetectivesWinScreen(
    onBackClick: () -> Unit,
    onQuit: () -> Unit
) {
    // Box stapelt alles übereinander: Hintergrundbild → Inhalt
    Box(modifier = Modifier.fillMaxSize()) {

        // 1) Hintergrundbild — füllt den ganzen Screen
        Image(
            painter = painterResource(id = R.drawable.detectives_win_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2) Leichter dunkler Overlay damit Text lesbar bleibt
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x40000000)) // 25% schwarz
        )

        // 3) Zentrale Inhalte: Text + Button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Großer siegreicher Text
            Text(
                text = "Mr. X has been caught",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons nebeneinander
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Restart Button
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .width(140.dp)
                        .height(52.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xAAFFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A3A)     // teal/dunkelgrün
                    )
                ) {
                    Text(
                        text = "Restart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Quit Button
                Button(
                    onClick = onQuit,
                    modifier = Modifier
                        .width(140.dp)
                        .height(52.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xAAFFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D0D0D)     // sehr dunkel
                    )
                ) {
                    Text(
                        text = "Quit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun DetectivesWinScreenPreview() {
    ScotlandYardTheme {
        DetectivesWinScreen(onBackClick = {}, onQuit = {})
    }
}


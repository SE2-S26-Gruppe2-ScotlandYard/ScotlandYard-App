package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.scotlandyard.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable base screen composable with common layout (back button + title area + content).
 * Uses background.png as background image.
 *
 * @param onBackClick Callback when back button is pressed
 * @param content Composable lambda for screen-specific content
 */
@Composable
fun BaseScreen(
    onBackClick: () -> Unit,
    content: @Composable (modifier: Modifier) -> Unit
) {
    val isEnabled = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Zurück-Button with debouncing
            IconButton(
                onClick = {
                    if (isEnabled.value) {
                        isEnabled.value = false
                        onBackClick()
                        scope.launch {
                            delay(500) // Debounce delay
                            isEnabled.value = true
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp),
                enabled = isEnabled.value
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück",
                    tint = Color.White
                )
            }

            // Screen-specific content
            content(Modifier)
        }
    }
}


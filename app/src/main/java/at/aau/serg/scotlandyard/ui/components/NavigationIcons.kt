package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavigationIconTint = Color(0xFFE0E0E0)
private val ActionButtonColor = Color(0xFF1A4A3A)
private val DarkActionButtonColor = Color(0xFF102920)
private val ActionButtonShape = RoundedCornerShape(6.dp)

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Zurueck"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.then(Modifier.then(Modifier))
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = NavigationIconTint,
            modifier = Modifier
        )
    }
}

@Composable
fun AppSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Settings"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = contentDescription,
            tint = NavigationIconTint,
            modifier = Modifier
        )
    }
}

@Composable
fun AppActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppStyledActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = ActionButtonColor
    )
}

@Composable
fun AppDarkActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppStyledActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = DarkActionButtonColor
    )
}

@Composable
private fun AppStyledActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .border(
                width = 1.dp,
                color = Color(0xAAFFFFFF),
                shape = ActionButtonShape
            ),
        shape = ActionButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}


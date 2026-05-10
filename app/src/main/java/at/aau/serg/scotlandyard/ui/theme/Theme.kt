package at.aau.serg.scotlandyard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

 val BackgroundDark = Color(0xFF0D1B2A)
 val SidebarBg = Color(0xFF101E2D)
 val SidebarBorder = Color(0xFF1E3347)
 val RoundCounterBg = Color(0xFF1A2F44)
 val RoundCounterBorder = Color(0xFF2A4A62)
 val AccentTeal = Color(0xFF1A4A3A)
 val AccentGlow = Color(0xFF22AA80)
 val TextPrimary = Color(0xFFE8EEF4)
 val TextMuted = Color(0xFF7A96B0)
 val CanvasBgColor = Color(0xFF0F2235)
 val WalkingColor = Color(0xFFD4B963)
 val EScooterColor = Color(0xFF3D8E79)
 val CarSharingColor = Color(0xFFED2939) // or Brown:0xFFA67C65
 val BlackColor = Color(0xFFCC44CC)
 val NodeFill = Color(0xFF1A3A55)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ScotlandYardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
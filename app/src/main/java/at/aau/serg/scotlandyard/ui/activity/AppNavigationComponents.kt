package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

private val NavigationIconTint = Color(0xFFE0E0E0)
private val ActionButtonColor = Color(0xFF1A4A3A)
private val DarkActionButtonColor = Color(0xFF102920)
private val ActionButtonShape = RoundedCornerShape(6.dp)
private val TicketButtonShape = RoundedCornerShape(12.dp)

private val WalkingTicketColor = Color(0xFFD4B963)
private val EScooterTicketColor = Color(0xFF3D8E79)
private val CarSharingTicketColor = Color(0xFFA67C65)
private val BlackTicketColor = Color(0xFF2C2C2C)
private val DoubleTicketColor = Color(0xFFF090F5)

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Zurueck"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = NavigationIconTint,
            modifier = Modifier.size(28.dp)
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
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = contentDescription,
            tint = NavigationIconTint,
            modifier = Modifier.size(28.dp)
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

enum class AppTicketType {
    Walking,
    EScooter,
    CarSharing,
    Black,
    Double
}

private enum class TicketCenterStyle {
    WalkingIcon,
    EScooterIcon,
    CarIcon,
    EmptyCircle,
    Text2x,
    TextValue
}

private data class TicketCardStyle(
    val id: String,
    val label: String,
    val backgroundColor: Color,
    val centerStyle: TicketCenterStyle
)

private val ticketPreviewData = listOf(
    TicketCardStyle("walking", "WALKING", WalkingTicketColor, TicketCenterStyle.WalkingIcon),
    TicketCardStyle("e_scooter", "E-SCOOTER", EScooterTicketColor, TicketCenterStyle.EScooterIcon),
    TicketCardStyle("car_sharing", "CAR SHARING", CarSharingTicketColor, TicketCenterStyle.CarIcon),
    TicketCardStyle("black_ticket", "BLACK TICKET", BlackTicketColor, TicketCenterStyle.EmptyCircle),
    TicketCardStyle("multiplier", "2x", DoubleTicketColor, TicketCenterStyle.Text2x)
)

@Composable
fun AppTicketButton(
    text: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ticketColor: Color = Color(0xFFE2C84B)
) {
    AppTicketButton(
        text = text,
        count = if (count > 0) count.toString() else "",
        onClick = onClick,
        modifier = modifier,
        ticketColor = ticketColor
    )
}

@Composable
fun AppTicketButton(
    text: String,
    count: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ticketColor: Color = Color(0xFFE2C84B)
) {
    val normalizedLabel = text.uppercase()
    val centerStyle = if (count.isNotBlank()) TicketCenterStyle.TextValue else TicketCenterStyle.EmptyCircle
    TicketCard(
        label = normalizedLabel,
        ticketColor = ticketColor,
        centerStyle = centerStyle,
        centerText = count,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun TicketCard(
    label: String,
    ticketColor: Color,
    centerStyle: TicketCenterStyle,
    centerText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFFFFFFF),
                shape = TicketButtonShape
            )
            .clickable(onClick = onClick),
        shape = TicketButtonShape,
        colors = CardDefaults.cardColors(containerColor = ticketColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TicketGraphic(
                ticketColor = ticketColor,
                centerStyle = centerStyle,
                centerText = centerText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TicketGraphic(
    ticketColor: Color,
    centerStyle: TicketCenterStyle,
    centerText: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.size(48.dp))
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .background(Color.White)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (centerStyle) {
                TicketCenterStyle.WalkingIcon -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "Walking",
                    tint = ticketColor,
                    modifier = Modifier.size(24.dp)
                )

                TicketCenterStyle.EScooterIcon -> Icon(
                    imageVector = Icons.Default.ElectricScooter,
                    contentDescription = "E-Scooter",
                    tint = ticketColor,
                    modifier = Modifier.size(24.dp)
                )

                TicketCenterStyle.CarIcon -> Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Car Sharing",
                    tint = ticketColor,
                    modifier = Modifier.size(24.dp)
                )

                TicketCenterStyle.Text2x,
                TicketCenterStyle.TextValue -> Text(
                    text = centerText,
                    color = ticketColor,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )

                TicketCenterStyle.EmptyCircle -> Unit
            }
        }
    }
}

@Composable
fun AppTicketButton(
    type: AppTicketType,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = ticketStyle(type)
    TicketCard(
        label = style.label,
        ticketColor = style.backgroundColor,
        centerStyle = style.centerStyle,
        centerText = if (style.centerStyle == TicketCenterStyle.TextValue) count.toString() else "2x",
        onClick = onClick,
        modifier = modifier
    )
}

private data class TicketVisualStyle(
    val label: String,
    val backgroundColor: Color,
    val centerStyle: TicketCenterStyle
)

private fun ticketStyle(type: AppTicketType): TicketVisualStyle {
    return when (type) {
        AppTicketType.Walking -> TicketVisualStyle("WALKING", WalkingTicketColor, TicketCenterStyle.WalkingIcon)
        AppTicketType.EScooter -> TicketVisualStyle("E-SCOOTER", EScooterTicketColor, TicketCenterStyle.EScooterIcon)
        AppTicketType.CarSharing -> TicketVisualStyle("CAR SHARING", CarSharingTicketColor, TicketCenterStyle.CarIcon)
        AppTicketType.Black -> TicketVisualStyle("BLACK TICKET", BlackTicketColor, TicketCenterStyle.EmptyCircle)
        AppTicketType.Double -> TicketVisualStyle("2x", DoubleTicketColor, TicketCenterStyle.Text2x)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 420)
@Composable
fun TicketButtonsPreview() {
    ScotlandYardTheme {
        LazyColumn(
            modifier = Modifier
                .background(Color(0xFF0D1B2A))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ticketPreviewData, key = { it.id }) { ticket ->
                TicketCard(
                    label = ticket.label,
                    ticketColor = ticket.backgroundColor,
                    centerStyle = ticket.centerStyle,
                    centerText = if (ticket.centerStyle == TicketCenterStyle.Text2x) "2x" else "",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import at.aau.serg.scotlandyard.model.TicketCenterStyle
import at.aau.serg.scotlandyard.model.TicketStyle
import at.aau.serg.scotlandyard.model.TicketStyleProvider
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

private val TicketButtonShape = RoundedCornerShape(16.dp)
private val TicketCardHeight = 120.dp
private val TicketBandHeight = 25.dp
private val TicketOuterDiscSize = 64.dp
private val TicketInnerDiscSize = 56.dp
private val TicketLabelSize = 20.sp

@Composable
fun AppTicketButton(
    type: TicketType,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = TicketStyleProvider.fromType(type)
    TicketCard(
        style = style,
        count = count,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun TicketCard(
    style: TicketStyle,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(TicketCardHeight)
            .border(
                width = 1.dp,
                color = Color(0xFFFFFFFF),
                shape = TicketButtonShape
            )
            .clickable(onClick = onClick),
        shape = TicketButtonShape,
        colors = CardDefaults.cardColors(containerColor = style.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TicketGraphic(
                style = style,
                count = count,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            )

            Text(
                text = style.label,
                color = Color.White,
                fontSize = TicketLabelSize,
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
    style: TicketStyle,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(TicketBandHeight)
                .align(Alignment.Center)
                .background(Color.White)
        )

        // Outer disc in ticket color cuts through the white band and sits on top.
        Box(
            modifier = Modifier
                .size(TicketOuterDiscSize)
                .align(Alignment.Center)
                .background(style.backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(TicketInnerDiscSize)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (style.centerStyle) {
                    TicketCenterStyle.WalkingIcon -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Walking",
                        tint = style.backgroundColor,
                        modifier = Modifier.size(28.dp)
                    )

                    TicketCenterStyle.EScooterIcon -> Icon(
                        imageVector = Icons.Default.ElectricScooter,
                        contentDescription = "E-Scooter",
                        tint = style.backgroundColor,
                        modifier = Modifier.size(28.dp)
                    )

                    TicketCenterStyle.CarIcon -> Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car Sharing",
                        tint = style.backgroundColor,
                        modifier = Modifier.size(28.dp)
                    )

                    TicketCenterStyle.Text2x,
                    TicketCenterStyle.TextValue -> {
                        val centerText = when {
                            style.centerStyle == TicketCenterStyle.Text2x -> style.centerText
                            count > 0 -> count.toString()
                            else -> ""
                        }
                        if (centerText.isNotBlank()) {
                            Text(
                                text = centerText,
                                color = style.backgroundColor,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    TicketCenterStyle.EmptyCircle -> Unit
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 800)
@Composable
fun TicketComponentPreview() {
    ScotlandYardTheme {
        LazyColumn(
            modifier = Modifier
                .background(Color(0xFF0D1B2A))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(TicketStyleProvider.allTickets(), key = { it.type.name }) { style ->
                AppTicketButton(
                    type = style.type,
                    count = when (style.type) {
                        TicketType.Walking -> 10
                        TicketType.EScooter -> 8
                        TicketType.CarSharing -> 6
                        TicketType.Black -> 2
                        TicketType.Double -> 2
                    },
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


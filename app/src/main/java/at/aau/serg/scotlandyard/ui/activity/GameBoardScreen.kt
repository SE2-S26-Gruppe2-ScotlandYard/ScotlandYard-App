package at.aau.serg.scotlandyard.ui.activity

import android.R.attr.viewportHeight
import android.R.attr.viewportWidth
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.model.TicketStyleProvider
import at.aau.serg.scotlandyard.ui.components.BOARD_HEIGHT_DP
import at.aau.serg.scotlandyard.ui.components.BOARD_WIDTH_DP
import at.aau.serg.scotlandyard.ui.components.GameBoardCanvas
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme

private val BackgroundDark = Color(0xFF0D1B2A)
private val SidebarBg = Color(0xFF101E2D)
private val SidebarBorder = Color(0xFF1E3347)
private val RoundCounterBg = Color(0xFF1A2F44)
private val RoundCounterBorder = Color(0xFF2A4A62)
private val AccentTeal = Color(0xFF1A4A3A)
private val AccentGlow = Color(0xFF22AA80)
private val TextPrimary = Color(0xFFE8EEF4)
private val TextMuted = Color(0xFF7A96B0)

enum class PlayerRole { DETECTIVE, MR_X }


/**
 * Main game screen.
 *
 * @param isMrX          true → show all 5 tickets; false → show detective tickets only
 * @param currentRound   Round number displayed in the header badge
 * @param totalRounds    Total rounds in the game
 * @param ticketCounts   Map from TicketType to remaining count for the local player
 * @param onTicketSelect Called when the player taps a ticket
 * @param onMenuClick    Called when the menu icon is tapped
 */
@Composable
fun GameBoardScreen(
    isMrX: Boolean = false,
    currentRound: Int = 1,
    totalRounds: Int = 22,
    ticketCounts: Map<TicketType, Int> = defaultTicketCounts(isMrX),
    onTicketSelect: (TicketType) -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    var selectedTicket by remember { mutableStateOf<TicketType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        BoardArea(modifier = Modifier.fillMaxSize())

        SidePanel(
            isMrX = isMrX,
            currentRound = currentRound,
            totalRounds = totalRounds,
            ticketCounts = ticketCounts,
            selectedTicket = selectedTicket,
            onMenuClick = onMenuClick,
            onTicketSelect = { type ->
                selectedTicket = if (selectedTicket == type) null else type
                onTicketSelect(type)
            }
        )
    }
}

@Composable
private fun SidePanel(
    isMrX: Boolean,
    currentRound: Int,
    totalRounds: Int,
    ticketCounts: Map<TicketType, Int>,
    selectedTicket: TicketType?,
    onMenuClick: () -> Unit,
    onTicketSelect: (TicketType) -> Unit
) {
    val visibleTickets = if (isMrX)
        listOf(TicketType.WALKING, TicketType.ESCOOTER, TicketType.CARSHARING, TicketType.BLACK, TicketType.DOUBLE)
    else
        listOf(TicketType.WALKING, TicketType.ESCOOTER, TicketType.CARSHARING)

    Column(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .background(SidebarBg)
            .border(
                width = 1.dp,
                color = SidebarBorder,
                shape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp)
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row: Menu and Round Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuButton(onClick = onMenuClick)
            RoundCounter(current = currentRound, total = totalRounds)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Role label
        Text(
            text = if (isMrX) "MR. X" else "DETECTIVE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (isMrX) Color(0xFFF090F5) else AccentGlow,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SidebarBorder)
        )

        // Ticket buttons
        visibleTickets.forEach { type ->
            val count = ticketCounts[type] ?: 0
            val isSelected = selectedTicket == type
            val isDisabled = count == 0

            SidePanelTicketButton(
                type = type,
                count = count,
                isSelected = isSelected,
                isDisabled = isDisabled,
                onClick = { if (!isDisabled) onTicketSelect(type) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Selected ticket hint
        if (selectedTicket != null) {
            val style = TicketStyleProvider.fromType(selectedTicket)
            Text(
                text = "${style.label}\nselected",
                fontSize = 10.sp,
                color = AccentGlow,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MenuButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = TextPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun RoundCounter(current: Int, total: Int) {
    Box(
        modifier = Modifier
            .background(RoundCounterBg, RoundedCornerShape(8.dp))
            .border(1.dp, RoundCounterBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ROUND",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Text(
                text = "$current/$total",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SidePanelTicketButton(
    type: TicketType,
    count: Int,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val style = TicketStyleProvider.fromType(type)

    val bgAlpha = when {
        isDisabled  -> 0.25f
        isSelected  -> 1f
        else        -> 0.75f
    }
    val scaleFactor by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = tween(150),
        label = "ticketScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentGlow else Color(0x44FFFFFF),
        animationSpec = tween(150),
        label = "ticketBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scaleFactor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isDisabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = style.backgroundColor.copy(alpha = bgAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = style.label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisabled) Color(0x88FFFFFF) else Color.White,
                lineHeight = 11.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color.White.copy(alpha = if (isDisabled) 0.1f else 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDisabled) Color(0x66FFFFFF) else Color.White
                )
            }
        }
    }
}

@Composable
private fun BoardArea(modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var viewportWidth  by remember { mutableFloatStateOf(0f) }
    var viewportHeight by remember { mutableFloatStateOf(0f) }

    val boardWidthDp  = BOARD_WIDTH_DP
    val boardHeightDp = BOARD_HEIGHT_DP
    val density = LocalDensity.current

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.8f, 5f)

        val boardWidthPx  = boardWidthDp  * density.density
        val boardHeightPx = boardHeightDp * density.density

        val scaledHalfW = (boardWidthPx  * newScale) / 2f
        val scaledHalfH = (boardHeightPx * newScale) / 2f
        val viewHalfW   = viewportWidth  / 2f
        val viewHalfH   = viewportHeight / 2f

        val xPaddingPx = 200f * density.density
        val maxX = (scaledHalfW - viewHalfW + xPaddingPx).coerceAtLeast(0f)
        val maxY = (scaledHalfH - viewHalfH).coerceAtLeast(0f)

        val newOffset = Offset(
            x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
        )

        scale  = newScale
        offset = newOffset
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1520))
            .transformable(state = transformState)
            .onSizeChanged { size ->
                viewportWidth  = size.width.toFloat()
                viewportHeight = size.height.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            GameBoardCanvas()
        }

        // Usability hint
        Text(
            text = "Pinch to zoom - Drag to pan",
            fontSize = 10.sp,
            color = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

// Ticket Count helper TODO: Replace hardcoded values with requests to server
fun defaultTicketCounts(isMrX: Boolean): Map<TicketType, Int> = buildMap {
    put(TicketType.WALKING,    isMrX.not().let { if (it) 10 else 4 })
    put(TicketType.ESCOOTER,   isMrX.not().let { if (it) 8  else 3 })
    put(TicketType.CARSHARING, isMrX.not().let { if (it) 4  else 3 })
    if (isMrX) {
        put(TicketType.BLACK,  5)
        put(TicketType.DOUBLE, 2)
    }
}

// Previews

@Preview(showBackground = true, widthDp = 800, heightDp = 400, name = "Detective View")
@Composable
fun GameBoardScreenDetectivePreview() {
    ScotlandYardTheme {
        GameBoardScreen(
            isMrX = false,
            currentRound = 5,
            totalRounds = 22
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400, name = "Mr. X View")
@Composable
fun GameBoardScreenMrXPreview() {
    ScotlandYardTheme {
        GameBoardScreen(
            isMrX = true,
            currentRound = 8,
            totalRounds = 22
        )
    }
}
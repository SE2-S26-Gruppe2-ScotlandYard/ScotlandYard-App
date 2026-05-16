package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.model.TicketStyleProvider
import at.aau.serg.scotlandyard.ui.components.BOARD_HEIGHT_DP
import at.aau.serg.scotlandyard.ui.components.BOARD_WIDTH_DP
import at.aau.serg.scotlandyard.ui.components.GameBoardCanvas
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import kotlin.math.abs

/**
 * Main game screen.
 *
 * @param isMrX                 true → show all 5 tickets; false → show detective tickets only
 * @param currentRound          Round number displayed in the header badge
 * @param totalRounds           Total rounds in the game
 * @param ticketCounts          Map from TicketType to remaining count for the local player
 * @param playerPositions       Map from Color to station ID for drawing player tokens
 * @param highlightedNodes      Set of station IDs to highlight as reachable
 * @param isMyTurn              Whether the local player is currently allowed to move
 * @param selectedTicket        Currently selected ticket (managed externally)
 * @param onTicketSelect        Called when the player taps a ticket
 * @param onNodeClick           Called when the player taps a board node
 * @param onNavigateToSettings  Called when the player taps Settings in the menu
 */
@Composable
fun GameBoardScreen(
    isMrX: Boolean = false,
    currentRound: Int = 1,
    displayMode: BoardDisplayMode,
    totalRounds: Int = 22,
    ticketCounts: Map<TicketType, Int> = defaultTicketCounts(isMrX),
    playerPositions: Map<Color, Int> = emptyMap(),
    highlightedNodes: Set<Int> = emptySet(),
    isMyTurn: Boolean = false,
    selectedTicket: TicketType? = null,
    onNodeClick: ((Int) -> Unit)? = null,
    onTicketSelect: (TicketType) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        BoardArea(
            modifier = Modifier.fillMaxSize(),
            displayMode = displayMode,
            playerPositions = playerPositions,
            highlightedNodes = highlightedNodes,
            onNodeClick = if (isMyTurn) onNodeClick else null
        )

        SidePanel(
            isMrX = isMrX,
            currentRound = currentRound,
            totalRounds = totalRounds,
            ticketCounts = ticketCounts,
            selectedTicket = selectedTicket,
            isMyTurn = isMyTurn,
            onMenuClick = { isMenuOpen = true },
            onTicketSelect = { type -> onTicketSelect(type) }
        )

        if (selectedTicket == TicketType.DOUBLE) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(AccentGlow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, AccentGlow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Double Move active – tap a station to use",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGlow,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Menu overlay - rendered on top of everything else
        MenuOverlay(
            isVisible = isMenuOpen,
            onClose = { isMenuOpen = false },
            onNavigateToSettings = {
                isMenuOpen = false
                onNavigateToSettings()
            }
        )
    }
}

@Composable
private fun MenuOverlay(
    isVisible: Boolean,
    onClose: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        // Dim backdrop - tap outside the box to close
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(initialScale = 0.88f, animationSpec = tween(220)) +
                        fadeIn(animationSpec = tween(220)),
                exit = scaleOut(targetScale = 0.88f, animationSpec = tween(160)) +
                        fadeOut(animationSpec = tween(160))
            ) {
                MenuCard(
                    onClose = onClose,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun MenuCard(
    onClose: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(min = 260.dp, max = 340.dp)
            // Stop clicks to the gameboard through the menu overlay
            .clickable(enabled = false, onClick = {}),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row: title + close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1E3347), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close menu",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                color = SidebarBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Menu entries
            MenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onNavigateToSettings
            )

            // TODO: Add other items here [MenuItem(icon = Icons.Default.ExitToApp, label = "Leave", onClick = onQuit)]
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF152536), RoundedCornerShape(10.dp))
            .border(1.dp, SidebarBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGlow,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
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
    isMyTurn: Boolean = false,
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visibleTickets.forEach { type ->
                val count = ticketCounts[type] ?: 0
                SidePanelTicketButton(
                    type = type,
                    count = count,
                    isSelected = selectedTicket == type,
                    isDisabled = count == 0 || !isMyTurn,
                    onClick = { onTicketSelect(type) }
                )
            }
        }

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
        isDisabled -> 0.25f
        isSelected -> 1f
        else -> 0.75f
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
                    text = if (count == Int.MAX_VALUE) "∞" else count.toString(),
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
private fun BoardArea(
    modifier: Modifier = Modifier,
    displayMode: BoardDisplayMode,
    playerPositions: Map<Color, Int> = emptyMap(),
    highlightedNodes: Set<Int> = emptySet(),
    onNodeClick: ((Int) -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var viewportWidth by remember { mutableFloatStateOf(0f) }
    var viewportHeight by remember { mutableFloatStateOf(0f) }

    val boardWidthDp = BOARD_WIDTH_DP
    val boardHeightDp = BOARD_HEIGHT_DP

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1520))
            .onSizeChanged { size ->
                viewportWidth = size.width.toFloat()
                viewportHeight = size.height.toFloat()
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    var zoom = 1f
                    var pan = Offset.Zero
                    var pastTouchSlop = false
                    val touchSlop = viewConfiguration.touchSlop

                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoom *= zoomChange
                                pan += panChange
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - zoom) * centroidSize
                                val panMotion = pan.getDistance()
                                if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                val newScale = (scale * zoomChange).coerceIn(0.8f, 5f)
                                val boardWidthPx = boardWidthDp * density
                                val boardHeightPx = boardHeightDp * density
                                val scaledHalfW = (boardWidthPx * newScale) / 2f
                                val scaledHalfH = (boardHeightPx * newScale) / 2f
                                val viewHalfW = viewportWidth / 2f
                                val viewHalfH = viewportHeight / 2f
                                val xPaddingPx = 200f * density
                                val maxX = (scaledHalfW - viewHalfW + xPaddingPx).coerceAtLeast(0f)
                                val maxY = (scaledHalfH - viewHalfH).coerceAtLeast(0f)
                                scale = newScale
                                offset = Offset(
                                    x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                                )
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
        ) {
            GameBoardCanvas(
                displayMode = displayMode,
                playerPositions = playerPositions,
                highlightedNodes = highlightedNodes,
                onNodeClick = onNodeClick
            )
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
    put(TicketType.WALKING, if (isMrX) 4 else 10)
    put(TicketType.ESCOOTER, if (isMrX) 3 else 8)
    put(TicketType.CARSHARING, if (isMrX) 3 else 4)
    if (isMrX) {
        put(TicketType.BLACK, 5)
        put(TicketType.DOUBLE, 2)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400, name = "Detective View")
@Composable
fun GameBoardScreenDetectivePreview() {
    ScotlandYardTheme {
        GameBoardScreen(
            isMrX = false,
            currentRound = 5,
            displayMode = BoardDisplayMode.GRAPH,
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
            displayMode = BoardDisplayMode.GRAPH,
            totalRounds = 22
        )
    }
}
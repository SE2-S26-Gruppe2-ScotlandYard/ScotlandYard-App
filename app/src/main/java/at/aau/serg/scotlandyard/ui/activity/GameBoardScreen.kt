package at.aau.serg.scotlandyard.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.model.TicketStyleProvider
import at.aau.serg.scotlandyard.ui.components.GameBoardCanvas
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R
import kotlin.math.abs

/**
 * Main game screen.
 *
 * @param isMrX                 true -> show all 5 tickets; false -> show detective tickets only
 * @param mrXRevealedPositions
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
    mrXRevealedPositions: Map<Int, Int> = emptyMap(),
    currentRound: Int = 1,
    displayMode: BoardDisplayMode,
    totalRounds: Int = 22,
    ticketCounts: Map<TicketType, Int> = defaultTicketCounts(isMrX),
    playerPositions: Map<Color, Int> = emptyMap(),
    highlightedNodes: Set<Int> = emptySet(),
    isMyTurn: Boolean = false,
    isDoubleActive: Boolean = false,
    mrXMoveHistory: List<String> = emptyList(),
    revealHistoryIndices: Map<Int, Int> = emptyMap(),
    selectedTicket: TicketType? = null,
    currentPlayerColor: Color? = null,
    onNodeClick: ((Int) -> Unit)? = null,
    onTicketSelect: (TicketType) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var isHistoryOpen by remember { mutableStateOf(false) }

    // Let the server decide which rounds are reveal rounds via the map it sends.
    val currentRevealPosition = mrXRevealedPositions[currentRound]

    var showRevealBanner by remember { mutableStateOf(false) }
    var revealedPosition by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(currentRevealPosition, isDoubleActive) {
        if (!isMrX && currentRevealPosition != null && !isDoubleActive) {
            revealedPosition = currentRevealPosition
            showRevealBanner = true
            delay(5_000.milliseconds)
            showRevealBanner = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(modifier = Modifier.fillMaxSize()) {
            SidePanel(
                isMrX = isMrX,
                currentRound = currentRound,
                totalRounds = totalRounds,
                ticketCounts = ticketCounts,
                selectedTicket = selectedTicket,
                isMyTurn = isMyTurn,
                currentPlayerColor = currentPlayerColor,
                onNavigateToSettings = onNavigateToSettings,
                onMrXHistoryClick = { isHistoryOpen = true },
                onTicketSelect = { type -> onTicketSelect(type) }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 8.dp, end = 8.dp, bottom = 8.dp)
                    .border(2.dp, AccentGlow.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                BoardArea(
                    modifier = Modifier.fillMaxSize(),
                    displayMode = displayMode,
                    playerPositions = playerPositions,
                    highlightedNodes = highlightedNodes,
                    onNodeClick = if (isMyTurn) onNodeClick else null
                )

                // Reveal Mr. X position banner, top-center, auto-dismisses after 5s
                RevealBanner(
                    visible = showRevealBanner,
                    position = revealedPosition ?: 0,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )

                // Double move active hint — shown while MrX picks their 2 sub-moves
                if (isDoubleActive && isMrX) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .background(Color(0xCC0D1E2E), RoundedCornerShape(8.dp))
                            .border(2.dp, AccentGlow, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.banner_double_move_hint),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGlow,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Mr. X move history overlay
        MrXHistoryOverlay(
            isVisible = isHistoryOpen,
            moveHistory = mrXMoveHistory,
            mrXRevealedPositions = mrXRevealedPositions,
            revealHistoryIndices = revealHistoryIndices,
            onClose = { isHistoryOpen = false }
        )

    }
}

@Composable
private fun MrXHistoryOverlay(
    isVisible: Boolean,
    moveHistory: List<String>,
    mrXRevealedPositions: Map<Int, Int> = emptyMap(),
    revealHistoryIndices: Map<Int, Int> = emptyMap(),
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkOverlay)
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
                MrXHistoryCard(moveHistory = moveHistory, mrXRevealedPositions = mrXRevealedPositions, revealHistoryIndices = revealHistoryIndices, onClose = onClose)
            }
        }
    }
}

@Composable
private fun MrXHistoryCard(
    moveHistory: List<String>,
    mrXRevealedPositions: Map<Int, Int> = emptyMap(),
    revealHistoryIndices: Map<Int, Int> = emptyMap(),
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(min = 260.dp, max = 360.dp)
            .clickable(enabled = false, onClick = {}),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_mrx_moves),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
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
                        contentDescription = stringResource(R.string.button_close),
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .background(SidebarBorder)
            )

            if (moveHistory.isEmpty()) {
                Text(
                    text = stringResource(R.string.text_no_moves_yet),
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Build a reverse lookup: history-index → reveal position.
                // revealHistoryIndices maps turn-key → last-move-index snapshotted by the ViewModel
                // at the exact moment the reveal appeared, so doubles are handled correctly.
                val indexToReveal: Map<Int, Int> = revealHistoryIndices
                    .mapNotNull { (turnKey, histIdx) ->
                        mrXRevealedPositions[turnKey]?.let { pos -> histIdx to pos }
                    }
                    .toMap()

                val startIdx = maxOf(0, moveHistory.size - 24)
                val rows = (startIdx until moveHistory.size).toList().chunked(3)

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEachIndexed { rowIndex, rowIndices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowIndices.forEachIndexed { colIndex, globalIndex ->
                                val ticket = moveHistory[globalIndex]
                                val moveNumber = globalIndex + 1
                                val revealedPos = indexToReveal[globalIndex]
                                val isRevealEntry = revealedPos != null

                                Text(
                                    text = "$moveNumber.",
                                    fontSize = 9.sp,
                                    color = if (isRevealEntry) TextPrimary else TextMuted,
                                    fontWeight = if (isRevealEntry) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.width(20.dp),
                                    textAlign = TextAlign.End
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    MrXHistoryTicketChip(
                                        ticket = ticket,
                                        revealedPos = revealedPos,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (colIndex < rowIndices.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(10.dp)
                                    )
                                }
                            }

                            repeat(3 - rowIndices.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MrXHistoryTicketChip(
    ticket: String,
    modifier: Modifier = Modifier,
    revealedPos: Int? = null
) {
    val (color, label) = when (ticket) {
        "WALKING" -> Pair(WalkingColor, stringResource(R.string.ticket_walking))
        "ESCOOTER" -> Pair(EScooterColor, stringResource(R.string.ticket_escooter))
        "CARSHARING" -> Pair(CarSharingColor, stringResource(R.string.ticket_car_sharing))
        "BLACK" -> Pair(BlackColor, stringResource(R.string.ticket_black))
        else -> Pair(Color.Gray, ticket)
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .border(1.dp, color, RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        if (revealedPos != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .size(20.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$revealedPos",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RevealBanner(visible: Boolean, position: Int, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.88f, animationSpec = tween(300)),
        exit  = fadeOut(tween(300)) + scaleOut(targetScale = 0.88f, animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xCC0D1E2E), RoundedCornerShape(12.dp))
                .border(2.dp, AccentGlow, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.banner_mrx_position_reveal, position),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
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
    currentPlayerColor: Color? = null,
    onNavigateToSettings: () -> Unit,
    onMrXHistoryClick: () -> Unit = {},
    onTicketSelect: (TicketType) -> Unit
) {
    val visibleTickets = if (isMrX)
        listOf(TicketType.WALKING, TicketType.ESCOOTER, TicketType.CARSHARING, TicketType.BLACK, TicketType.DOUBLE)
    else
        listOf(TicketType.WALKING, TicketType.ESCOOTER, TicketType.CARSHARING)

    Column(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row: Menu and Round Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MenuButton(onClick = onNavigateToSettings)
            RoundCounter(current = currentRound, total = totalRounds)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Role label
        Text(
            text = if (isMrX) stringResource(R.string.role_mrx) else stringResource(R.string.role_detective),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = when {
                isMrX -> MrXPurple
                currentPlayerColor != null -> currentPlayerColor
                else -> AccentGlow
            },
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

        if (!isMrX) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A2E40), RoundedCornerShape(8.dp))
                    .border(1.dp, SidebarBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onMrXHistoryClick)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.title_mrx_moves),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MrXPurple,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Selected ticket hint
        if (selectedTicket != null) {
            val style = TicketStyleProvider.fromType(selectedTicket)
            Text(
                text = stringResource(R.string.hint_ticket_selected, style.label),
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
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.title_settings),
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
                text = stringResource(R.string.title_round),
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
                    text = if (count == Int.MAX_VALUE) stringResource(R.string.char_unlimited) else count.toString(),
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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val vpW = constraints.maxWidth.toFloat()
        val vpH = constraints.maxHeight.toFloat()

        // minScale = 1 so the map always fills (stretches to) the container at minimum zoom
        val minScale = 1f
        var scale  by remember(vpW, vpH) { mutableFloatStateOf(minScale) }
        var offset by remember(vpW, vpH) { mutableStateOf(Offset.Zero) }

        val dispMaxX = ((vpW * scale) / 2f - vpW / 2f).coerceAtLeast(0f)
        val dispMaxY = ((vpH * scale) / 2f - vpH / 2f).coerceAtLeast(0f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(vpW, vpH) {
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
                                    val newScale = (scale * zoomChange).coerceIn(minScale, 5f)
                                    val maxX = ((vpW * newScale) / 2f - vpW / 2f).coerceAtLeast(0f)
                                    val maxY = ((vpH * newScale) / 2f - vpH / 2f).coerceAtLeast(0f)
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
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x.coerceIn(-dispMaxX, dispMaxX),
                        translationY = offset.y.coerceIn(-dispMaxY, dispMaxY)
                    )
            ) {
                GameBoardCanvas(
                    modifier = Modifier.fillMaxSize(),
                    displayMode = displayMode,
                    playerPositions = playerPositions,
                    highlightedNodes = highlightedNodes,
                    onNodeClick = onNodeClick
                )
            }

            Text(
                text = stringResource(R.string.hint_usability),
                fontSize = 10.sp,
                color = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

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
            totalRounds = 22,
            currentPlayerColor = Color(0xFF2196F3)
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
            totalRounds = 22,
            currentPlayerColor = Color(0xFF2C2C2C)
        )
    }
}
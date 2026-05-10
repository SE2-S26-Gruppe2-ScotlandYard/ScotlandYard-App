package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.aau.serg.scotlandyard.model.BoardData
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme


const val BOARD_WIDTH_DP = 900f
const val BOARD_HEIGHT_DP = 720f

private const val NODE_RADIUS = 11f
private const val LABEL_SIZE = 14f

// Edge stroke widths per transport type
private const val WALK_STROKE = 1.8f
private const val SCOOT_STROKE = 2.8f
private const val CAR_STROKE = 3.5f
private const val BLACK_STROKE = 2.5f

@Composable
fun GameBoardCanvas(
    modifier: Modifier = Modifier,
    highlightedNodes: Set<Int> = emptySet(),
    highlightedEdgeTargets: Set<Int> = emptySet(),
    playerPositions: Map<Color, Int> = emptyMap()
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    val positions = remember(canvasWidth, canvasHeight) {
        if (canvasWidth == 0f || canvasHeight == 0f) emptyMap()
        else BoardData.nodePositions.mapValues { (_, pos) ->
            Offset(
                x = pos.first * canvasWidth,
                y = pos.second * canvasHeight
            )
        }
    }

    Canvas(
        modifier = modifier
            .size(BOARD_WIDTH_DP.dp, BOARD_HEIGHT_DP.dp)
            .background(CanvasBgColor)
    ) {
        if (size.width != canvasWidth || size.height != canvasHeight) {
            canvasWidth = size.width
            canvasHeight = size.height
        }

        if (positions.isEmpty()) return@Canvas

        drawEdges(positions, highlightedEdgeTargets)

        drawNodes(positions, highlightedNodes)

        drawPlayers(positions, playerPositions)
    }
}

private fun DrawScope.drawEdges(
    positions: Map<Int, Offset>,
    highlightedTargets: Set<Int>
) {
    for (edge in BoardData.edges) {
        val a = positions[edge.from] ?: continue
        val b = positions[edge.to] ?: continue

        val isHighlighted = edge.to in highlightedTargets || edge.from in highlightedTargets

        val (color, strokeWidth) = when (edge.transport) {
            TicketType.WALKING -> Pair(WalkingColor, WALK_STROKE)
            TicketType.ESCOOTER -> Pair(EScooterColor, SCOOT_STROKE)
            TicketType.CARSHARING -> Pair(CarSharingColor, CAR_STROKE)
            else -> Pair(BlackColor, BLACK_STROKE)
        }

        val offset = parallelOffset(a, b, edge.transport)

        drawLine(
            color = if (isHighlighted) color.copy(alpha = 1f) else color.copy(alpha = 0.55f),
            start = a + offset,
            end = b + offset,
            strokeWidth = if (isHighlighted) strokeWidth * 1.6f else strokeWidth
        )
    }
}

private fun parallelOffset(a: Offset, b: Offset, type: TicketType): Offset {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
    // Perpendicular unit vector
    val px = -dy / len
    val py = dx / len

    val shift = when (type) {
        TicketType.WALKING -> 0f
        TicketType.ESCOOTER -> 3.5f
        TicketType.CARSHARING -> -3.5f
        TicketType.BLACK -> 7f
        else -> 0f
    }
    return Offset(px * shift, py * shift)
}

private fun DrawScope.drawNodes(
    positions: Map<Int, Offset>,
    highlighted: Set<Int>
) {
    drawIntoCanvas { canvas ->
        val textPaint = android.graphics.Paint().apply {
            color = TextPrimary.toArgb()
            textSize = LABEL_SIZE
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        for ((id, pos) in positions) {
            val isHighlighted = id in highlighted
            val transports = BoardData.nodeTransports[id] ?: emptySet()

            val hasWalking = TicketType.WALKING in transports
            val hasEScooter = TicketType.ESCOOTER in transports
            val hasCarSharing = TicketType.CARSHARING in transports
            val hasBlack = TicketType.BLACK in transports

            if (isHighlighted) {
                drawCircle(
                    color = Color(0xFF22AA80).copy(alpha = 0.4f),
                    radius = NODE_RADIUS + 6f,
                    center = pos
                )
            }

            val circlePath = Path().apply {
                addOval(
                    Rect(
                        center = pos,
                        radius = NODE_RADIUS
                    )
                )
            }

            if (isHighlighted) {
                // Highlighted: solid accent fill, skip transport colours
                drawCircle(color = Color(0xFF22AA80), radius = NODE_RADIUS, center = pos)
            } else {
                clipPath(circlePath) {
                    drawRect(
                        color = if (hasWalking) WalkingColor else NodeFill,
                        topLeft = Offset(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS),
                        size = Size(NODE_RADIUS * 2f, NODE_RADIUS)   // top half
                    )
                    drawRect(
                        color = if (hasEScooter) EScooterColor else WalkingColor,
                        topLeft = Offset(pos.x - NODE_RADIUS, pos.y),
                        size = Size(NODE_RADIUS * 2f, NODE_RADIUS)   // bottom half
                    )
                }

                if (hasCarSharing) {
                    val bandH = NODE_RADIUS * 0.55f
                    val bandW = NODE_RADIUS * 1.6f
                    clipPath(circlePath) {
                        drawRect(
                            color = CarSharingColor,
                            topLeft = Offset(pos.x - bandW / 2f, pos.y - bandH / 2f),
                            size = Size(bandW, bandH)
                        )
                    }
                }

                drawCircle(
                    color = AccentTeal,
                    radius = NODE_RADIUS,
                    center = pos,
                    style  = Stroke(width = 1.2f)
                )

                if (hasBlack) {
                    drawCircle(
                        color = BlackColor,
                        radius = NODE_RADIUS * 0.35f,
                        center = Offset(pos.x + NODE_RADIUS * 0.6f, pos.y - NODE_RADIUS * 0.6f)
                    )
                }
            }

            if (isHighlighted) {
                drawCircle(
                    color = Color(0xFF88FFCC),
                    radius = NODE_RADIUS,
                    center = pos,
                    style  = Stroke(width = 2f)
                )
            }

            canvas.nativeCanvas.drawText(
                id.toString(),
                pos.x,
                pos.y + LABEL_SIZE * 0.38f,
                textPaint
            )
        }
    }
}

private fun DrawScope.drawPlayers(
    positions: Map<Int, Offset>,
    players: Map<Color, Int>
) {
    players.entries.forEachIndexed { index, (color, stationId) ->
        val pos = positions[stationId] ?: return@forEachIndexed
        // Stack multiple player slightly offset if on same station
        val stackOffset = Offset(x = index * 5f, y = -index * 5f)
        val tokenPos = pos + stackOffset + Offset(0f, -(NODE_RADIUS + 8f))

        // Outer circle
        drawCircle(color = Color.White, radius = 10f, center = tokenPos)
        drawCircle(color = color, radius = 8f, center = tokenPos)
    }
}

// Preview
@Preview(showBackground = true, widthDp = 900, heightDp = 720)
@Composable
private fun GameBoardCanvasPreview() {
    ScotlandYardTheme {
        GameBoardCanvas(
            highlightedNodes = setOf(1, 8, 9, 46, 58),
            playerPositions = mapOf(
                Color(0xFF22AA80) to 1,   // detective (teal)
                Color(0xFF333333) to 46   // Mr. X (dark)
            )
        )
    }
}
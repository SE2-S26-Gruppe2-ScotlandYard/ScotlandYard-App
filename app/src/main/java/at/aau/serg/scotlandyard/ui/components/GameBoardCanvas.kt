package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.aau.serg.scotlandyard.model.BoardData
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme


private val BgColor = Color(0xFF0F2235)
private val WalkingColor = Color(0xFFD4B963)
private val EScooterColor = Color(0xFF3D8E79)
private val CarSharingColor = Color(0xFFA67C65)
private val BlackColor = Color(0xFFCC44CC)
private val NodeFill = Color(0xFF1A3A55)
private val NodeStroke = Color(0xFF4A9ECC)
private val NodeTextColor = Color(0xFFE8EEF4)


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
    Canvas(
        modifier = modifier
            .size(BOARD_WIDTH_DP.dp, BOARD_HEIGHT_DP.dp)
            .background(BgColor)
    ) {
        val positions = BoardData.nodePositions.mapValues { (_, pos) ->
            Offset(
                x = pos.first * size.width,
                y = pos.second * size.height
            )
        }

        // 1. Edges (drawn below nodes so nodes sit on top)
        drawEdges(positions, highlightedEdgeTargets)

        // 2. Station nodes + labels
        drawNodes(positions, highlightedNodes)

        // 3. Player tokens
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
        TicketType.CARSHARING -> 7f
        TicketType.BLACK -> -3.5f
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
            color = NodeTextColor.toArgb()
            textSize = LABEL_SIZE
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        for ((id, pos) in positions) {
            val isHighlighted = id in highlighted

            // Shadow / glow for highlighted nodes
            if (isHighlighted) {
                drawCircle(
                    color = Color(0xFF22AA80).copy(alpha = 0.4f),
                    radius = NODE_RADIUS + 6f,
                    center = pos
                )
            }

            // Node fill
            drawCircle(
                color = if (isHighlighted) Color(0xFF22AA80) else NodeFill,
                radius = NODE_RADIUS,
                center = pos
            )

            // Node border
            drawCircle(
                color = if (isHighlighted) Color(0xFF88FFCC) else NodeStroke,
                radius = NODE_RADIUS,
                center = pos,
            )

            // Station number label
            canvas.nativeCanvas.drawText(
                id.toString(),
                pos.x,
                pos.y + LABEL_SIZE * 0.38f,  // vertical center correction
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
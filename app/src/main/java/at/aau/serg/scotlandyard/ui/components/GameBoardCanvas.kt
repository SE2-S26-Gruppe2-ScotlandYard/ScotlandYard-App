package at.aau.serg.scotlandyard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.aau.serg.scotlandyard.model.BoardData
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.model.TicketType
import at.aau.serg.scotlandyard.ui.theme.*
import at.aau.serg.scotlandyard.ui.theme.ScotlandYardTheme
import com.example.scotlandyard.R

// Board width:height has been tested in 5:4 aspect ratio
const val BOARD_WIDTH_DP = 900f
const val BOARD_HEIGHT_DP = 720f

// Relative size factors (based on min canvas dimension)
private const val NODE_RADIUS_FACTOR = 0.015f
private const val LABEL_SIZE_FACTOR = 0.019f
private const val PLAYER_RADIUS_OUTER_FACTOR = 0.014f
private const val PLAYER_RADIUS_INNER_FACTOR = 0.011f

private const val WALK_STROKE_FACTOR = 0.0025f
private const val SCOOT_STROKE_FACTOR = 0.0039f
private const val CAR_STROKE_FACTOR = 0.0049f
private const val BLACK_STROKE_FACTOR = 0.0035f

private const val WALK_OFFSET_FACTOR = 0f
private const val SCOOT_OFFSET_FACTOR = 0.0049f
private const val CAR_OFFSET_FACTOR = -0.0053f
private const val BLACK_OFFSET_FACTOR = 0.0097f

private const val HIGHLIGHT_RADIUS_OFFSET_FACTOR = 0.0083f

@Composable
fun GameBoardCanvas(
    modifier: Modifier = Modifier,
    displayMode: BoardDisplayMode = BoardDisplayMode.GRAPH,
    highlightedNodes: Set<Int> = emptySet(),
    highlightedEdgeTargets: Set<Int> = emptySet(),
    playerPositions: Map<Color, Int> = emptyMap()
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var minDimension by remember { mutableFloatStateOf(0f) }

    val positions = remember(canvasWidth, canvasHeight) {
        if (canvasWidth == 0f || canvasHeight == 0f) emptyMap()
        else BoardData.nodePositions.mapValues { (_, pos) ->
            Offset(
                x = pos.first * canvasWidth,
                y = pos.second * canvasHeight
            )
        }
    }

    // In MAP mode: stack a background image below the transparent Canvas.
    Box(
        modifier = modifier
            .size(BOARD_WIDTH_DP.dp, BOARD_HEIGHT_DP.dp)
    ) {
        val backgroundImage = when (displayMode) {
            BoardDisplayMode.MAP -> R.drawable.map
            BoardDisplayMode.GRAPH -> R.drawable.background
        }
        val backgroundAlpha = when (displayMode) {
            BoardDisplayMode.MAP -> 1f
            BoardDisplayMode.GRAPH -> 0.3f
        }

        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                /*.then(
                    if (displayMode == BoardDisplayMode.GRAPH)
                        Modifier.background(CanvasBgColor)
                    else
                        Modifier
                )*/
        ) {
            if (size.width != canvasWidth || size.height != canvasHeight) {
                canvasWidth = size.width
                canvasHeight = size.height
                minDimension = minOf(size.width, size.height)
            }

            if (positions.isEmpty()) return@Canvas

            // Edges only in GRAPH mode
            if (displayMode == BoardDisplayMode.GRAPH) {
                drawEdges(positions, highlightedEdgeTargets, minDimension)
            }

            drawNodes(positions, highlightedNodes, minDimension)

            drawPlayers(positions, playerPositions, minDimension)
        }
    }
}

private fun DrawScope.drawEdges(
    positions: Map<Int, Offset>,
    highlightedTargets: Set<Int>,
    minDimension: Float
) {
    val walkStroke = WALK_STROKE_FACTOR * minDimension
    val scootStroke = SCOOT_STROKE_FACTOR * minDimension
    val carStroke = CAR_STROKE_FACTOR * minDimension
    val blackStroke = BLACK_STROKE_FACTOR * minDimension

    val walkOffset = WALK_OFFSET_FACTOR * minDimension
    val scootOffset = SCOOT_OFFSET_FACTOR * minDimension
    val carOffset = CAR_OFFSET_FACTOR * minDimension
    val blackOffset = BLACK_OFFSET_FACTOR * minDimension

    for (edge in BoardData.edges) {
        val a = positions[edge.from] ?: continue
        val b = positions[edge.to] ?: continue

        val isHighlighted = edge.to in highlightedTargets || edge.from in highlightedTargets

        val (color, strokeWidth, offset) = when (edge.transport) {
            TicketType.WALKING   -> Triple(WalkingColor,    walkStroke,  walkOffset)
            TicketType.ESCOOTER  -> Triple(EScooterColor,  scootStroke, scootOffset)
            TicketType.CARSHARING -> Triple(CarSharingColor, carStroke,  carOffset)
            else                 -> Triple(BlackColor,      blackStroke, blackOffset)
        }

        val finalOffset = parallelOffset(a, b, offset)

        drawLine(
            color = if (isHighlighted) color.copy(alpha = 1f) else color.copy(alpha = 0.55f),
            start = a + finalOffset,
            end = b + finalOffset,
            strokeWidth = if (isHighlighted) strokeWidth * 1.6f else strokeWidth
        )
    }
}

private fun parallelOffset(a: Offset, b: Offset, shiftAmount: Float): Offset {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.01f)
    // Perpendicular unit vector
    val px = -dy / len
    val py = dx / len

    return Offset(px * shiftAmount, py * shiftAmount)
}

private fun DrawScope.drawNodes(
    positions: Map<Int, Offset>,
    highlighted: Set<Int>,
    minDimension: Float
) {
    val nodeRadius = NODE_RADIUS_FACTOR * minDimension
    val labelSize = LABEL_SIZE_FACTOR * minDimension
    val highlightOffset = HIGHLIGHT_RADIUS_OFFSET_FACTOR * minDimension

    drawIntoCanvas { canvas ->
        val textPaint = android.graphics.Paint().apply {
            color = TextPrimary.toArgb()
            textSize = labelSize
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
                    radius = nodeRadius + highlightOffset,
                    center = pos
                )
            }

            val circlePath = Path().apply {
                addOval(
                    Rect(
                        center = pos,
                        radius = nodeRadius
                    )
                )
            }

            if (isHighlighted) {
                // Highlighted: solid accent fill, skip transport colours
                drawCircle(color = Color(0xFF22AA80), radius = nodeRadius, center = pos)
            } else {
                clipPath(circlePath) {
                    drawRect(
                        color = if (hasWalking) WalkingColor else NodeFill,
                        topLeft = Offset(pos.x - nodeRadius, pos.y - nodeRadius),
                        size = Size(nodeRadius * 2f, nodeRadius)   // top half
                    )
                    drawRect(
                        color = if (hasEScooter) EScooterColor else WalkingColor,
                        topLeft = Offset(pos.x - nodeRadius, pos.y),
                        size = Size(nodeRadius * 2f, nodeRadius)   // bottom half
                    )
                }

                if (hasCarSharing) {
                    val bandH = nodeRadius * 0.55f
                    val bandW = nodeRadius * 1.6f
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
                    radius = nodeRadius,
                    center = pos,
                    style = Stroke(width = minDimension * 0.0017f)
                )

                if (hasBlack) {
                    drawCircle(
                        color = BlackColor,
                        radius = nodeRadius * 0.35f,
                        center = Offset(pos.x + nodeRadius * 0.6f, pos.y - nodeRadius * 0.6f)
                    )
                }
            }

            if (isHighlighted) {
                drawCircle(
                    color = Color(0xFF88FFCC),
                    radius = nodeRadius,
                    center = pos,
                    style = Stroke(width = minDimension * 0.0028f)
                )
            }

            canvas.nativeCanvas.drawText(
                id.toString(),
                pos.x,
                pos.y + labelSize * 0.38f,
                textPaint
            )
        }
    }
}

private fun DrawScope.drawPlayers(
    positions: Map<Int, Offset>,
    players: Map<Color, Int>,
    minDimension: Float
) {
    val outerRadius = PLAYER_RADIUS_OUTER_FACTOR * minDimension
    val innerRadius = PLAYER_RADIUS_INNER_FACTOR * minDimension
    val stackSpacing = minDimension * 0.007f

    players.entries.forEachIndexed { index, (color, stationId) ->
        val pos = positions[stationId] ?: return@forEachIndexed
        // Stack multiple player slightly offset if on same station
        val stackOffset = Offset(x = index * stackSpacing, y = -index * stackSpacing)
        val tokenPos = pos + stackOffset + Offset(0f, -(nodeRadius + 8f))

        // Outer circle
        drawCircle(color = color, radius = outerRadius, center = tokenPos)
        drawCircle(color = color, radius = innerRadius, center = tokenPos)
    }
}

private val DrawScope.nodeRadius: Float
    get() = NODE_RADIUS_FACTOR * minOf(size.width, size.height)

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
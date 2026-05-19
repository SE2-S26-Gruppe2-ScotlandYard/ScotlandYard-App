package at.aau.serg.scotlandyard.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- public constants used by tests / other composables ----------

/** Height of a single wheel item in dp. */
const val WHEEL_ITEM_HEIGHT_DP = 60

/** Number of items visible in the wheel at once. */
const val WHEEL_VISIBLE_ITEMS = 5

/** How many times the positions list is repeated for a smooth multi-rotation effect. */
const val WHEEL_REPEAT_COUNT = 7

/**
 * A vertical spin-wheel picker for selecting a start position.
 *
 * **Normal mode** (`isCheatMode = false`):
 *  - When [triggerSpin] flips to `true` the wheel animates for ~3.5 s and
 *    decelerates to stop exactly on [targetPosition].
 *  - After the animation [onSpinComplete] is called.
 *
 * **Cheat mode** (`isCheatMode = true`):
 *  - The wheel is scrollable by the user (snap-to-item).
 *  - Every time the centered item changes [onSelectionChanged] is called with the new value.
 *
 * @param positions        Ordered list of valid positions (e.g. 1..200).
 * @param targetPosition   Position to land on in auto-spin; initial selection in cheat mode.
 * @param isCheatMode      `true` → manual; `false` → auto-spin.
 * @param triggerSpin      Flip from `false` to `true` to start the auto-spin animation.
 * @param onSpinComplete   Called once the auto-spin animation finishes.
 * @param onSelectionChanged Called with the currently centred position (cheat mode only).
 */
@Composable
fun SpinnerWheelPicker(
    positions: List<Int>,
    targetPosition: Int,
    isCheatMode: Boolean,
    triggerSpin: Boolean,
    onSpinComplete: () -> Unit,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val half = WHEEL_VISIBLE_ITEMS / 2

    // Repeat the list WHEEL_REPEAT_COUNT times so the animation can spin multiple rotations
    val extendedItems = remember(positions) {
        List(WHEEL_REPEAT_COUNT) { positions }.flatten()
    }

    // Initial scroll: place position-list start (index 0) at the top of the 3rd repetition,
    // centred in the viewport.
    val initialFirst = (positions.size * 2 - half).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialFirst)
    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)

    // Exact pixel height of one wheel item (density-aware)
    val itemHeightPx = with(LocalDensity.current) { WHEEL_ITEM_HEIGHT_DP.dp.toPx() }

    // ── Auto-spin animation ──────────────────────────────────────────────────────────────────
    LaunchedEffect(triggerSpin, targetPosition) {
        if (triggerSpin && !isCheatMode) {
            val targetIdx = positions.indexOf(targetPosition).coerceAtLeast(0)
            // Land in the 6th repetition so we always scroll forward 3-4 rotations
            val targetFirst = (positions.size * 5 + targetIdx - half).coerceAtLeast(0)
            val currentFirst = listState.firstVisibleItemIndex
            val pixelsToScroll = (targetFirst - currentFirst) * itemHeightPx

            listState.animateScrollBy(
                value = pixelsToScroll,
                animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
            )
            // Force exact snap so the displayed number always matches targetPosition
            listState.scrollToItem(targetFirst)
            onSpinComplete()
        }
    }

    // ── Cheat-mode: jump to targetPosition when mode first activates ─────────────────────────
    LaunchedEffect(isCheatMode) {
        if (isCheatMode) {
            val targetIdx = positions.indexOf(targetPosition).coerceAtLeast(0)
            val jumpTo = (positions.size * 3 + targetIdx - half).coerceAtLeast(0)
            listState.scrollToItem(jumpTo)
        }
    }

    // ── Track centred item and report selection changes ──────────────────────────────────────
    val centredPosition by remember {
        derivedStateOf {
            val centredIdx = listState.firstVisibleItemIndex + half
            if (centredIdx < extendedItems.size) extendedItems[centredIdx]
            else positions.last()
        }
    }

    LaunchedEffect(centredPosition) {
        if (isCheatMode) onSelectionChanged(centredPosition)
    }

    // ── Render ───────────────────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier.height((WHEEL_ITEM_HEIGHT_DP * WHEEL_VISIBLE_ITEMS).dp)
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = if (isCheatMode) snapFling else ScrollableDefaults.flingBehavior(),
            userScrollEnabled = isCheatMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(extendedItems.size) { index ->
                val position = extendedItems[index]
                val isCenter = index == listState.firstVisibleItemIndex + half
                WheelItem(position = position, isSelected = isCenter, isCheatMode = isCheatMode)
            }
        }

        // Top & bottom fade overlays for depth effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((WHEEL_ITEM_HEIGHT_DP * half).dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((WHEEL_ITEM_HEIGHT_DP * half).dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )

        // Centre selection box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WHEEL_ITEM_HEIGHT_DP.dp)
                .align(Alignment.Center)
                .border(width = 2.dp, color = if (isCheatMode) Color(0xFFFF6B00) else Color.White.copy(alpha = 0.7f))
                .background(Color.White.copy(alpha = 0.07f))
        )
    }
}

// ── Private sub-composables ──────────────────────────────────────────────────────────────────

@Composable
private fun WheelItem(
    position: Int,
    isSelected: Boolean,
    isCheatMode: Boolean
) {
    val alpha = if (isSelected) 1f else 0.35f
    val fontSize = if (isSelected) 30.sp else 20.sp
    val selectedColor = if (isCheatMode) Color(0xFFFF6B00) else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WHEEL_ITEM_HEIGHT_DP.dp)
            .alpha(alpha)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            fontSize = fontSize,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isSelected) selectedColor else Color.White
        )
    }
}


package at.aau.serg.scotlandyard.ui.components

import at.aau.serg.scotlandyard.model.StartPositionConstants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for SpinnerWheelPicker logic.
 *
 * The composable itself cannot be rendered in JVM tests, but all the
 * pure-logic aspects (index mapping, position lookups, scroll-target
 * calculation, constants) are covered here.
 */
class SpinnerWheelPickerTest {

    private val positions = StartPositionConstants.VALID_POSITIONS  // 1..200

    // ── Constants ────────────────────────────────────────────────────────────────────────────

    @Test
    fun itemHeight_is_positive() {
        assertTrue(WHEEL_ITEM_HEIGHT_DP > 0)
    }

    @Test
    fun visibleItems_is_odd_so_center_is_unambiguous() {
        assertTrue(WHEEL_VISIBLE_ITEMS % 2 == 1,
            "WHEEL_VISIBLE_ITEMS should be odd so exactly one item is centred")
    }

    @Test
    fun repeatCount_produces_enough_items_for_multirotation_animation() {
        // We need at least 5 repetitions so the animation can spin 3-4 rotations
        assertTrue(WHEEL_REPEAT_COUNT >= 5,
            "WHEEL_REPEAT_COUNT must be at least 5 for smooth multi-rotation spin")
    }

    // ── Position list sanity ─────────────────────────────────────────────────────────────────

    @Test
    fun positions_list_has_200_items() {
        assertEquals(200, positions.size)
    }

    @Test
    fun positions_first_is_1() {
        assertEquals(1, positions.first())
    }

    @Test
    fun positions_last_is_200() {
        assertEquals(200, positions.last())
    }

    // ── Index-to-position mapping (same logic used in wheel) ─────────────────────────────────

    @Test
    fun extendedList_maps_index_to_correct_position_first_repetition() {
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()
        assertEquals(positions[0], extended[0])
        assertEquals(positions[1], extended[1])
        assertEquals(positions[199], extended[199])
    }

    @Test
    fun extendedList_maps_index_to_correct_position_second_repetition() {
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()
        assertEquals(positions[0], extended[200])
        assertEquals(positions[99], extended[299])
    }

    @Test
    fun extendedList_wraps_via_modulo() {
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()
        for (i in extended.indices) {
            val expected = positions[i % positions.size]
            assertEquals(expected, extended[i], "Index $i should map to ${expected}")
        }
    }

    @Test
    fun extendedList_total_size_equals_repeat_times_positions() {
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()
        assertEquals(WHEEL_REPEAT_COUNT * positions.size, extended.size)
    }

    // ── Auto-spin target calculation ─────────────────────────────────────────────────────────

    @Test
    fun autoSpin_targetFirst_is_within_extended_bounds_for_all_valid_positions() {
        val half = WHEEL_VISIBLE_ITEMS / 2
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()

        for (target in positions) {
            val targetIdx = positions.indexOf(target).coerceAtLeast(0)
            // Target in 6th repetition (index 5, 0-based)
            val targetFirst = (positions.size * 5 + targetIdx - half).coerceAtLeast(0)
            assertTrue(targetFirst < extended.size,
                "targetFirst=$targetFirst must be within extended list for position=$target")
        }
    }

    @Test
    fun autoSpin_targetFirst_is_always_positive() {
        val half = WHEEL_VISIBLE_ITEMS / 2
        for (target in positions) {
            val targetIdx = positions.indexOf(target).coerceAtLeast(0)
            val targetFirst = (positions.size * 5 + targetIdx - half).coerceAtLeast(0)
            assertTrue(targetFirst >= 0)
        }
    }

    @Test
    fun autoSpin_centred_item_at_targetFirst_plus_half_matches_target() {
        val half = WHEEL_VISIBLE_ITEMS / 2
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()

        val testPositions = listOf(1, 50, 100, 150, 200)
        for (target in testPositions) {
            val targetIdx = positions.indexOf(target).coerceAtLeast(0)
            val targetFirst = (positions.size * 5 + targetIdx - half).coerceAtLeast(0)
            val centredAbsIdx = targetFirst + half
            val positionAtCentre = extended[centredAbsIdx]
            assertEquals(target, positionAtCentre,
                "After spinning, centred item should be $target but was $positionAtCentre")
        }
    }

    // ── Cheat-mode initial position ───────────────────────────────────────────────────────────

    @Test
    fun cheatMode_jumpTo_is_within_bounds_for_all_valid_positions() {
        val half = WHEEL_VISIBLE_ITEMS / 2
        val extended = List(WHEEL_REPEAT_COUNT) { positions }.flatten()

        for (target in positions) {
            val targetIdx = positions.indexOf(target).coerceAtLeast(0)
            val jumpTo = (positions.size * 3 + targetIdx - half).coerceAtLeast(0)
            assertTrue(jumpTo < extended.size,
                "jumpTo=$jumpTo must be within extended list for position=$target")
        }
    }

    // ── Valid-position checks (delegates to StartPositionConstants) ───────────────────────────

    @Test
    fun position_1_is_valid() {
        assertTrue(StartPositionConstants.isValid(1))
    }

    @Test
    fun position_200_is_valid() {
        assertTrue(StartPositionConstants.isValid(200))
    }

    @Test
    fun position_0_is_invalid() {
        assertFalse(StartPositionConstants.isValid(0))
    }

    @Test
    fun position_201_is_invalid() {
        assertFalse(StartPositionConstants.isValid(201))
    }
}


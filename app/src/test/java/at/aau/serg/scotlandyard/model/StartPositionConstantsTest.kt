package at.aau.serg.scotlandyard.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StartPositionConstantsTest {

    @Test
    fun minPosition_is_1() {
        assertEquals(1, StartPositionConstants.MIN_POSITION)
    }

    @Test
    fun maxPosition_is_200() {
        assertEquals(200, StartPositionConstants.MAX_POSITION)
    }

    @Test
    fun validPositions_contains_exactly_200_entries() {
        assertEquals(200, StartPositionConstants.VALID_POSITIONS.size)
    }

    @Test
    fun validPositions_first_element_is_1() {
        assertEquals(1, StartPositionConstants.VALID_POSITIONS.first())
    }

    @Test
    fun validPositions_last_element_is_200() {
        assertEquals(200, StartPositionConstants.VALID_POSITIONS.last())
    }

    @Test
    fun validPositions_is_ordered_ascending() {
        val sorted = StartPositionConstants.VALID_POSITIONS.sorted()
        assertEquals(sorted, StartPositionConstants.VALID_POSITIONS)
    }

    @Test
    fun validPositions_contains_all_integers_1_to_200() {
        val expected = (1..200).toList()
        assertEquals(expected, StartPositionConstants.VALID_POSITIONS)
    }

    @Test
    fun isValid_returns_true_for_min_position() {
        assertTrue(StartPositionConstants.isValid(1))
    }

    @Test
    fun isValid_returns_true_for_max_position() {
        assertTrue(StartPositionConstants.isValid(200))
    }

    @Test
    fun isValid_returns_true_for_mid_position() {
        assertTrue(StartPositionConstants.isValid(100))
    }

    @Test
    fun isValid_returns_false_for_zero() {
        assertFalse(StartPositionConstants.isValid(0))
    }

    @Test
    fun isValid_returns_false_for_negative() {
        assertFalse(StartPositionConstants.isValid(-1))
    }

    @Test
    fun isValid_returns_false_for_201() {
        assertFalse(StartPositionConstants.isValid(201))
    }

    @Test
    fun validRange_covers_min_to_max() {
        assertEquals(StartPositionConstants.MIN_POSITION, StartPositionConstants.VALID_RANGE.first)
        assertEquals(StartPositionConstants.MAX_POSITION, StartPositionConstants.VALID_RANGE.last)
    }
}


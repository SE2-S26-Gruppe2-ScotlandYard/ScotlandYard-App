package at.aau.serg.scotlandyard.model

/**
 * Central definition of valid start positions for Scotland Yard.
 * All position-related constants are sourced from here – no duplication.
 */
object StartPositionConstants {
    const val MIN_POSITION = 1
    const val MAX_POSITION = 200

    val VALID_RANGE = MIN_POSITION..MAX_POSITION

    /** Ordered list of all valid starting positions (1 to 200). */
    val VALID_POSITIONS: List<Int> = VALID_RANGE.toList()

    /** Returns true if [position] is a valid start position. */
    fun isValid(position: Int): Boolean = position in VALID_RANGE
}


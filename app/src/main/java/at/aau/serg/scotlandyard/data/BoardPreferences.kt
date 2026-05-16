package at.aau.serg.scotlandyard.data

import android.content.Context
import androidx.core.content.edit
import at.aau.serg.scotlandyard.model.BoardDisplayMode

private const val PREFS_NAME = "scotland_yard_preferences"
private const val KEY_DISPLAY_MODE = "board_display_mode"

fun Context.getDisplayModePreference(): BoardDisplayMode {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val modeName = prefs.getString(KEY_DISPLAY_MODE, BoardDisplayMode.GRAPH.name)

    return try {
        BoardDisplayMode.valueOf(modeName ?: BoardDisplayMode.GRAPH.name)
    } catch (e: IllegalArgumentException) {
        BoardDisplayMode.GRAPH
    }
}

fun Context.saveDisplayModePreference(mode: BoardDisplayMode) {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    prefs.edit {
        putString(KEY_DISPLAY_MODE, mode.name)
    }
}
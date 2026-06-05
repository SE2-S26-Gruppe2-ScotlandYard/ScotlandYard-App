package at.aau.serg.scotlandyard.data

import android.content.Context
import androidx.core.content.edit
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import java.util.Locale

private const val PREFS_NAME = "scotland_yard_preferences"
private const val KEY_DISPLAY_MODE = "board_display_mode"
private const val KEY_LANGUAGE = "app_language"

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

fun Context.getLanguagePreference(): String {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
}

fun Context.saveLanguagePreference(languageCode: String) {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    prefs.edit {
        putString(KEY_LANGUAGE, languageCode)
    }
}

fun Context.applyLanguage(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)

    return createConfigurationContext(config)
}
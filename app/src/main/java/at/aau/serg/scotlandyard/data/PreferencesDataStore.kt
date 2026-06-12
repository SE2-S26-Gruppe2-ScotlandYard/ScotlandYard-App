package at.aau.serg.scotlandyard.data

import android.content.Context
import androidx.core.content.edit
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import at.aau.serg.scotlandyard.network.ServerConfig
import java.util.Locale

enum class ServerType { GLOBAL, LOCAL, DEVICE }

private const val PREFS_NAME = "scotland_yard_preferences"
private const val KEY_DISPLAY_MODE = "board_display_mode"
private const val KEY_LANGUAGE = "app_language"
private const val KEY_SERVER_URI_TYPE = "server_uri_type"
private const val KEY_SERVER_URI_CUSTOM = "server_uri_custom"

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

fun Context.getServerUriTypePreference(): String {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_SERVER_URI_TYPE, "LOCAL") ?: "LOCAL"
}

fun Context.saveServerUriTypePreference(type: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_SERVER_URI_TYPE, type)
    }
}

fun Context.getServerUriCustomPreference(): String {
    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_SERVER_URI_CUSTOM, "") ?: ""
}

fun Context.saveServerUriCustomPreference(uri: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_SERVER_URI_CUSTOM, uri)
    }
}
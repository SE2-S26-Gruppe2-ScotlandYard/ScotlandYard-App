package at.aau.serg.scotlandyard.data

import android.content.Context
import androidx.core.content.edit
import at.aau.serg.scotlandyard.model.BoardDisplayMode
import java.util.Locale

enum class ServerType { GLOBAL, LOCAL, DEVICE }

private const val PREFS_NAME = "scotland_yard_preferences"
private const val KEY_DISPLAY_MODE = "board_display_mode"
private const val KEY_LANGUAGE = "app_language"
private const val KEY_SERVER_URI_TYPE = "server_uri_type"
private const val KEY_SERVER_URI_CUSTOM = "server_uri_custom"
private const val KEY_USER_ID = "user_id"
private const val KEY_USER_NICKNAME = "user_nickname"
private const val KEY_LOBBY_ID = "lobby_id"
private const val KEY_GAME_ID = "game_id"

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
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_DISPLAY_MODE, mode.name)
    }
}

fun Context.getLanguagePreference(): String {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, "en") ?: "en"
}

fun Context.saveLanguagePreference(languageCode: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
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
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_SERVER_URI_TYPE, "GLOBAL") ?: "GLOBAL"
}

fun Context.saveServerUriTypePreference(type: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_SERVER_URI_TYPE, type)
    }
}

fun Context.getServerUriCustomPreference(): String {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_SERVER_URI_CUSTOM, "") ?: ""
}

fun Context.saveServerUriCustomPreference(uri: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_SERVER_URI_CUSTOM, uri)
    }
}

// ── Session Storage für Game State Recovery ───────────────────────────────────

fun Context.saveUserSession(userId: String, nickname: String) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_USER_ID, userId)
        putString(KEY_USER_NICKNAME, nickname)
    }
}

fun Context.getUserId(): String? =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_ID, null)

fun Context.getUserNickname(): String? =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_NICKNAME, null)

fun Context.saveLobbyId(lobbyId: String?) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        if (lobbyId != null) putString(KEY_LOBBY_ID, lobbyId)
        else remove(KEY_LOBBY_ID)
    }
}

fun Context.getLobbyId(): String? =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LOBBY_ID, null)

fun Context.saveGameId(gameId: String?) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        if (gameId != null) putString(KEY_GAME_ID, gameId)
        else remove(KEY_GAME_ID)
    }
}

fun Context.getGameId(): String? =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_GAME_ID, null)

fun Context.clearSession() {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        remove(KEY_USER_ID)
        remove(KEY_USER_NICKNAME)
        remove(KEY_LOBBY_ID)
        remove(KEY_GAME_ID)

        remove(KEY_PLAYER_ID)
        remove(KEY_IS_MRX)
    }
}
private const val KEY_PLAYER_ID = "player_id"
private const val KEY_IS_MRX = "is_mrx"

fun Context.savePlayerInfo(playerId: String, isMrX: Boolean) {
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(KEY_PLAYER_ID, playerId)
        putBoolean(KEY_IS_MRX, isMrX)
    }
}

fun Context.getPlayerId(): String? =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_PLAYER_ID, null)

fun Context.getIsMrX(): Boolean =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_MRX, false)

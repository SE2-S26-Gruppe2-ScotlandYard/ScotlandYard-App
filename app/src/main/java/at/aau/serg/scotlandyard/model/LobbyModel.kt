package at.aau.serg.scotlandyard.model

import org.json.JSONObject

data class LobbyUserData(
    val id: String,
    val name: String
)

data class LobbyData(
    val id: String,
    val name: String,
    val hostId: String,
    val isStarted: Boolean,
    val users: List<LobbyUserData>,
    val readyStatus: Map<String, Boolean>,
    val selectedRoles: Map<String, String>
)

data class LobbyResponse(
    val success: Boolean,
    val message: String,
    val lobbyId: String?,
    val lobby: LobbyData?
)

fun JSONObject.toLobbyResponse(): LobbyResponse {
    val success = getBoolean("success")
    val message = getString("message")
    val lobbyId = optString("lobbyId").takeIf { it.isNotBlank() }
    val lobby   = if (has("lobby") && !isNull("lobby"))
        getJSONObject("lobby").toLobbyData()
    else null
    return LobbyResponse(success, message, lobbyId, lobby)
}

fun JSONObject.toLobbyData(): LobbyData {
    val id        = getString("id")
    val name      = getString("name")
    val hostId    = getString("hostId")
    val isStarted = optBoolean("started", false)

    val usersArray = getJSONArray("users")
    val users = (0 until usersArray.length()).map { i ->
        val u = usersArray.getJSONObject(i)
        LobbyUserData(id = u.getString("id"), name = u.optString("nickName", u.optString("name", "Unknown")))
    }

    val readyObj    = optJSONObject("readyStatus") ?: JSONObject()
    val readyStatus = buildMap<String, Boolean> {
        readyObj.keys().forEach { k -> put(k, readyObj.getBoolean(k)) }
    }

    val rolesObj      = optJSONObject("selectedRoles") ?: JSONObject()
    val selectedRoles = buildMap<String, String> {
        rolesObj.keys().forEach { k -> put(k, rolesObj.getString(k)) }
    }

    return LobbyData(id, name, hostId, isStarted, users, readyStatus, selectedRoles)
}

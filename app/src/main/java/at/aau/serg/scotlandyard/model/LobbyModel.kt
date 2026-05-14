package at.aau.serg.scotlandyard.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// ── Datenklassen ──────────────────────────────────────────────────────────────

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
    val gameId: String?,
    val lobby: LobbyData?
)

// ── Gson Hilfsdatenklassen (intern für Parsing) ───────────────────────────────

private data class GsonUser(
    @SerializedName("id") val id: String?,
    @SerializedName("nickName") val nickName: String?,
    @SerializedName("name") val name: String?
)

private data class GsonLobby(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("hostId") val hostId: String?,
    @SerializedName("started") val started: Boolean?,
    @SerializedName("users") val users: List<GsonUser>?,
    @SerializedName("readyStatus") val readyStatus: Map<String, Boolean>?,
    @SerializedName("selectedRoles") val selectedRoles: Map<String, String>?
)

private data class GsonResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("lobbyId") val lobbyId: String?,
    @SerializedName("gameId") val gameId: String?,
    @SerializedName("lobby") val lobby: GsonLobby?
)

// ── Parser ────────────────────────────────────────────────────────────────────

private val gson = Gson()

fun String.toLobbyResponse(): LobbyResponse {
    val raw = gson.fromJson(this, GsonResponse::class.java)
    return LobbyResponse(
        success  = raw.success ?: false,
        message  = raw.message ?: "",
        lobbyId  = raw.lobbyId?.takeIf { it.isNotBlank() },
        gameId   = raw.gameId?.takeIf { it.isNotBlank() },
        lobby    = raw.lobby?.toLobbyData()
    )
}

private fun GsonLobby.toLobbyData(): LobbyData {
    return LobbyData(
        id            = id ?: "",
        name          = name ?: "",
        hostId        = hostId ?: "",
        isStarted     = started ?: false,
        users         = users?.map { u ->
            LobbyUserData(
                id   = u.id ?: "",
                name = u.nickName ?: u.name ?: "Unknown"
            )
        } ?: emptyList(),
        readyStatus   = readyStatus ?: emptyMap(),
        selectedRoles = selectedRoles ?: emptyMap()
    )
}
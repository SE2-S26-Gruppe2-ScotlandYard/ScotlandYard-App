package at.aau.serg.scotlandyard.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.json.JSONObject

private const val TAG = "RejoinStompService"

class RejoinStompService(private val session: StompSession) {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when player reconnects and was previously in a lobby.
     */
    fun rejoinLobby(lobbyId: String, userId: String, nickName: String) {
        sendToServer("/app/lobby/rejoin", JSONObject().apply {
            put("lobbyId", lobbyId)
            put("userId", userId)
            put("nickName", nickName)
        })
        Log.d(TAG, "Rejoining lobby: $lobbyId")
    }

    /**
     * Called when player reconnects and was previously in a game.
     * gameId can be null – server will look it up from session tracking.
     */
    fun rejoinGame(userId: String, gameId: String? = null) {
        sendToServer("/app/game/rejoin", JSONObject().apply {
            put("userId", userId)
            if (gameId != null) put("gameId", gameId)
        })
        Log.d(TAG, "Rejoining game: $gameId")
    }

    private fun sendToServer(destination: String, json: JSONObject) {
        scope.launch {
            try {
                session.sendText(destination, json.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Send failed to $destination", e)
            }
        }
    }
}
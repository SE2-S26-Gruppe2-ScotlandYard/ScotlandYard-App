package at.aau.serg.scotlandyard.network

import android.content.Context
import at.aau.serg.scotlandyard.data.getServerUriCustomPreference
import at.aau.serg.scotlandyard.data.getServerUriTypePreference

object ServerConfig {
    /** Emulator: 10.0.2.2 is the host machine's loopback address as seen from the Android emulator */
    const val LOCAL_URI = "ws://10.0.2.2:8080/scotlandyard"

    /** Physical device: LAN IP of the machine running the backend (must be on the same network) */
    const val DEVICE_URI = "ws://192.168.68.109:8080/scotlandyard"

    /** AAU demo server – works from everywhere without a local backend */
    const val GLOBAL_URI = "ws://se2-demo.aau.at:53206/scotlandyard"

    var activeUri: String = LOCAL_URI
        private set

    fun init(context: Context) {
        activeUri = when (context.getServerUriTypePreference()) {
            "LOCAL" -> LOCAL_URI
            "DEVICE" -> context.getServerUriCustomPreference().takeIf { it.isNotBlank() } ?: DEVICE_URI
            else -> GLOBAL_URI
        }
    }
}
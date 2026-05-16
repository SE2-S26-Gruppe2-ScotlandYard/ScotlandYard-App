package at.aau.serg.scotlandyard.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject


object BoardConnection {
    private var adjacency: Map<Int, Map<TicketType, Set<Int>>> = emptyMap()
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        val json = context.assets.open("board.json").bufferedReader().use { it.readText() }

        adjacency = parse(json)
        initialized = true
    }

    fun reachableFrom(fromNode: Int, ticket: TicketType): Set<Int> {
        val byTicket = adjacency[fromNode] ?: return emptySet()

        return if (ticket == TicketType.BLACK) {
            // wildcard: union of all transport types
            byTicket.values.flatten().toSet()
        } else {
            byTicket[ticket] ?: emptySet()
        }
    }

    private fun parse(json: String): Map<Int, Map<TicketType, Set<Int>>> {
        val root = JSONObject(json)
        val result = HashMap<Int, Map<TicketType, Set<Int>>>(root.length())

        for (key in root.keys()) {
            val nodeId = key.toIntOrNull() ?: continue
            val connections: JSONArray = root.getJSONArray(key)

            val byTicket = HashMap<TicketType, MutableSet<Int>>()

            for (i in 0 until connections.length()) {
                val conn: JSONObject = connections.getJSONObject(i)
                val to = conn.getInt("to")
                val transport = conn.getString("transport")

                val ticketType = when (transport) {
                    "WALKING" -> TicketType.WALKING
                    "ESCOOTER" -> TicketType.ESCOOTER
                    "CARSHARING" -> TicketType.CARSHARING
                    "BLACK" -> TicketType.BLACK
                    else -> continue   // unknown transport → skip
                }

                byTicket.getOrPut(ticketType) { mutableSetOf() }.add(to)
            }

            result[nodeId] = byTicket.mapValues { (_, v) -> v.toSet() }
        }

        return result
    }
}
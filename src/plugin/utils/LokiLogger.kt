package plugin.utils

import arc.util.Http
import arc.util.Log
import plugin.PVars.*
import java.util.concurrent.CopyOnWriteArrayList

data class LogEntry(
    val level: String,
    val message: String,
    val ts: Long = now()
)

val logs = CopyOnWriteArrayList<LogEntry>()

fun addLog(level: String, message: String) {
    logs.add(LogEntry(level, message))
}

fun pushLogs() {
    if (logs.isEmpty()) return

    // Snapshot current logs atomically and clear them
    val snapshot = mutableListOf<LogEntry>()
    snapshot.addAll(logs)
    logs.clear()

    if (snapshot.isEmpty()) return

    val grouped = snapshot.groupBy { it.level }

    val sb = StringBuilder()
    sb.append("""{"streams":[""")

    var firstStream = true

    for ((level, entries) in grouped) {

        val sorted = entries.sortedBy { it.ts }

        if (!firstStream) sb.append(',')
        firstStream = false

        sb.append("""{"stream":{"job":"${gamemode.simpleName}","level":"$level"},"values":[""")

        var firstValue = true
        for (entry in sorted) {
            if (!firstValue) sb.append(',')
            firstValue = false

            val msg = escapeJson(stripAnsi(entry.message))
            sb.append("""["${entry.ts}","$msg"]""")
        }

        sb.append("]}")
    }

    sb.append("]}")

    send(sb.toString())
}

fun stripAnsi(str: String): String {
    val ansiRegex = "\u001B\\[[;\\d]*m".toRegex()
    return str.replace(ansiRegex, "")
}

fun escapeJson(s: String): String {
    val sb = StringBuilder(s.length)
    for (c in s) {
        when (c) {
            '\\' -> sb.append("\\\\")
            '"' -> sb.append("\\\"")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> sb.append(c)
        }
    }
    return sb.toString()
}

fun now(): Long {
    return System.currentTimeMillis() * 1_000_000L
}
/*
fun send(payload: String) {
    Log.debug(payload)

    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(lokiApi))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build()

    try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() in 200..299) {
            Log.info("Sent logs to Loki successfully!")
        } else {
            Log.err("Loki push failed: HTTP ${response.statusCode()}")
            Log.err("Response body: ${response.body()}")
        }
    } catch (e: Exception) {
        Log.err("Exception while sending logs to Loki: $e")
    }
}
*/

fun send(payload: String) {
    Log.debug(payload)

    Http.post(lokiApi)
        .content(payload)
        .header("Content-Type", "application/json")
        .header("Authorization", "Basic $apiAuth")
        .error { fail -> Log.err(fail) }
        .submit(
            { res -> Log.info("Sent logs to loki!") }
        )
}
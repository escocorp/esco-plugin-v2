package plugin

import arc.struct.Seq
import arc.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import plugin.database.models.MapStats
import plugin.models.ChatMessageData

object KVars {
    val errorHandler = CoroutineExceptionHandler { _, e ->
        Log.err("Exception in coroutine", e)
    }

    val globalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)
    val eventsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    var mapStats: MapStats? = null
    var startTime = System.currentTimeMillis()
    val messageBuffer = Seq<ChatMessageData>()

    const val buildsBaseUrl = "https://builds.larzed.icu"
    const val buildsLatestTxtUrl = "$buildsBaseUrl/latest.txt"
}
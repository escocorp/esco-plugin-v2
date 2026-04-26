package plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import plugin.database.models.MapStats

object KVars {
    val globalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val eventsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var mapStats: MapStats? = null
    var startTime = System.currentTimeMillis()

    const val buildsBaseUrl = "https://builds.larzed.icu"
    const val buildsLatestTxtUrl = "$buildsBaseUrl/latest.txt"
}
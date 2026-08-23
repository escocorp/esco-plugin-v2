package plugin

import arc.files.Fi
import arc.struct.Seq
import arc.util.Log
import com.sun.management.OperatingSystemMXBean
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import mindustry.Vars
import plugin.model.ChatMessageData
import plugin.model.GlobalConfig
import java.lang.management.ManagementFactory

object KVars {
    val errorHandler =
        CoroutineExceptionHandler { _, e ->
            Log.err("Exception in coroutine", e)
        }

    val globalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)
    val eventsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + errorHandler)

    val messageBuffer = Seq<ChatMessageData>()

    const val buildsBaseUrl = "https://builds.larzed.icu"
    const val buildsLatestTxtUrl = "$buildsBaseUrl/latest.txt"

    const val frozenTag = "[white]<\uF7B5>[]"

    val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

    var globalConfigLink: String? = null
    var globalConfig: GlobalConfig? = null
    val globalConfigCache get() = KVars.cacheDir.child("globalconfig.json")

    val cacheDir: Fi by lazy {
        Vars.dataDirectory.child("cache").apply { mkdirs() }
    }
}

package plugin

import kotlinx.coroutines.*

object KVars {
    val eventsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
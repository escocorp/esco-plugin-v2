package plugin.events

import arc.Events
import kotlinx.coroutines.launch
import plugin.KVars.eventsScope
// vibecode
object EventRegistrar {
    fun register(instance: Any) {
        for (method in instance::class.java.declaredMethods) {
            val annotation = method.getAnnotation(EventListener::class.java) ?: continue

            require(method.parameterCount == 1) {
                "${method.name} must have exactly one parameter"
            }

            val eventClass = method.parameterTypes[0]
            method.isAccessible = true

            @Suppress("UNCHECKED_CAST")
            Events.on(eventClass as Class<Any>) { event ->
                if (annotation.async) {
                    eventsScope.launch { method.invoke(instance, event) }
                } else {
                    method.invoke(instance, event)
                }
            }
        }
    }
}

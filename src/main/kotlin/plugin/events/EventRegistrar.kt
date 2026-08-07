package plugin.events

import arc.Events

object EventRegistrar {
    fun register(instance: Any) {
        for (method in instance::class.java.declaredMethods) {
            if (!method.isAnnotationPresent(EventListener::class.java))
                continue

            require(method.parameterCount == 1) {
                "${method.name} must have exactly one parameter"
            }

            val eventClass = method.parameterTypes[0]

            @Suppress("UNCHECKED_CAST")
            Events.on(eventClass as Class<Any>) { event ->
                method.invoke(instance, event)
            }
        }
    }
}
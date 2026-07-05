package plugin.discord.register

import arc.util.CommandHandler
import arc.util.Log
import net.dv8tion.jda.api.EmbedBuilder
import plugin.discord.Context
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure

object DiscordCommandAnnotationProcessor {
    fun registerCommands(listener: Any, defaultCommandHandler: CommandHandler, globalCommandListener: CommandHandler) {
        for (function in listener::class.memberFunctions) {
            if (function.parameters.isEmpty()) continue
            val annotation = function.findAnnotation<DiscordCommand>() ?: continue
            val handler = CommandHandler.CommandRunner { args: Array<String>, ctx: Context ->
                if (!ctx.hasPerm(annotation.requiredPerm)) {
                    return@CommandRunner
                }
                try {
                    val instanceParam = function.instanceParameter
                        ?: throw IllegalStateException("Function ${function.name} must be a class member")

                    val contextParam = function.parameters.find { it.type.jvmErasure == Context::class }
                        ?: throw IllegalStateException("Function ${function.name} missing Context parameter")

                    val argsParam = function.parameters.filter { it.kind == KParameter.Kind.VALUE }
                        .find {
                            it.type.jvmErasure.java.isArray || it.type.classifier == Array::class
                        }
                        ?: throw IllegalStateException("Function ${function.name} missing Array<String> parameter")

                    val params = mapOf(
                        instanceParam to listener,
                        contextParam to ctx,
                        argsParam to args
                    )
                    try {
                        function.callBy(params)
                    } catch (e: InvocationTargetException) {
                        throw e.cause ?: e
                    }

                } catch (e: DiscordCommandException) {
                    ctx.replyEmbed(EmbedBuilder().apply {
                        setColor(0xff0000)
                        setTitle("Error")
                        setDescription(e.message)
                    }.build())
                } catch (e: Exception) {
                    ctx.replyEmbed(EmbedBuilder().apply {
                        setColor(0xff0000)
                        setTitle("Internal Error")
                        setDescription("An internal error occurred while running this command.")
                    }.build())
                    Log.err(e)
                }
            }
            val targetHandlers = when (annotation.type) {
                CommandType.ALL -> listOf(defaultCommandHandler, globalCommandListener)
                CommandType.DEFAULT -> listOf(defaultCommandHandler)
                CommandType.GLOBAL -> listOf(globalCommandListener)
            }
            if (annotation.args.isEmpty()) {
                targetHandlers.forEach { it.register(annotation.name, annotation.desc, handler) }
            } else {
                targetHandlers.forEach { it.register(annotation.name, annotation.args, annotation.desc, handler) }
            }
        }
    }
}
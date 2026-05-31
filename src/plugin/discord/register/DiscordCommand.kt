package plugin.discord.register

import plugin.utils.Permission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DiscordCommand(val name: String, val desc: String, val args: String = "", val type: CommandType = CommandType.DEFAULT, val requiredPerm: Permission = Permission.none)

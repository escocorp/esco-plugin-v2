package plugin.events

import plugin.commands.CustomHandler

class EscoPluginLoadEvent() {}

data class RegisterEscoCommandsEvent(val handler: CustomHandler) {}
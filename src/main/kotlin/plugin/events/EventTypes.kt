package plugin.events

import plugin.commands.CustomHandler
import javax.sql.DataSource

class EscoPluginLoadEvent

data class RegisterEscoCommandsEvent(val handler: CustomHandler)

class DatabaseLoadEvent(dataSource: DataSource)
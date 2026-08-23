package plugin.model

data class GlobalConfig(
    val aiApiUrl: String?,
    val aiEnabled: Boolean = aiApiUrl != null,
    val aiApiKey: String?,
)
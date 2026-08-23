package plugin.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GlobalConfig(
    val aiApiUrl: String?,
    val aiEnabled: Boolean = aiApiUrl != null,
    val aiApiKey: String?,
)

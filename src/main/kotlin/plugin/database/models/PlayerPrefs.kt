package plugin.database.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PlayerPrefs(
    var showWelcomeMenu: Boolean = true,
    var owoAccent: Boolean = false,
    var ohioAccent: Boolean = false
)
package plugin.replays

import kotlinx.serialization.Serializable

@Serializable
data class ReplayPlayer(val name: String?, val id: Int?) {
}
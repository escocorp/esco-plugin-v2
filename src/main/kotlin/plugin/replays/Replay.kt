package plugin.replays

import kotlinx.serialization.Serializable

@Serializable
data class Replay(
    val map: String,
    val players: List<ReplayPlayer>,
    val actions: HashMap<Long, ReplayStack>
)
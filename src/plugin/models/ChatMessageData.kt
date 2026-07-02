package plugin.models

import kotlin.time.Instant

data class ChatMessageData(
    val playerId: Int,
    val unformatted: String,
    val formatted: String,
    val timestamp: Instant
)
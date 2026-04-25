// Auto-generated on 2026-04-25T22:28:05.484Z
package plugin.database.models

data class MapStats(
    val id: Int,
    val name: String,
    val server: Int,
    val minWave: Int,
    val avgWave: Int,
    val maxWave: Int,
    val minPlaytime: Int,
    val avgPlaytime: Int,
    val maxPlaytime: Int,
    val wins: Int,
    val loses: Int,
    val skips: Int
)
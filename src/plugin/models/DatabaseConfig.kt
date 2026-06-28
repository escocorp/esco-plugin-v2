package plugin.models

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val dbName: String
)
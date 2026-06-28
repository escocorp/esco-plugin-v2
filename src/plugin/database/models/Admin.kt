package plugin.database.models

import arc.struct.Seq
import plugin.database.Database.executeUpdate

class Admin(
    val id: Int,
    val playerId: Int,
    val rankId: Int,
    val rankName: String,
    val perms: Seq<Permission>,
    var hidden: Boolean
) {

    fun updateHidden(hidden: Boolean): Boolean {
        val updated = executeUpdate(
            """
            UPDATE ADMINS SET
            hidden = ?
            WHERE id = ?
            """.trimIndent()
        ) { stmt ->
            stmt.setBoolean(1, hidden)
            stmt.setInt(2, id)
        }

        if (updated) {
            this.hidden = hidden
        }

        return updated
    }
}
package uqac.catwalk.sauvegarde.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    var obtenu: Boolean = false,
    val débloqué: Boolean = false,
)

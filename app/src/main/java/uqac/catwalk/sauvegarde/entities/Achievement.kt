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
    val reward: Int = 0,
    val objectif : Int = 0,
    val type: Type
)

sealed class Objectif {
    data class Int(val value: Int) : Objectif()
    data class Boolean(val value: Boolean) : Objectif()
    data class String(val value: String) : Objectif()
}

 enum class Type {
     FinBallade,
     AchatChat,
     FinChat,
 }

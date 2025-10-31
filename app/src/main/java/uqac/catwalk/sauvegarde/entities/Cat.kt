package uqac.catwalk.sauvegarde.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "cats",
    foreignKeys = [ForeignKey(
        entity = Achievement::class,
        parentColumns = ["id"],
        childColumns = ["achievementId"],
        onDelete = ForeignKey.SET_NULL
    )])
data class Cat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    var happiness: Int,
    var cleanliness: Int,
    var affection: Float,
    var achievementId: Int?, // Nullable in case the cat has no achievement
    val obtenu: Boolean,
)

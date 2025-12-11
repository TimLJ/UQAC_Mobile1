package uqac.catwalk.sauvegarde.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "cats",
    foreignKeys = [ForeignKey(
        entity = Achievement::class,
        parentColumns = ["id"],
        childColumns = ["achievementId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["achievementId"])]
)
data class Cat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: String,
    var happiness: Int,
    var cleanliness: Int,
    var affection: Float,
    var achievementId: Int?, // Nullable in case the cat has no achievement
    val obtenu: Boolean,
    @ColumnInfo(name = "last_decay_at")
    val lastDecayAt: Long = System.currentTimeMillis(), // derniere fois ou les stats ont decaye
    @ColumnInfo(name = "lastSeenAt")
    val lastSeenAt: Long = System.currentTimeMillis() // frequence de visite
) : Parcelable

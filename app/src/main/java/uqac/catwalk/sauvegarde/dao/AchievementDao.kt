package uqac.catwalk.sauvegarde.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uqac.catwalk.sauvegarde.entities.Achievement

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: Achievement)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("UPDATE achievements SET obtenu = 1 WHERE id = :id")
    suspend fun claim (id: Int)

    @Query("UPDATE achievements SET débloqué = 1 WHERE id = :id")
    suspend fun accomplished (id: Int)

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementByID(id: Int): Achievement?

    @Query("SELECT * FROM achievements WHERE obtenu = 0 AND type IN (:type)")
    fun getUnachievedAchievementsByType(type: uqac.catwalk.sauvegarde.entities.Type): List<Achievement>
}

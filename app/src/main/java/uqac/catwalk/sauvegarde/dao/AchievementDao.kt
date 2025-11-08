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
}

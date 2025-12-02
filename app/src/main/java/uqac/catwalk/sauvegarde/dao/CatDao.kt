package uqac.catwalk.sauvegarde.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import uqac.catwalk.sauvegarde.entities.Cat

@Dao
interface CatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cat: Cat)

    @Update
    suspend fun update(cat: Cat)

    @Query("SELECT * FROM cats")
    fun getAllCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE id = :id")
    fun getCatById(id: Int): Flow<Cat>

    @Query("SELECT * FROM cats WHERE obtenu = 1")
    fun getDebloques(): Flow<List<Cat>>

    @Query("UPDATE cats SET name = :name WHERE id = :id")
    suspend fun updateCatName(id: Int, name: String)

    @Query("UPDATE cats SET color = :color WHERE id = :id")
    suspend fun updateCatColor(id: Int, color: String)

    @Query("UPDATE cats SET happiness = :happiness WHERE id = :id")
    suspend fun updateCatHappiness(id: Int, happiness: Int)

    @Query("UPDATE cats SET cleanliness = :cleanliness WHERE id = :id")
    suspend fun updateCatCleanliness(id: Int, cleanliness: Int)

    @Query("UPDATE cats SET affection = :affection WHERE id = :id")
    suspend fun updateCatAffection(id: Int, affection: Double)

    @Query("UPDATE cats SET achievementId = :achievementId WHERE id = :id")
    suspend fun updateCatAchievementId(id: Int, achievementId: Int?)

    @Query("UPDATE cats SET obtenu = :obtenu WHERE id = :id")
    suspend fun updateCatObtenu(id: Int, obtenu: Boolean)
}

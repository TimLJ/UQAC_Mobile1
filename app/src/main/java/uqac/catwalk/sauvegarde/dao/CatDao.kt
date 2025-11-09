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

    @Query("SELECT * FROM cats WHERE obtenu = true")
    fun getDébloqués(): Flow<List<Cat>>
}

package uqac.catwalk.sauvegarde.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uqac.catwalk.sauvegarde.entities.Item

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: Item)

    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>> // Flow pour des mises à jour automatiques de l'UI

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): Item?
}
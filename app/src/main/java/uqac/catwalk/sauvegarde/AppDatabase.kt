package uqac.catwalk.sauvegarde

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uqac.catwalk.sauvegarde.dao.AchievementDao
import uqac.catwalk.sauvegarde.dao.CatDao
import uqac.catwalk.sauvegarde.dao.ItemDao
import uqac.catwalk.sauvegarde.entities.Achievement
import uqac.catwalk.sauvegarde.entities.Cat
import uqac.catwalk.sauvegarde.entities.Converters
import uqac.catwalk.sauvegarde.entities.Item

@Database(
    entities = [Cat::class, Item::class, Achievement::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CatDao(): CatDao
    abstract fun ItemDao(): ItemDao
    abstract fun AchievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catwalk_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            // Pré-remplir les achievements
            val achievementDao = database.AchievementDao()
            val achievements = listOf(
                Achievement(name = "Premier pas", description = "Adopter votre premier chat", débloqué = true, obtenu = true),
                Achievement(name = "Collectionneur", description = "Adopter 5 chats"),
                Achievement(name = "Amoureux des chats", description = "Adopter 10 chats"),
                Achievement(name = "Roi du bonheur", description = "Atteindre 100 de bonheur"),
                Achievement(name = "Propre comme un sou neuf", description = "Atteindre 100 de propreté"),
                Achievement(name = "Marcheur", description = "Faire sa première ballade", débloqué = true, obtenu = false),
            )
            achievements.forEach { achievementDao.insert(it) }

            // Pré-remplir les items
            val itemDao = database.ItemDao()
            val items = listOf(
                Item(name = "Nourriture basique", price = 10.0, level = 1, unlock = listOf(1)),
                Item(name = "Jouet souris", price = 15.0, level = 1, unlock = listOf(1)),
                Item(name = "Brosse", price = 20.0, level = 1, unlock = listOf(2)),
                Item(name = "Nourriture premium", price = 50.0, level = 2, unlock = listOf(3)),
                Item(name = "Arbre à chat", price = 100.0, level = 3, unlock = listOf(5))
            )
            items.forEach { itemDao.insert(it) }

            // Optionnel : ajouter des chats de départ
            val catDao = database.CatDao()
            val starterCats = listOf(
                Cat(
                    name = "Minou",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0.5f,
                    achievementId = null,
                    obtenu = true
                ),
                Cat(
                    name = "Bouboule",
                    happiness = 70,
                    cleanliness = 30,
                    affection = 1.2f,
                    achievementId = null,
                    obtenu = true
                ),
                Cat(
                    name = "Whiskers",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0.5f,
                    achievementId = null,
                    obtenu = false
                ),
                Cat(
                    name = "Odie",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0.5f,
                    achievementId = null,
                    obtenu = false
                ),
                Cat(
                    name = "Félix",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0.5f,
                    achievementId = null,
                    obtenu = true
                )
            )
            starterCats.forEach { catDao.insert(it) }
        }
    }
}

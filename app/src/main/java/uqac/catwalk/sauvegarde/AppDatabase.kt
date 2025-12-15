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
import uqac.catwalk.sauvegarde.entities.Type

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
                Achievement(name = "Premier pas", description = "Adopter votre premier chat", type = Type.AchatChat, objectif = 3, reward = 25),
                Achievement(name = "Collectionneur", description = "Adopter 3 chats", type = Type.AchatChat, objectif = 5, reward = 50),
                Achievement(name = "Amoureux des chats", description = "Adopter 7 chats", type = Type.AchatChat, objectif = 7, reward = 0),
                Achievement(name = "Roi du bonheur", description = "Atteindre 100 de bonheur sur tous les chats" , type = Type.FinChat, reward = 25),
                Achievement(name = "Propre comme un sou neuf", description = "Atteindre 100 de propreté sur tous les chats", type = Type.FinChat, reward = 25),
                Achievement(name = "Marcheur", description = "Faire une ballade de 1000 mètres", type = Type.FinBallade, objectif = 1000, reward = 50),
                Achievement(name = "Grand Marcheur", description = "Faire une ballade de 3000 mètres", type = Type.FinBallade, objectif = 3000, reward = 75),
                //Achievement(name = "Marathonien", description = "Marcher un marathon en une journée", type = Type.FinBallade, objectif = 42195, reward = 200),
                Achievement(name = "Usain Bolt ?", description = "Parcourir 100 mètres au total", type = Type.FinBallade, objectif = 100, reward = 50),

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

            // liste chat
            val catDao = database.CatDao()
            val listCats = listOf(
                Cat(
                    name = "Minou",
                    color = "chat_blanc_noir",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = true,
                    price = 0.0,
                    level = 1
                ),
                Cat(
                    name = "Bouboule",
                    color = "chat_gris",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = true,
                    price = 0.0,
                    level = 1
                ),
                Cat(
                    name = "Whiskers",
                    color = "chat_roux",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 50.0,
                    level = 1
                ),
                Cat(
                    name = "Snowball",
                    color = "chat_blanc",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 70.0,
                    level = 2
                ),
                Cat(
                    name = "Félix",
                    color = "chat_noir",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 100.0,
                    level = 2
                ),
                Cat(
                    name = "Lily",
                    color = "chat_tricolore",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 120.0,
                    level = 3
                ),
                Cat(
                    name = "Simba",
                    color = "chat_roux",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 150.0,
                    level = 3
                ),
                Cat(
                    name = "Chloe",
                    color = "chat_blanc_noir",
                    happiness = 50,
                    cleanliness = 50,
                    affection = 0f,
                    achievementId = null,
                    obtenu = false,
                    price = 200.0,
                    level = 4
                )
            )
            listCats.forEach { catDao.insert(it) }
        }
    }
}

package uqac.catwalk.sauvegarde

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

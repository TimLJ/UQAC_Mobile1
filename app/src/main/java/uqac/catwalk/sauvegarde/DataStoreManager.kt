package uqac.catwalk.sauvegarde

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Déclare l'instance de DataStore une seule fois, de manière globale.
// Le nom "player_preferences" sera le nom du fichier de sauvegarde.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_preferences")

class DataStoreManager(private val context: Context) {

    // Créez des clés pour chaque donnée que vous voulez sauvegarder.
    companion object {
        val PLAYER_NAME_KEY = stringPreferencesKey("player_name")
        val PLAYER_LV_KEY = intPreferencesKey("player_lv")
        val PLAYER_XP_KEY = intPreferencesKey("player_xp")
        val PLAYER_MONEY_KEY = intPreferencesKey("player_money")
        val PLAYER_TOTAL_DISTANCE_KEY = intPreferencesKey("player_total_distance")
        val PLAYER_DAILY_DISTANCE_KEY = intPreferencesKey("player_daily_distance")
    }

    // Fonction pour sauvegarder TOUTES les données du joueur.
    suspend fun savePlayerData(playerData: PlayerData) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_NAME_KEY] = playerData.name
            preferences[PLAYER_LV_KEY] = playerData.Lv
            preferences[PLAYER_XP_KEY] = playerData.Xp
            preferences[PLAYER_MONEY_KEY] = playerData.money
            preferences[PLAYER_TOTAL_DISTANCE_KEY] = playerData.TDistance
            preferences[PLAYER_DAILY_DISTANCE_KEY] = playerData.DDistance
        }
    }

    // Fonction pour lire les données du joueur au démarrage.
    suspend fun loadPlayerData(): PlayerData {
        val preferences = context.dataStore.data.first() // Lit les données une seule fois
        return PlayerData.apply {
            name = preferences[PLAYER_NAME_KEY] ?: "PlayerName"
            Lv = preferences[PLAYER_LV_KEY] ?: 1
            Xp = preferences[PLAYER_XP_KEY] ?: 0
            money = preferences[PLAYER_MONEY_KEY] ?: 100
            TDistance = preferences[PLAYER_TOTAL_DISTANCE_KEY] ?: 0
            DDistance = preferences[PLAYER_DAILY_DISTANCE_KEY] ?: 0
        }
    }
}

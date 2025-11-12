package uqac.catwalk.sauvegarde

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.serialization.Serializable
import kotlin.math.pow
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

@Serializable
data class PlayerData(
    var name: String = "PlayerName", //Nom du joueur
    var Lv: Int = 1, //Niveau du joueur
    var Xp: Int = 0, //Exp du joueur
    var money: Int = 100, //Argent du joueur
    var TDistance: Long = 0L, //Distance totale marchée
    var DDistance: Int = 0, //Distance marchée ce jour ci
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _playerData = MutableStateFlow(PlayerData())
    val playerData: StateFlow<PlayerData> = _playerData.asStateFlow()

    private val context = application.applicationContext
    private val playerFile = File(context.filesDir, "InfoPlayer.json")

    init {
        loadPlayerData()
    }

    private fun loadPlayerData() {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    if (playerFile.exists()) {
                        val jsonString = playerFile.readText()
                        Gson().fromJson(jsonString, PlayerData::class.java)
                    } else {
                        // Créer un nouveau joueur avec des valeurs par défaut
                        PlayerData()
                    }
                }
                _playerData.value = data
            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur, utiliser les valeurs par défaut
                _playerData.value = PlayerData()
            }
        }
    }

    fun addXp(xp: Int) {
        val currentData = _playerData.value
        val newXp = currentData.Xp + xp
        var newLevel = currentData.Lv
        var remainingXp = newXp

        // Vérifier si le joueur peut monter de niveau
        while (remainingXp >= xPReq(newLevel)) {
            remainingXp -= xPReq(newLevel)
            newLevel++
        }

        _playerData.value = currentData.copy(
            Xp = remainingXp,
            Lv = newLevel
        )
        savePlayerData()
    }

    fun delXp(xp: Int) {
        val currentData = _playerData.value
        val newXp =  currentData.Xp - xp
        _playerData.value = currentData.copy(Xp = newXp)
        savePlayerData()
    }

    private fun xPReq(level: Int): Int {
        return (0.5 * (level + 1).toDouble()
            .pow(3)).toInt() //Fonction de calcul de l'XP pour Lv Up, à ajuster si besoin
    }

    fun addMoney(money: Int) {
        val currentData = _playerData.value
        _playerData.value = currentData.copy(money = currentData.money + money)
        savePlayerData()
    }

    fun delMoney(money: Int) {
        val currentData = _playerData.value
        _playerData.value = currentData.copy(money = maxOf(0, currentData.money - money))
        savePlayerData()
    }

    fun addDistance(distance: Int) {
        val currentData = _playerData.value
        _playerData.value = currentData.copy(
            DDistance = currentData.DDistance + distance,
            TDistance = currentData.TDistance + distance
        )
        savePlayerData()
    }

    fun resetDistance() {
        val currentData = _playerData.value
        _playerData.value = currentData.copy(DDistance = 0)
        savePlayerData()
    }

    fun updateName(newName: String) {
        val currentData = _playerData.value
        _playerData.value = currentData.copy(name = newName)
        savePlayerData()
    }

    private fun savePlayerData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val jsonString = Gson().toJson(_playerData.value)
                    playerFile.writeText(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Méthodes pour obtenir des valeurs spécifiques
    fun getCurrentLevel(): Int = _playerData.value.Lv
    fun getCurrentXp(): Int = _playerData.value.Xp
    fun getCurrentMoney(): Int = _playerData.value.money
    fun getCurrentName(): String = _playerData.value.name
    fun getTotalDistance(): Long = _playerData.value.TDistance
    fun getDailyDistance(): Int = _playerData.value.DDistance
    fun getXpRequiredForNextLevel(): Int = xPReq(_playerData.value.Lv)
}
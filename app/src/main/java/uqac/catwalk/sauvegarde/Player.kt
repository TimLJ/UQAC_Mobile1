package uqac.catwalk.sauvegarde

import android.content.Context
import kotlin.math.pow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


object PlayerData {
    var name: String = "PlayerName"
    var Lv: Int = 1
    var Xp: Int = 0
    var money: Int = 100
    var TDistance: Long = 0L
    var DDistance: Int = 0
}

public var MsMoney by mutableStateOf(PlayerData.money)



// Les fonctions de mise à jour doivent être suspendues
// pour appeler des fonctions de sauvegarde asynchrones.
suspend fun updtXp(xp: Int, context: Context) {
    val newXp = PlayerData.Xp + xp
    var newLevel = PlayerData.Lv
    var remainingXp = newXp

    while (remainingXp >= xPReq(newLevel)) {
        remainingXp -= xPReq(newLevel)
        newLevel++
    }
    PlayerData.Xp = remainingXp
    PlayerData.Lv = newLevel

    // Sauvegarder les nouvelles données
    val dataStoreManager = DataStoreManager(context)
    dataStoreManager.savePlayerData(PlayerData)
}

private fun xPReq(level: Int): Int {
    return (0.5 * (level + 1).toDouble().pow(3)).toInt()
}

suspend fun updtMoney(money: Int, context: Context) {
    PlayerData.money += money
    // Sauvegarder les nouvelles données
    val dataStoreManager = DataStoreManager(context)
    dataStoreManager.savePlayerData(PlayerData)
    updateMoneyValue(PlayerData.money)
}

fun updateMoneyValue(newMoney: Int) {
    MsMoney = newMoney
}


suspend fun addDistance(distance: Int, context: Context) {
    PlayerData.DDistance += distance
    PlayerData.TDistance += distance
    // Sauvegarder les nouvelles données
    val dataStoreManager = DataStoreManager(context)
    dataStoreManager.savePlayerData(PlayerData)
}

suspend fun resetDistance(context: Context) {
    PlayerData.DDistance = 0
    // Sauvegarder les nouvelles données
    val dataStoreManager = DataStoreManager(context)
    dataStoreManager.savePlayerData(PlayerData)
}

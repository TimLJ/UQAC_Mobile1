package uqac.catwalk.achievements

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.PlayerData // Votre classe pour DataStore
import uqac.catwalk.sauvegarde.updtXp


object AchievementManager {
    private const val TAG = "AchievementManager"

    suspend fun checkAchievementsAfterWalk(context: Context, progress: Int) {
        val db = AppDatabase.getDatabase(context)
        val listToCheck = db.AchievementDao().getUnachievedAchievementsByType(uqac.catwalk.sauvegarde.entities.Type.FinBallade)
        for (achievement in listToCheck) {
            when (achievement.name) {
                "Marcheur" -> checkBalladeAchievement(
                    achievement.id,
                    achievement.objectif,
                    progress,
                    db,
                    context = context
                )

                "Grand Marcheur" -> checkBalladeAchievement(
                    achievement.id,
                    achievement.objectif,
                    progress,
                    db,
                    context = context
                )

                "Marathonien" -> checkBalladeAchievement(
                    achievement.id,
                    achievement.objectif,
                    PlayerData.DDistance,
                    db,
                    context = context
                )

                "Usain Bolt ?" -> checkBalladeAchievement(
                    achievement.id,
                    achievement.objectif,
                    PlayerData.TDistance,
                    db,
                    context = context
                )
            }
        }
    }

    private suspend fun checkBalladeAchievement(
        id: Int,
        target: Int,
        res: Int,
        db: AppDatabase,
        context: Context
    ) {
        if (res >= target) {
            markAsObtenu(id, db, context)
        }
    }


    suspend fun checkAchievementsAfterBuy(context: Context) {
        Log.d(TAG, "checkAchievementsAfterBuy lancé")
        val db = AppDatabase.getDatabase(context)
        val listToCheck = db.AchievementDao()
            .getUnachievedAchievementsByType(uqac.catwalk.sauvegarde.entities.Type.AchatChat)
        val count = db.CatDao().getOwnedCatsCount()
        for (achievement in listToCheck) {
            Log.d(TAG, "Vérification du succès : ${achievement.name} avec objectif ${achievement.objectif}")
            when (achievement.name) {
                "Premier pas" -> checkAchatAchievement(
                    achievement.id,
                    achievement.objectif,
                    count,
                    db,
                    context = context
                )

                "Collectionneur" -> checkAchatAchievement(
                    achievement.id,
                    achievement.objectif,
                    count,
                    db,
                    context = context
                )

                "Amoureux des Chats" -> checkAchatAchievement(
                    achievement.id,
                    achievement.objectif,
                    count,
                    db,
                    context = context
                )
            }
        }
    }


    private suspend fun checkAchatAchievement(
        id: Int,
        target: Int,
        current: Int,
        db: AppDatabase,
        context: Context
    ) {
        Log.d(TAG, "checkAchatAchievement lancé")
        if (current >= target) {
            markAsObtenu(id, db, context)
        }
        else {
            Log.d(TAG, "checkAchatAchievement : Objectif $target et current $current  ")
        }
    }


    suspend fun checkAchievementsAfterCare(context: Context) {
        Log.d(TAG, "checkAchievementsAfterCare lancé")
        val db = AppDatabase.getDatabase(context)
        val listToCheck = db.AchievementDao().getUnachievedAchievementsByType(uqac.catwalk.sauvegarde.entities.Type.FinChat)

        // Collecter le nombre de chats débloqués
        val totalCats = db.CatDao().getDebloques().firstOrNull()?.size ?: 0
        if (totalCats == 0) return // Pas de chats, pas de succès à vérifier

        // Récupérer les autres comptes de manière asynchrone
        val happyCatsCount = db.CatDao().getHappyCatsCount()
        val cleanCatsCount = db.CatDao().getCleanCatsCount()

        for (achievement in listToCheck) {
            Log.d(TAG, "Vérification du succès : ${achievement.name} avec objectif ${achievement.objectif}")
            when (achievement.name) {
                "Roi du bonheur" -> checkJeuAchievement(
                    id = achievement.id,
                    db = db,
                    target = totalCats,
                    res = happyCatsCount, // Utiliser la valeur récupérée
                    context = context
                )

                "Propre comme un sou neuf" -> checkJeuAchievement(
                    id = achievement.id,
                    db = db,
                    target = totalCats,
                    res = cleanCatsCount, // Utiliser la valeur récupérée
                    context = context
                )
            }
        }
    }


    private suspend fun checkJeuAchievement(
        id: Int,
        target: Int,
        res: Int,
        db: AppDatabase,
        context: Context
    ) {
        Log.d(TAG, "checkJeuAchievement lancé")
        if (res >= target) {
            markAsObtenu(id, db, context)
        }
        else {
            Log.d(TAG, "checkJeuAchievement : Objectif $target et current $res  ")
        }
    }


    private suspend fun markAsObtenu(idAch: Int, db: AppDatabase, context: Context) {
        val achievement = db.AchievementDao().getAchievementByID(idAch)
        if (achievement != null && !achievement.obtenu) {
            // Met à jour le succès pour le marquer comme obtenu, mais pas encore réclamé
            db.AchievementDao().accomplished(achievement.id)
            updtXp(achievement.reward, context)

            //Msg de log
            Log.d(TAG, "Succès obtenu : ${achievement.name}")

            // Optionnel : Notifier le joueur
            //Toast.makeText(context, "Succès obtenu : ${achievement.name}", Toast.LENGTH_SHORT).show()

        }
        else{
            Log.d(TAG, "Succès déjà obtenu ou introuvable : ${achievement?.name}")
        }
    }
}

package uqac.catwalk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.DataStoreManager
import uqac.catwalk.sauvegarde.PlayerData
import uqac.catwalk.sauvegarde.entities.Cat
import uqac.catwalk.ui.bar.AppBottomBar
import uqac.catwalk.ui.bar.AppTopBar
import uqac.catwalk.ui.theme.CatwalkTheme
import uqac.catwalk.achievements.AchievementManager

// Représente un chat dans la boutique
data class ShopCat(val name: String, val color: String, val price: Double, val level: Int)

class ShopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShopContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ShopContent(modifier: Modifier = Modifier) {
    // Contexte pour accéder aux ressources et à la BDD
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context = context)
    val catDao = database.CatDao()
    val dataStoreManager = DataStoreManager(context)

    // Etats pour les informations utilisateur
    var playerData by remember { mutableStateOf<PlayerData?>(null) }
    var levelUser by remember { mutableStateOf(1) }
    var moneyUser by remember { mutableStateOf(100) }

    // Charge les données utilisateur au démarrage
    LaunchedEffect(Unit) {
        val pd = dataStoreManager.loadPlayerData()
        playerData = pd
        levelUser = pd.Lv
        moneyUser = pd.money
    }

    // Récupère la liste de chats depuis la base (Flow<List<Cat>> attendu)
    val catsFromDb by catDao.getCatsNotObtained().collectAsState(initial = emptyList())

    // Mappe les chats de la BDD en ShopCat (prix par défaut ici)
    val shopCats = catsFromDb.map { dbCat ->
        ShopCat(
            name = dbCat.name,
            color = dbCat.color,
            price = dbCat.price,
            level = dbCat.level
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(80.dp),
            )
        },

        content = { paddingValues ->
            Box(
                modifier = modifier
                    .padding(
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                        top = 80.dp,
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = 100.dp
                    )
                    .fillMaxSize()
                    .background(color = colorResource(R.color.yellow_white))
            ) {
                // Liste scrollable des articles
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Boutique de Chats ",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 16.dp,
                                bottom = 16.dp
                            ),
                        textAlign = TextAlign.Center
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(shopCats) { shopCat ->
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .height(120.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = catPainter(shopCat.color),
                                            contentDescription = "Chat ${shopCat.name}",
                                            modifier = Modifier
                                                .size(64.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = shopCat.name,
                                            textAlign = TextAlign.Start,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier
                                        )
                                    }

                                    val priceInt = shopCat.price.toInt()
                                    val lockedByLevel = shopCat.level > levelUser
                                    val canBuy = (!lockedByLevel) && (moneyUser >= priceInt)

                                    if (lockedByLevel) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Verrouillé",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Text(
                                                text = "Lvl ${shopCat.level}",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    // Bouton d'achat
                                    Button(
                                        onClick = {
                                            if (!canBuy) {
                                                if (lockedByLevel) {
                                                    Toast.makeText(context, "Niveau insuffisant", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Pas assez d'argent", Toast.LENGTH_SHORT).show()
                                                }
                                                return@Button
                                            }
                                            // Mise à jour
                                            PlayerData.money -= priceInt
                                            playerData = PlayerData
                                            moneyUser = PlayerData.money

                                            // Mise à jour BDD et DataStore en arrière-plan
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val existing: Cat = catDao.getByName(shopCat.name)
                                                val updatedCat = existing.copy(
                                                    price = shopCat.price,
                                                    level = shopCat.level,
                                                    obtenu = true
                                                )
                                                catDao.update(updatedCat)
                                                dataStoreManager.savePlayerData(PlayerData)
                                            }

                                            Toast.makeText(context, "${shopCat.name} acheté !", Toast.LENGTH_SHORT).show()

                                            //gestion des achievements
                                            CoroutineScope(Dispatchers.IO).launch {
                                                Log.d("AchievementManager", "Lancement de checkAchievementsAfterBuy")
                                                AchievementManager.checkAchievementsAfterBuy(context)
                                            }

                                        },
                                        enabled = canBuy,
                                        modifier = Modifier
                                            .width(120.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingCart,
                                            contentDescription = "Ajouter au panier",
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = "${shopCat.price.toInt()}",
                                            textAlign = TextAlign.Center,
                                            fontSize = 16.sp,
                                        )

                                    }
                                }
                            }
                        }
                    }
                }
            }
        },

        bottomBar = {
            AppBottomBar(
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(100.dp)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ShopContentPreview() {
    CatwalkTheme {
        ShopContent()
    }
}
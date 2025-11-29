// kotlin
package uqac.catwalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import uqac.catwalk.ui.theme.CatwalkTheme
import androidx.compose.foundation.layout.padding
import uqac.catwalk.sauvegarde.AppDatabase
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// Dans CatInteractionActivity.kt

data class Heart(val id: Long, val x: Float, val y: Float)

class CatInteractionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Récupérer l'ID depuis l'Intent
        val catId = intent.getIntExtra("catID", -1) // Utilisez -1 ou une autre valeur par défaut invalide

        // Sécurité : si l'ID est invalide, on ne peut rien faire.
        if (catId == -1) {
            // Gérer l'erreur : fermer l'activité, afficher un message, etc.
            finish()
            return
        }

        setContent {
            CatwalkTheme {
                // 2. Récupérer les données du chat depuis la BDD
                val database = AppDatabase.getDatabase(context = this)
                val catDao = database.CatDao()
                // Idéalement, utilisez un ViewModel ici pour une meilleure architecture.
                // findById devrait retourner un Flow pour des mises à jour en temps réel.
                val cat by catDao.getCatById(catId).collectAsState(initial = null)

                // 3. Afficher le contenu uniquement quand le chat est chargé
                cat?.let { loadedCat ->
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        // Passez 'loadedCat' à vos Composables
                        CatInteractionContent(
                            cat = loadedCat,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } ?: run {
                    // Optionnel : Afficher un indicateur de chargement pendant que 'cat' est null
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        // CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun CatInteractionContent(modifier: Modifier = Modifier, cat: Cat?) {
    val context = LocalContext.current

    // Créer une instance de catDao dans le composable
    val database = AppDatabase.getDatabase(context)
    val catDao = database.CatDao()

    var proprete by remember { mutableIntStateOf(80) }
    var amusement by remember { mutableIntStateOf(60) }
    var affection by remember { mutableIntStateOf(2) }

    var isWashing by remember { mutableStateOf(false) }
    var isPetting by remember { mutableStateOf(false) }

    // Bounds du chat calculés sur l'image principale (coordonnées fenêtre)
    var catBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    Scaffold()(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
        topBar = {
            AppTopBar(
                coinAmount = 56.toString(),
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(80.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Nom du chat
            Text(
                text = cat?.name ?: "Chat Inconnu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            // Barre d’affection (coeurs)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                repeat(3) { index ->
                    val heartIcon = if (index (cat?.affection ?: 0).toInt()) {
                        painterResource(R.drawable.ic_heart_full)
                    } else
                        painterResource(R.drawable.ic_heart_empty)
                    Image(
                        painter = heartIcon,
                        contentDescription = "Cœur ${index + 1}",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                    )
                }
            }

            // Image du chat
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
            Image(
                painter = painterResource(R.drawable.chat_roux),
                contentDescription = "Chat",
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp)
                    .onGloballyPositioned { layout ->
                    val pos = layout.positionInWindow()
                    catBounds = Rect(
                        pos.x,
                        pos.y,
                        pos.x + layout.size.width,
                        pos.y + layout.size.height
                    )
                                          },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Propreté", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                    progress = {cat?.cleanliness?.div(100f) ?: 0f},
                    modifier = Modifier
                                                .width(130.dp)
                                                .height(10.dp)
                                                .padding(top = 4.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFC8E6C9),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amusement", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                    progress = { cat?.happiness?.div(100f) ?: 0f },
                    modifier = Modifier
                                                .width(130.dp)
                                                .height(10.dp)
                                                .padding(top = 4.dp),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFFFE0B2),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }

                // Footer : boutons d’action
                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Play section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                        cat?.let { nonNullCat ->
                            CoroutineScope(Dispatchers.IO).launch {
                                catDao.updateCatHappiness(
                                    id = nonNullCat.id,
                                    happiness = min(nonNullCat.happiness + 10, 100)
                                )
                            }
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Jouer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Jouer", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                    )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            cat?.let { nonNullCat ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    catDao.updateCatCleanliness(
                                        id = nonNullCat.id,
                                        cleanliness = min(nonNullCat.cleanliness + 10, 100),
                                        isWashing = true
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Laver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Laver", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                    )

                // Caresser
                    Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            cat?.let { nonNullCat ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    catDao.updateCatAffection(
                                        id = nonNullCat.id,
                                        affection = min(nonNullCat.affection + 0.1, 3.0),
                                        isPetting = true
                                    )
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = "Caresser",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Caresser", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
            // Overlay mode lavage
            if (isWashing) {
                WashingOverlay(
                    initialProprete = proprete,
                    catBounds = catBounds,
                    onPropreteChange = { proprete = it },
                    onClose = { isWashing = false }
                )
            }
            // Overlay mode caresse
            if (isPetting) {
                PettingOverlay(
                    catBounds = catBounds,
                    onAffectionChange = { affection = (affection + it).coerceAtMost(100) },
                    onClose = { isPetting = false }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatInteractionPreview() {
    CatwalkTheme {
        CatInteractionContent(
            modifier = Modifier,
            cat = null
        )
    }
}

// kotlin
package uqac.catwalk

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.graphicsLayer
import uqac.catwalk.sauvegarde.AppDatabase
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uqac.catwalk.sauvegarde.entities.Cat


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

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
        topBar = {
            AppTopBar(
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(80.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
                .padding(top = 8.dp)
        ) {
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
                    val heartIcon = if (index < (cat?.affection ?: 0).toInt()) {
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
                                        cleanliness = min(nonNullCat.cleanliness + 10, 100)
                                    )
                                }
                            }
                            isWashing = true
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
                                        affection = min(nonNullCat.affection + 0.1, 3.0)
                                    )
                                }
                            }
                            isPetting = true
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

@Composable
private fun HeartItem(
    startX: Float,
    startY: Float,
    onFinished: (Long) -> Unit
) {
    val id = remember { System.nanoTime() }
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }
    val heartSize = 200.dp

    LaunchedEffect(Unit) {
        animY.animateTo(-80f, animationSpec = tween(durationMillis = 700))
        animAlpha.animateTo(0f, animationSpec = tween(durationMillis = 9300))
        delay(100)
        onFinished(id)
    }

    Image(
        painter = painterResource(R.drawable.petits_coeurs),
        contentDescription = "Cœur",
        modifier = Modifier
            .offset { IntOffset((startX).toInt()-300, (startY + animY.value).toInt()) }
            .size(heartSize)
            .graphicsLayer { alpha = animAlpha.value }
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WashingOverlay(
    initialProprete: Int,
    catBounds: Rect,
    onPropreteChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    var proprete by remember { mutableIntStateOf(initialProprete) }

    // Position de l'éponge relative au coin supérieur gauche du parent
    var spongeX by remember { mutableFloatStateOf(50f) }
    var spongeY by remember { mutableFloatStateOf(50f) }

    // Position de l'overlay dans la fenêtre
    var overlayPosX by remember { mutableFloatStateOf(0f) }
    var overlayPosY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spongeSizePx = with(density) { 120.dp.toPx() }

    // Quand on a les bounds du chat et la position mesurée de l'overlay, recentre l'éponge dessus
    LaunchedEffect(catBounds, overlayPosX, overlayPosY) {
        if (catBounds.width > 0f && catBounds.height > 0f && (overlayPosX != 0f || overlayPosY != 0f)) {
            val catCenterX = catBounds.left + catBounds.width / 2f
            val catCenterY = catBounds.top + catBounds.height / 2f
            spongeX = catCenterX - overlayPosX - spongeSizePx / 2f
            spongeY = catCenterY - overlayPosY - spongeSizePx / 2f
        }
    }

    // Boîte transparente
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { layout ->
                val pos = layout.positionInWindow()
                overlayPosX = pos.x
                overlayPosY = pos.y
            }
    ) {
        // Éponge affichée au‑dessus, déplaçable
        Image(
            painter = painterResource(R.drawable.eponge),
            contentDescription = "Éponge",
            modifier = Modifier
                .size(120.dp)
                .offset { IntOffset(spongeX.toInt(), spongeY.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        spongeX += dragAmount.x
                        spongeY += dragAmount.y

                        // convertit rectangle de l'éponge en coordonnées fenêtre
                        val spongeRectWindow = Rect(
                            spongeX + overlayPosX,
                            spongeY + overlayPosY,
                            spongeX + overlayPosX + spongeSizePx,
                            spongeY + overlayPosY + spongeSizePx
                        )

                        // détection contact éponge/chat en coordonnées fenêtre
                        if (spongeRectWindow.overlaps(catBounds)) {
                            proprete = (proprete + 1).coerceAtMost(100)
                            onPropreteChange(proprete)
                        }
                    }
                }
        )

        // Bouton Terminer — en bas au centre
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
        ) {
            Text("Terminer")
        }
    }
}

@Composable
fun PettingOverlay(
    catBounds: Rect,
    onAffectionChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    var overlayPosX by remember { mutableFloatStateOf(0f) }
    var overlayPosY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val handSizePx = with(density) { 120.dp.toPx() }
    var handX by remember { mutableFloatStateOf(50f) }
    var handY by remember { mutableFloatStateOf(50f) }

    // liste de coeurs à afficher
    val hearts = remember { mutableStateListOf<Heart>() }

    // cooldown pour éviter plusieurs ajouts trop rapides
    var lastPetTime by remember { mutableLongStateOf(0L) }
    val cooldownMs = 500L

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { layout ->
                val pos = layout.positionInWindow()
                overlayPosX = pos.x
                overlayPosY = pos.y
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val windowX = tapOffset.x + overlayPosX
                    val windowY = tapOffset.y + overlayPosY
                    val now = System.currentTimeMillis()
                    if (now - lastPetTime > cooldownMs && catBounds.contains(Offset(windowX, windowY))) {
                        lastPetTime = now
                        // positionner le coeur au dessus de la tête du chat (en coords overlay)
                        val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                        val heartY = (catBounds.top) - overlayPosY - 40f
                        hearts.add(Heart(System.nanoTime(), heartX, heartY))
                        onAffectionChange.invoke(1)
                    }
                }
            }
    ) {
        // Main déplaçable (remplacée par une icône pour éviter une drawable manquante)
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = "Main",
            tint = Color.Red,
            modifier = Modifier
                .size(64.dp)
                .offset { IntOffset(handX.toInt(), handY.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        handX += dragAmount.x
                        handY += dragAmount.y

                        // rectangle de la main en coords fenêtre
                        val handRect = Rect(
                            handX + overlayPosX,
                            handY + overlayPosY,
                            handX + overlayPosX + handSizePx,
                            handY + overlayPosY + handSizePx
                        )

                        val now = System.currentTimeMillis()
                        if (now - lastPetTime > cooldownMs && handRect.overlaps(catBounds)) {
                            lastPetTime = now
                            // créer coeur au dessus du chat
                            val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                            val heartY = (catBounds.top) - overlayPosY - 40f
                            hearts.add(Heart(System.nanoTime(), heartX, heartY))
                            onAffectionChange.invoke(1)
                        }
                    }
                }
        )

        // Rendu des coeurs animés
        hearts.forEach { heart ->
            key(heart.id) {
                HeartItem(
                    startX = heart.x,
                    startY = heart.y,
                    onFinished = { finishedId ->
                        hearts.removeAll { it.id == finishedId }
                    }
                )
            }
        }

        // Bouton pour fermer
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
        ) {
            Text("Terminer")
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

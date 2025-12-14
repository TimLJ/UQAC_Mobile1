// kotlin
package uqac.catwalk

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.entities.Cat
import uqac.catwalk.ui.animation.CongratsDialog
import uqac.catwalk.ui.theme.CatwalkTheme

data class Heart(val id: Long, val x: Float, val y: Float)

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun catPainter(colorName: String?, @androidx.annotation.DrawableRes fallback: Int = R.drawable.chat_roux): androidx.compose.ui.graphics.painter.Painter {
    val context = LocalContext.current
    val resName = colorName?.substringBefore('.') ?: ""
    val resId = remember(resName) {
        if (resName.isBlank()) fallback
        else context.resources.getIdentifier(resName, "drawable", context.packageName)
            .takeIf { it != 0 } ?: fallback
    }
    return painterResource(id = resId)
}

class CatInteractionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val catId = intent?.getIntExtra("catID", -1)
        if (catId == null || catId == -1) {
            finish()
            return
        }

        setContent {
            // Récupérer les données du chat depuis la BDD
            val database = AppDatabase.getDatabase(context = this)
            val catDao = database.CatDao()
            val cat by catDao.getCatById(catId).collectAsState(initial = null)

            val DECAY_PER_DAY = 5 // Valeur de dégradation par jour
            val DAY_MS = 24*60*60*1000 // Millisecondes dans un jour
            val scope = rememberCoroutineScope() // CoroutineScope pour les opérations BDD

            LaunchedEffect(cat) {
                val loaded = cat ?: return@LaunchedEffect
                val now = System.currentTimeMillis() // temps actuel en ms
                val days = ((now - loaded.lastDecayAt) / DAY_MS).toInt() // jours depuis la dernière dégradation
                if (days > 0) { // appliquer la dégradation si au moins un jour s'est écoulé
                    val decay = days * DECAY_PER_DAY // dégradation totale
                    // nouvelles valeurs de propreté et d'amusement
                    val newCleanliness = max(0, loaded.cleanliness - decay)
                    val newHappiness = max(0, loaded.happiness - decay)

                    // calculer affection liée à la fréquence de visite
                    val visitInc = when {
                        // plus de 7 jours depuis la dernière visite
                        now - loaded.lastSeenAt > 7 * DAY_MS -> 0.03f
                        // entre 3 et 7 jours
                        now - loaded.lastSeenAt > DAY_MS -> 0.01f
                        else -> 0f
                    }

                    // pénalité si propreté / amusement trop bas
                    val penalty = (if (newCleanliness < 50) 0.2f else 0f) + (if (newHappiness < 50) 0.2f else 0f)

                    val newAffection = (loaded.affection + visitInc - penalty).coerceIn(0f, 0.4f)

                    scope.launch(Dispatchers.IO) {
                        catDao.updateCatCleanlinessAndLastDecay(loaded.id, newCleanliness, now)
                        catDao.updateCatHappinessAndLastDecay(loaded.id, newHappiness, now)
                        catDao.updateCatAffectionAndLastSeen(loaded.id, newAffection, now)
                    }
                }
            }
            // Afficher le contenu uniquement quand le chat est chargé
            CatwalkTheme {
                val database = AppDatabase.getDatabase(context = this)
                val catDao = database.CatDao()
                val cat by catDao.getCatById(catId).collectAsState(initial = null)

                cat?.let { loadedCat ->
                    CatInteractionContent(
                        cat = loadedCat,
                        modifier = Modifier
                    )
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

// composable that composes the screen from smaller parts
@SuppressLint("UseKtx")
@Composable
fun CatInteractionContent(modifier: Modifier = Modifier, cat: Cat) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val catDao = database.CatDao()
    val scope = rememberCoroutineScope()

    val catName = cat.name ?: "Chat Inconnu"
    // local states for cleanliness and happiness
    var proprete by remember { mutableStateOf(cat.cleanliness) }
    var amusement by remember { mutableStateOf(cat.happiness) }
    var affection by remember { mutableStateOf(cat.affection) }

    // States pour les dialogues de félicitations
    var showCongratsDialog by remember { mutableStateOf(false) }
    var congratsShownForSession by remember { mutableStateOf(false) }

    // états précédents pour détecter une montée jusqu'au palier
    var prevProprete by remember { mutableStateOf(cat.cleanliness) }
    var prevAmusement by remember { mutableStateOf(cat.happiness) }

    // Synchroniser les états locaux quand la BDD renvoie une nouvelle valeur
    LaunchedEffect(cat.cleanliness) { proprete = cat.cleanliness }
    LaunchedEffect(cat.happiness) { amusement = cat.happiness }
    LaunchedEffect(cat.affection) { affection = cat.affection }

    // Déclencheur qui affiche la boîte de dialogue quand les deux stats sont au maximum
    LaunchedEffect(proprete, amusement) {
        val cleaned = proprete >= 290
        val played = amusement >= 290

        val reachedByIncrease =
            (prevProprete < 290 && proprete >= 290) ||
                    (prevAmusement < 290 && amusement >= 290)

        if (cleaned && played && !congratsShownForSession && reachedByIncrease) {
            congratsShownForSession = true
            showCongratsDialog = true

            val newAff = (affection + 0.1f).coerceIn(0f, 0.1f)
            affection = newAff
            scope.launch(Dispatchers.IO) {
                catDao.updateCatAffectionAndLastSeen(
                    cat.id,
                    newAff,
                    System.currentTimeMillis()
                )
            }
        }

        if (!cleaned || !played) {
            congratsShownForSession = false
        }

        prevProprete = proprete
        prevAmusement = amusement
    }


    var isWashing by remember { mutableStateOf(false) }
    var isPetting by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

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
        },
        bottomBar = {
            // Barre d'actions placée dans bottomBar pour que Scaffold réserve l'espace correctement
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(100.dp)
                    .background(colorResource(R.color.orange))
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            isPlaying = true
                            isWashing = false
                            isPetting = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Jouer",
                            tint = colorResource(R.color.black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Jouer", color = colorResource(R.color.black))
                    }
                }

                VerticalDivider(
                    color = colorResource(R.color.black),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            isWashing = true
                            isPetting = false
                            isPlaying = false
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
                            tint = colorResource(R.color.black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Laver", color = colorResource(R.color.black))
                    }
                }

                VerticalDivider(
                    color = colorResource(R.color.black),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            isPetting = true
                            isWashing = false
                            isPlaying = false
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
                            tint = colorResource(R.color.black)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Caresser", color = colorResource(R.color.black))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    top = paddingValues.calculateTopPadding() + 8.dp, // ajoute +8.dp si tu veux un espace supplémentaire
                    end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // Bouton Retour
            IconButton(
                onClick = {
                    (context as? ComponentActivity)
                        ?.onBackPressedDispatcher
                        ?.onBackPressed()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            // Contenu principal
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nom du chat
                Text(
                    text = catName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
                // Cœurs d'affection
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    repeat(3) { index ->
                        val heartIcon = if (index < (affection * 3)) // comparer sur 3 coeurs
                            painterResource(R.drawable.full_heart)
                        else
                            painterResource(R.drawable.empty_heart)
                        Image(
                            painter = heartIcon,
                            contentDescription = "Cœur ${index + 1}",
                            modifier = Modifier
                                .size(36.dp)
                                .padding(4.dp)
                        )
                    }
                }
                // Image du chat avec saleté superposée
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = catPainter(cat.color),
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
                    if (proprete < 100) {
                        Image(
                            painter = painterResource(R.drawable.salete3),
                            contentDescription = "Saleté",
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (proprete < 200) {
                        Image(
                            painter = painterResource(R.drawable.salete2),
                            contentDescription = "Saleté",
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (proprete < 290) {
                        Image(
                            painter = painterResource(R.drawable.salete1),
                            contentDescription = "Saleté",
                            modifier = Modifier
                                .size(300.dp)
                                .padding(16.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
                CongratsDialog(
                    visible = showCongratsDialog,
                    onDismiss = { showCongratsDialog = false }
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
                            progress = { proprete / 300f },
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
                            progress = { amusement / 300f },
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
                Spacer(modifier = Modifier.height(8.dp)) // espace avant le bottomBar réservé par Scaffold
            }

            if (isWashing) {
                isPetting = false
                isPlaying = false
                WashingOverlay(
                    initialProprete = proprete,
                    catBounds = catBounds,
                    onPropreteChange = { newCleanliness ->
                        proprete = newCleanliness
                        scope.launch(Dispatchers.IO) {
                            catDao.updateCatCleanlinessAndLastDecay(id = cat.id, cleanliness = newCleanliness, lastDecayAt = System.currentTimeMillis())
                        }
                    },
                    onClose = { isWashing = false }
                )

            }

            if (isPetting) {
                isWashing = false
                isPlaying = false
                PettingOverlay(
                    catBounds = catBounds,
                    onAffectionIncrease = { delta ->
                        affection = (affection + delta).coerceIn(0f, 0.2f)
                        scope.launch(Dispatchers.IO) {
                            val now = System.currentTimeMillis()
                            catDao.updateCatAffectionAndLastSeen(cat.id, affection, now)
                        }
                    },
                    onClose = { isPetting = false }
                )
            }

            if (isPlaying) {
                isWashing = false
                isPetting = false
                PlayingOverlay(
                    initialAmusement = amusement,
                    catBounds = catBounds,
                    onAmusementChange = { newAmusement ->
                        amusement = newAmusement
                        scope.launch(Dispatchers.IO) {
                            catDao.updateCatHappinessAndLastDecay(id = cat.id, happiness = newAmusement, lastDecayAt = System.currentTimeMillis())
                        }
                    },
                    onClose = { isPlaying = false }
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
        contentDescription = "Cœurs",
        modifier = Modifier
            .offset { IntOffset(startX.toInt(), (startY + animY.value).toInt()) }
            .size(heartSize)
            .graphicsLayer { alpha = animAlpha.value }
    )
}

@Composable
fun InteractionOverlay(
    catBounds: Rect,
    handDrawable: Int,
    handSizeDp: Dp = 120.dp,
    soundName: String? = null,
    cooldownMs: Long = 0L,
    onHit: (
        windowHandRect: Rect,
        addEffect: (Heart) -> Unit
    ) -> Unit = { _, _ -> },
    effects: List<Heart> = emptyList(),
    onEffectsChange: (List<Heart>) -> Unit = {},
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var overlayPosX by remember { mutableFloatStateOf(0f) }
    var overlayPosY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val handSizePx = with(density) { handSizeDp.toPx() }
    var handX by remember { mutableFloatStateOf(50f) }
    var handY by remember { mutableFloatStateOf(50f) }

    var lastHitTime by remember { mutableLongStateOf(0L) }

    // état du son
    var isSoundPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isSoundPlaying) {
                SoundPlayer.stop()
                isSoundPlaying = false
            }
        }
    }

    fun handleHit(rect: Rect) {
        val now = System.currentTimeMillis()
        if (now - lastHitTime > cooldownMs) {
            lastHitTime = now
            onHit(rect) { heart ->
                onEffectsChange(effects + heart)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
            .padding(top = 60.dp)
            .onGloballyPositioned {
                val pos = it.positionInWindow()
                overlayPosX = pos.x
                overlayPosY = pos.y
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { tapOffset ->
                        handX = tapOffset.x - handSizePx / 2f
                        handY = tapOffset.y - handSizePx / 2f

                        val rect = Rect(
                            handX + overlayPosX,
                            handY + overlayPosY,
                            handX + overlayPosX + handSizePx,
                            handY + overlayPosY + handSizePx
                        )

                        if (rect.overlaps(catBounds)) {
                            handleHit(rect)

                            if (soundName != null && !isSoundPlaying) {
                                SoundPlayer.start(context, soundName, loop = true)
                                isSoundPlaying = true
                            }
                        }

                        tryAwaitRelease()

                        if (isSoundPlaying) {
                            SoundPlayer.stop()
                            isSoundPlaying = false
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handX = offset.x - handSizePx / 2f
                        handY = offset.y - handSizePx / 2f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        handX += dragAmount.x
                        handY += dragAmount.y

                        val rect = Rect(
                            handX + overlayPosX,
                            handY + overlayPosY,
                            handX + overlayPosX + handSizePx,
                            handY + overlayPosY + handSizePx
                        )

                        if (rect.overlaps(catBounds)) {
                            handleHit(rect)

                            if (soundName != null && !isSoundPlaying) {
                                SoundPlayer.start(context, soundName, loop = true)
                                isSoundPlaying = true
                            }
                        }
                    },
                    onDragEnd = {
                        if (isSoundPlaying) {
                            SoundPlayer.stop()
                            isSoundPlaying = false
                        }
                    },
                    onDragCancel = {
                        if (isSoundPlaying) {
                            SoundPlayer.stop()
                            isSoundPlaying = false
                        }
                    }
                )
            }
    ) {
        Image(
            painter = painterResource(handDrawable),
            contentDescription = null,
            modifier = Modifier
                .size(handSizeDp)
                .offset { IntOffset(handX.toInt(), handY.toInt()) }
        )

        // Affichage des effets (ex: cœurs)
        effects.forEach { heart ->
            key(heart.id) {
                HeartItem(
                    startX = heart.x - overlayPosX - 290f,
                    startY = heart.y - overlayPosY,
                    onFinished = { finishedId ->
                        onEffectsChange(effects.filter { it.id != finishedId })
                    }
                )
            }
        }

        Button(
            onClick = {
                if (isSoundPlaying) {
                    SoundPlayer.stop()
                }
                onClose()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Text("Terminer")
        }
    }
}


@Composable
fun WashingOverlay(
    initialProprete: Int,
    catBounds: Rect,
    onPropreteChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    var proprete by remember { mutableIntStateOf(initialProprete) }

    InteractionOverlay(
        catBounds = catBounds,
        handDrawable = R.drawable.eponge,
        handSizeDp = 120.dp,
        soundName = "sponge",
        cooldownMs = 0L,
        effects = emptyList(),
        onEffectsChange = {},
        onHit = { _, _ ->
            proprete = (proprete + 1).coerceAtMost(300)
            onPropreteChange(proprete)
        },
        onClose = onClose
    )
}


@Composable
fun PettingOverlay(
    catBounds: Rect,
    onClose: () -> Unit,
    onAffectionIncrease: (Float) -> Unit = {},
) {
    val hearts = remember { mutableStateListOf<Heart>() }

    InteractionOverlay(
        catBounds = catBounds,
        handDrawable = R.drawable.main,
        handSizeDp = 64.dp,
        soundName = "ronronnement",
        cooldownMs = 500L,
        effects = hearts,
        onEffectsChange = { newList ->
            hearts.clear()
            hearts.addAll(newList)
        },
        onHit = { _, addEffect ->
            val heart = Heart(
                id = System.nanoTime(),
                x = (catBounds.left + catBounds.width / 2f),
                y = (catBounds.top - 40f)
            )
            addEffect(heart)
            onAffectionIncrease(0.05f)
        },
        onClose = onClose
    )
}


@Composable
fun PlayingOverlay(
    initialAmusement: Int,
    catBounds: Rect,
    onAmusementChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    var amusement by remember { mutableIntStateOf(initialAmusement) }

    InteractionOverlay(
        catBounds = catBounds,
        handDrawable = R.drawable.plumeau,
        handSizeDp = 120.dp,
        soundName = "plumeau",
        cooldownMs = 0L,
        effects = emptyList(),
        onEffectsChange = {},
        onHit = { _, _ ->
            amusement = (amusement + 1).coerceAtMost(300)
            onAmusementChange(amusement)
        },
        onClose = onClose
    )
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatInteractionPreview() {
    CatwalkTheme {
        CatInteractionContent(
            modifier = Modifier,
            cat = Cat(
                name = "Minou",
                color = "chat_banc_noir.png",
                happiness = 50,
                cleanliness = 50,
                affection = 0.5f,
                achievementId = null,
                obtenu = true,
                price = 0.0,
                level = 1
            ),
        )
    }
}

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
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.entities.Cat
import uqac.catwalk.ui.theme.CatwalkTheme

data class Heart(val id: Long, val x: Float, val y: Float)

@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun catPainter(colorName: String?, @androidx.annotation.DrawableRes fallback: Int = R.drawable.chat_roux): androidx.compose.ui.graphics.painter.Painter {
    val context = LocalContext.current
    val resName = colorName?.substringBefore('.') ?: ""
    val resId = remember(resName) {
        if (resName.isBlank()) fallback
        else {
            context.resources.getIdentifier(resName, "drawable", context.packageName)
                .takeIf { it != 0 } ?: fallback
        }
    }
    return painterResource(id = resId)
}

class CatInteractionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val catId = intent?.getIntExtra("catID",-1)
        if (catId == -1 || catId == null) {
            // Gérer l'erreur : fermer l'activité, afficher un message, etc.
            finish()
            return
        }
        setContent {
            // Récupérer les données du chat depuis la BDD
            val database = AppDatabase.getDatabase(context = this)
            val catDao = database.CatDao()
            val cat by catDao.getCatById(catId).collectAsState(initial = null)

            CatwalkTheme {
                // Afficher le contenu uniquement quand le chat est chargé
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

@Composable
fun CatInteractionContent(modifier: Modifier = Modifier, cat: Cat) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val catDao = database.CatDao()
    val scope = rememberCoroutineScope()

    val catName = cat.name
    var proprete = cat.cleanliness
    var amusement = cat.happiness
    var affection = cat.affection

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
                    text = catName ?: "Chat Inconnu",
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
                        val heartIcon = if (index < affection)
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
                        scope.launch(Dispatchers.IO) {
                            catDao.updateCatCleanliness(id = cat.id, cleanliness = newCleanliness)
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
                        scope.launch(Dispatchers.IO) {
                            catDao.updateCatHappiness(id = cat.id, happiness = newAmusement)
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
    onClose: () -> Unit
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
                obtenu = true
            ),
        )
    }
}

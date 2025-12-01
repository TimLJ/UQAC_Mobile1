package uqac.catwalk

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
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import uqac.catwalk.ui.theme.CatwalkTheme

data class Heart(val id: Long, val x: Float, val y: Float)

class CatInteractionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val catName = intent?.getStringExtra("catName") ?: "Inconnu"
        setContent {
            CatwalkTheme {
                CatInteractionContent(catName = catName)
            }
        }
    }
}

@Composable
fun CatInteractionContent(modifier: Modifier = Modifier, catName: String) {
    val context = LocalContext.current

    var proprete by remember { mutableIntStateOf(80) }
    var amusement by remember { mutableIntStateOf(60) }
    var affection by remember { mutableIntStateOf(2) }

    var isWashing by remember { mutableStateOf(false) }
    var isPetting by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(innerPadding)
            .padding(top = 8.dp)
        ) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = catName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    repeat(3) { index ->
                        val heartIcon = if (index < affection)
                            painterResource(R.drawable.ic_heart_full)
                        else
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

                // Image du chat principale
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
                        progress = { proprete / 100f },
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
                        progress = { amusement / 100f },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Bouton Jouer -> active le mode jeu
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { isPlaying = true },
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

                    // Bouton Laver -> active le mode lavage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { isWashing = true },
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
                            .clickable { isPetting = true },
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
                isPetting = false
                WashingOverlay(
                    initialProprete = proprete,
                    catBounds = catBounds,
                    onPropreteChange = { proprete = it },
                    onClose = { isWashing = false }
                )
            }
            // Overlay mode caresse
            if (isPetting) {
                isWashing = false
                PettingOverlay(
                    catBounds = catBounds,
                    onClose = { isPetting = false }
                )
            }

            // Overlay mode jeu
            if (isPlaying) {
                PlayingOverlay(
                    initialAmusement = amusement,
                    catBounds = catBounds,
                    onAmusementChange = { amusement = it },
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
            .offset { IntOffset((startX).toInt()-300, (startY + animY.value).toInt()) }
            .size(heartSize)
            .graphicsLayer { alpha = animAlpha.value }
    )
}

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

    // Boîte transparente — capte taps et drags sur toute la zone
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { layout ->
                val pos = layout.positionInWindow()
                overlayPosX = pos.x
                overlayPosY = pos.y
            }
            .pointerInput(Unit) {
                // positionne à l'appui et suit le doigt pendant le drag
                detectTapGestures(
                    onPress = { offset ->
                        // offset est local au Box
                        spongeX = offset.x - spongeSizePx / 2f
                        spongeY = offset.y - spongeSizePx / 2f

                        // convertit rectangle de l'éponge en coordonnées fenêtre
                        val spongeRectWindow = Rect(
                            spongeX + overlayPosX,
                            spongeY + overlayPosY,
                            spongeX + overlayPosX + spongeSizePx,
                            spongeY + overlayPosY + spongeSizePx
                        )

                        if (spongeRectWindow.overlaps(catBounds)) {
                            proprete = (proprete + 1).coerceAtMost(100)
                            onPropreteChange(proprete)
                        }

                        tryAwaitRelease() // attend le release si nécessaire
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // centrer l'éponge sous le doigt au démarrage du drag
                        spongeX = offset.x - spongeSizePx / 2f
                        spongeY = offset.y - spongeSizePx / 2f
                    },
                    onDrag = { change, dragAmount ->
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
                )
            }
    ) {
        // Éponge affichée au‑dessus
        Image(
            painter = painterResource(R.drawable.eponge),
            contentDescription = "Éponge",
            modifier = Modifier
                .size(120.dp)
                .offset { IntOffset(spongeX.toInt(), spongeY.toInt()) }
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
                // tap pour positionner la main et créer un coeur si sur le chat
                detectTapGestures(
                    onPress = { tapOffset ->
                        val windowX = tapOffset.x + overlayPosX
                        val windowY = tapOffset.y + overlayPosY
                        val now = System.currentTimeMillis()
                        // positionne la main centrée sous le doigt
                        handX = tapOffset.x - handSizePx / 2f
                        handY = tapOffset.y - handSizePx / 2f

                        if (now - lastPetTime > cooldownMs && catBounds.contains(Offset(windowX, windowY))) {
                            lastPetTime = now
                            val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                            val heartY = (catBounds.top) - overlayPosY - 40f
                            hearts.add(Heart(System.nanoTime(), heartX, heartY))
                        }

                        tryAwaitRelease()
                    }
                )
            }
            .pointerInput(Unit) {
                // drag pour suivre le doigt et générer des coeurs en collision
                detectDragGestures(
                    onDragStart = { offset ->
                        handX = offset.x - handSizePx / 2f
                        handY = offset.y - handSizePx / 2f
                    },
                    onDrag = { change, dragAmount ->
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
                            val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                            val heartY = (catBounds.top) - overlayPosY - 40f
                            hearts.add(Heart(System.nanoTime(), heartX, heartY))
                        }
                    }
                )
            }
    ) {
        // Main déplaçable affichée (positionnée par le Box pointerInput)
        Image(
            painter = painterResource(R.drawable.main),
            contentDescription = "Main",
            modifier = Modifier
                .size(64.dp)
                .offset { IntOffset(handX.toInt(), handY.toInt()) }
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

@Composable
fun PlayingOverlay(
    initialAmusement: Int,
    catBounds: Rect,
    onAmusementChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    var amusement by remember { mutableIntStateOf(initialAmusement) }

    // Position du plumeau relative au coin supérieur gauche du parent
    var toyX by remember { mutableFloatStateOf(50f) }
    var toyY by remember { mutableFloatStateOf(50f) }

    // Position de l'overlay dans la fenêtre
    var overlayPosX by remember { mutableFloatStateOf(0f) }
    var overlayPosY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val toySizePx = with(density) { 120.dp.toPx() }

    // Quand on a les bounds du chat et la position mesurée de l'overlay, recentre le plumeau dessus
    LaunchedEffect(catBounds, overlayPosX, overlayPosY) {
        if (catBounds.width > 0f && catBounds.height > 0f && (overlayPosX != 0f || overlayPosY != 0f)) {
            val catCenterX = catBounds.left + catBounds.width / 2f
            val catCenterY = catBounds.top + catBounds.height / 2f
            toyX = catCenterX - overlayPosX - toySizePx / 2f
            toyY = catCenterY - overlayPosY - toySizePx / 2f
        }
    }

    // Boîte transparente — capte taps et drags sur toute la zone
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { layout ->
                val pos = layout.positionInWindow()
                overlayPosX = pos.x
                overlayPosY = pos.y
            }
            .pointerInput(Unit) {
                // positionne à l'appui et suit le doigt pendant le drag
                detectTapGestures(
                    onPress = { offset ->
                        // offset est local au Box
                        toyX = offset.x - toySizePx / 2f
                        toyY = offset.y - toySizePx / 2f

                        // convertit rectangle de l'éponge en coordonnées fenêtre
                        val toyRectWindow = Rect(
                            toyX + overlayPosX,
                            toyY + overlayPosY,
                            toyX + overlayPosX + toySizePx,
                            toyY + overlayPosY + toySizePx
                        )

                        if (toyRectWindow.overlaps(catBounds)) {
                            amusement = (amusement + 1).coerceAtMost(100)
                            onAmusementChange(amusement)
                        }

                        tryAwaitRelease() // attend le release si nécessaire
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // centrer le plumeau sous le doigt au démarrage du drag
                        toyX = offset.x - toySizePx / 2f
                        toyY = offset.y - toySizePx / 2f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        toyX += dragAmount.x
                        toyY += dragAmount.y

                        // convertit rectangle du plumeau en coordonnées fenêtre
                        val toyRectWindow = Rect(
                            toyX + overlayPosX,
                            toyY + overlayPosY,
                            toyX + overlayPosX + toySizePx,
                            toyY + overlayPosY + toySizePx
                        )

                        // détection contact plumeau/chat en coordonnées fenêtre
                        if (toyRectWindow.overlaps(catBounds)) {
                            amusement = (amusement + 1).coerceAtMost(100)
                            onAmusementChange(amusement)
                        }
                    }
                )
            }
    ) {
        // Plumeau affichée au‑dessus
        Image(
            painter = painterResource(R.drawable.plumeau),
            contentDescription = "Plumeau",
            modifier = Modifier
                .size(120.dp)
                .offset { IntOffset(toyX.toInt(), toyY.toInt()) }
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatInteractionPreview() {
    CatwalkTheme {
        CatInteractionContent(
            modifier = Modifier,
            catName = "Minou"
        )
    }
}

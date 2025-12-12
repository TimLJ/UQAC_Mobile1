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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
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
            CatwalkTheme {
                val database = AppDatabase.getDatabase(context = this)
                val catDao = database.CatDao()
                val cat by catDao.getCatById(catId).collectAsState(initial = null)

                cat?.let { loadedCat ->
                    CatInteractionContent(cat = loadedCat)
                } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Placeholder while loading
                }
            }
        }
    }
}

// composable that composes the screen from smaller parts
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
    // affection
    val affectionInt = remember(cat.affection) { kotlin.runCatching { cat.affection.toInt() }.getOrDefault(0) }

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
            CatActionBar(
                onPlay = { isPlaying = true; isWashing = false; isPetting = false },
                onWash = { isWashing = true; isPetting = false; isPlaying = false },
                onPet = { isPetting = true; isWashing = false; isPlaying = false }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                top = paddingValues.calculateTopPadding() + 8.dp,
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
            IconButton(
                onClick = { (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = MaterialTheme.colorScheme.onBackground)
            }

            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                Text(text = catName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center)

                HeartsRow(affection = affectionInt)

                CatImageArea(cat = cat, catBoundsStateUpdater = { catBounds = it })

                StatsRow(proprete = proprete, amusement = amusement)

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Overlays
            if (isWashing) WashingOverlay(
                initialProprete = proprete,
                catBounds = catBounds,
                onPropreteChange = { new -> proprete = new; scope.launch(Dispatchers.IO) { catDao.updateCatCleanliness(id = cat.id, cleanliness = new) } },
                onClose = { isWashing = false })

            if (isPetting) PettingOverlay(catBounds = catBounds, onClose = { isPetting = false })

            if (isPlaying) PlayingOverlay(initialAmusement = amusement, catBounds = catBounds, onAmusementChange = { new -> amusement = new; scope.launch(Dispatchers.IO) { catDao.updateCatHappiness(id = cat.id, happiness = new) } }, onClose = { isPlaying = false })
        }
    }
}

//Smaller composables extracted for readability
@Composable
private fun CatActionBar(onPlay: () -> Unit, onWash: () -> Unit, onPet: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(100.dp)
            .background(colorResource(R.color.orange))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionItem(icon = Icons.Filled.Star, label = "Jouer", onClick = onPlay, modifier = Modifier.weight(1f))
        VerticalDivider(color = colorResource(R.color.black), thickness = 1.dp, modifier = Modifier.fillMaxHeight())
        ActionItem(icon = Icons.Filled.PlayArrow, label = "Laver", onClick = onWash, modifier = Modifier.weight(1f))
        VerticalDivider(color = colorResource(R.color.black), thickness = 1.dp, modifier = Modifier.fillMaxHeight())
        ActionItem(icon = Icons.Filled.FavoriteBorder, label = "Caresser", onClick = onPet, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = colorResource(R.color.black))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = colorResource(R.color.black))
        }
    }
}

@Composable
private fun HeartsRow(affection: Int) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        repeat(3) { index ->
            val heartIcon = if (index < affection) painterResource(R.drawable.full_heart) else painterResource(R.drawable.empty_heart)
            Image(painter = heartIcon, contentDescription = "Cœur ${index + 1}", modifier = Modifier.size(36.dp).padding(4.dp))
        }
    }
}

@Composable
private fun CatImageArea(cat: Cat, catBoundsStateUpdater: (Rect) -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Image(painter = catPainter(cat.color), contentDescription = "Chat", modifier = Modifier.size(300.dp).padding(16.dp).onGloballyPositioned { layout ->
            val pos = layout.positionInWindow()
            catBoundsStateUpdater(Rect(pos.x, pos.y, pos.x + layout.size.width, pos.y + layout.size.height))
        }, contentScale = androidx.compose.ui.layout.ContentScale.Crop)

        val proprete = cat.cleanliness
        if (proprete < 100) Image(painter = painterResource(R.drawable.salete3), contentDescription = "Saleté", modifier = Modifier.size(300.dp).padding(16.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        else if (proprete < 150) Image(painter = painterResource(R.drawable.salete2), contentDescription = "Saleté", modifier = Modifier.size(300.dp).padding(16.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        else if (proprete < 225) Image(painter = painterResource(R.drawable.salete1), contentDescription = "Saleté", modifier = Modifier.size(300.dp).padding(16.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
    }
}

@Composable
private fun StatsRow(proprete: Int, amusement: Int) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Propreté", fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
            progress = { (proprete / 500f).coerceIn(0f, 1f) },
            modifier = Modifier.width(130.dp).height(10.dp).padding(top = 4.dp),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFFC8E6C9),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Amusement", fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
            progress = { (amusement / 300f).coerceIn(0f, 1f) },
            modifier = Modifier.width(130.dp).height(10.dp).padding(top = 4.dp),
            color = Color(0xFFFF9800),
            trackColor = Color(0xFFFFE0B2),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
    }
}

// --- Heart animation item (kept mostly as-is) ---
@Composable
private fun HeartItem(startX: Float, startY: Float, onFinished: (Long) -> Unit) {
    val id = remember { System.nanoTime() }
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }
    val heartSize = 200.dp

    LaunchedEffect(Unit) {
        animY.animateTo(-80f, animationSpec = tween(durationMillis = 700))
        animAlpha.animateTo(0f, animationSpec = tween(durationMillis = 930))
        delay(100)
        onFinished(id)
    }

    Image(painter = painterResource(R.drawable.petits_coeurs), contentDescription = "Cœurs", modifier = Modifier.offset { IntOffset((startX).toInt() - 300, (startY + animY.value).toInt()) }.size(heartSize).graphicsLayer { alpha = animAlpha.value })
}

// --- The overlays: Washing, Petting, Playing ---
// For the overlays the logic has been preserved but the code was cleaned (combined pointer inputs where appropriate, clarified state names)

@Composable
fun WashingOverlay(initialProprete: Int, catBounds: Rect, onPropreteChange: (Int) -> Unit, onClose: () -> Unit) {
    var proprete by remember { mutableStateOf(initialProprete) }
    var spongeX by remember { mutableStateOf(50f) }
    var spongeY by remember { mutableStateOf(50f) }
    var overlayPosX by remember { mutableStateOf(0f) }
    var overlayPosY by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val spongeSizePx = with(density) { 120.dp.toPx() }

    LaunchedEffect(catBounds, overlayPosX, overlayPosY) {
        if (catBounds.width > 0f && catBounds.height > 0f && (overlayPosX != 0f || overlayPosY != 0f)) {
            val catCenterX = catBounds.left + catBounds.width / 2f
            val catCenterY = catBounds.top + catBounds.height / 2f
            spongeX = catCenterX - overlayPosX - spongeSizePx / 2f
            spongeY = catCenterY - overlayPosY - spongeSizePx / 2f
        }
    }

    Box(Modifier.fillMaxSize().padding(bottom = 100.dp).padding(top = 60.dp).onGloballyPositioned { layout ->
        val pos = layout.positionInWindow()
        overlayPosX = pos.x
        overlayPosY = pos.y
    }.pointerInput(Unit) {
        detectTapGestures(onPress = { offset ->
            spongeX = offset.x - spongeSizePx / 2f
            spongeY = offset.y - spongeSizePx / 2f
            val spongeRectWindow = Rect(spongeX + overlayPosX, spongeY + overlayPosY, spongeX + overlayPosX + spongeSizePx, spongeY + overlayPosY + spongeSizePx)
            if (spongeRectWindow.overlaps(catBounds)) {
                proprete = (proprete + 1).coerceAtMost(100)
                onPropreteChange(proprete)
            }
            tryAwaitRelease()
        })
    }.pointerInput(Unit) {
        detectDragGestures(onDragStart = { offset ->
            spongeX = offset.x - spongeSizePx / 2f
            spongeY = offset.y - spongeSizePx / 2f
        }, onDrag = { change, dragAmount ->
            change.consume()
            spongeX += dragAmount.x
            spongeY += dragAmount.y
            val spongeRectWindow = Rect(spongeX + overlayPosX, spongeY + overlayPosY, spongeX + overlayPosX + spongeSizePx, spongeY + overlayPosY + spongeSizePx)
            if (spongeRectWindow.overlaps(catBounds)) {
                proprete = (proprete + 1).coerceAtMost(300)
                onPropreteChange(proprete)
            }
        })
    }) {
        Image(painter = painterResource(R.drawable.eponge), contentDescription = "Éponge", modifier = Modifier.size(120.dp).offset { IntOffset(spongeX.toInt(), spongeY.toInt()) })

        Button(onClick = onClose, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) {
            Text("Terminer")
        }
    }
}

@Composable
fun PettingOverlay(catBounds: Rect, onClose: () -> Unit) {
    var overlayPosX by remember { mutableStateOf(0f) }
    var overlayPosY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val handSizePx = with(density) { 120.dp.toPx() }
    var handX by remember { mutableStateOf(50f) }
    var handY by remember { mutableStateOf(50f) }
    val hearts = remember { mutableStateListOf<Heart>() }
    var lastPetTime by remember { mutableStateOf(0L) }
    val cooldownMs = 500L

    Box(Modifier.fillMaxSize().padding(bottom = 100.dp).padding(top = 60.dp).onGloballyPositioned { layout ->
        val pos = layout.positionInWindow()
        overlayPosX = pos.x
        overlayPosY = pos.y
    }.pointerInput(Unit) {
        detectTapGestures(onPress = { tapOffset ->
            val windowX = tapOffset.x + overlayPosX
            val windowY = tapOffset.y + overlayPosY
            val now = System.currentTimeMillis()
            handX = tapOffset.x - handSizePx / 2f
            handY = tapOffset.y - handSizePx / 2f
            if (now - lastPetTime > cooldownMs && catBounds.contains(Offset(windowX, windowY))) {
                lastPetTime = now
                val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                val heartY = (catBounds.top) - overlayPosY - 40f
                hearts.add(Heart(System.nanoTime(), heartX, heartY))
            }
            tryAwaitRelease()
        })
    }.pointerInput(Unit) {
        detectDragGestures(onDragStart = { offset ->
            handX = offset.x - handSizePx / 2f
            handY = offset.y - handSizePx / 2f
        }, onDrag = { change, dragAmount ->
            change.consume()
            handX += dragAmount.x
            handY += dragAmount.y
            val handRect = Rect(handX + overlayPosX, handY + overlayPosY, handX + overlayPosX + handSizePx, handY + overlayPosY + handSizePx)
            val now = System.currentTimeMillis()
            if (now - lastPetTime > cooldownMs && handRect.overlaps(catBounds)) {
                lastPetTime = now
                val heartX = (catBounds.left + catBounds.width / 2f) - overlayPosX
                val heartY = (catBounds.top) - overlayPosY - 40f
                hearts.add(Heart(System.nanoTime(), heartX, heartY))
            }
        })
    }) {
        Image(painter = painterResource(R.drawable.main), contentDescription = "Main", modifier = Modifier.size(64.dp).offset { IntOffset(handX.toInt(), handY.toInt()) })

        hearts.forEach { heart ->
            key(heart.id) {
                HeartItem(startX = heart.x, startY = heart.y, onFinished = { finishedId -> hearts.removeAll { it.id == finishedId } })
            }
        }

        Button(onClick = onClose, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) { Text("Terminer") }
    }
}

@Composable
fun PlayingOverlay(initialAmusement: Int, catBounds: Rect, onAmusementChange: (Int) -> Unit, onClose: () -> Unit) {
    var amusement by remember { mutableStateOf(initialAmusement) }
    var toyX by remember { mutableStateOf(50f) }
    var toyY by remember { mutableStateOf(50f) }
    var overlayPosX by remember { mutableStateOf(0f) }
    var overlayPosY by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val toySizePx = with(density) { 120.dp.toPx() }

    LaunchedEffect(catBounds, overlayPosX, overlayPosY) {
        if (catBounds.width > 0f && catBounds.height > 0f && (overlayPosX != 0f || overlayPosY != 0f)) {
            val catCenterX = catBounds.left + catBounds.width / 2f
            val catCenterY = catBounds.top + catBounds.height / 2f
            toyX = catCenterX - overlayPosX - toySizePx / 2f
            toyY = catCenterY - overlayPosY - toySizePx / 2f
        }
    }

    Box(Modifier.fillMaxSize().padding(bottom = 100.dp).padding(top = 60.dp).onGloballyPositioned { layout ->
        val pos = layout.positionInWindow()
        overlayPosX = pos.x
        overlayPosY = pos.y
    }.pointerInput(Unit) {
        detectTapGestures(onPress = { offset ->
            toyX = offset.x - toySizePx / 2f
            toyY = offset.y - toySizePx / 2f
            val toyRectWindow = Rect(toyX + overlayPosX, toyY + overlayPosY, toyX + overlayPosX + toySizePx, toyY + overlayPosY + toySizePx)
            if (toyRectWindow.overlaps(catBounds)) {
                amusement = (amusement + 1).coerceAtMost(100)
                onAmusementChange(amusement)
            }
            tryAwaitRelease()
        })
    }.pointerInput(Unit) {
        detectDragGestures(onDragStart = { offset ->
            toyX = offset.x - toySizePx / 2f
            toyY = offset.y - toySizePx / 2f
        }, onDrag = { change, dragAmount ->
            change.consume()
            toyX += dragAmount.x
            toyY += dragAmount.y
            val toyRectWindow = Rect(toyX + overlayPosX, toyY + overlayPosY, toyX + overlayPosX + toySizePx, toyY + overlayPosY + toySizePx)
            if (toyRectWindow.overlaps(catBounds)) {
                amusement = (amusement + 1).coerceAtMost(500)
                onAmusementChange(amusement)
            }
        })
    }) {
        Image(painter = painterResource(R.drawable.plumeau), contentDescription = "Plumeau", modifier = Modifier.size(120.dp).offset { IntOffset(toyX.toInt(), toyY.toInt()) })

        Button(onClick = onClose, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) { Text("Terminer") }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatInteractionPreview() {
    CatwalkTheme {
        CatInteractionContent(cat = Cat(name = "Minou", color = "chat_banc_noir.png", happiness = 50, cleanliness = 50, affection = 1f, achievementId = null, obtenu = true, price = 0.0, level = 1))
    }
}

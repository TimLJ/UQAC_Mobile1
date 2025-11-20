package uqac.catwalk

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import uqac.catwalk.ui.theme.CatwalkTheme
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

    var proprete by remember { mutableStateOf(80) }
    var amusement by remember { mutableStateOf(60) }
    var affection by remember { mutableStateOf(2) }

    var isWashing by remember { mutableStateOf(false) }

    Scaffold(
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(innerPadding)
            .padding(top = 8.dp)
        ) {
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
                        painter = painterResource(R.drawable.chat),
                        contentDescription = "Chat",
                        modifier = Modifier
                            .size(300.dp)
                            .padding(16.dp),
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
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
                    onPropreteChange = { proprete = it },
                    onClose = { isWashing = false }
                )
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WashingOverlay(
    initialProprete: Int,
    onPropreteChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    var proprete by remember { mutableStateOf(initialProprete) }

    // Position de l'éponge
    var spongeX by remember { mutableStateOf(200f) }
    var spongeY by remember { mutableStateOf(200f) }

    // Rectangle du chat
    var catBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
    ) {

        // 🐱 Image du chat + récupération position
        Image(
            painter = painterResource(R.drawable.chat),
            contentDescription = "Chat",
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .onGloballyPositioned { layout ->
                    val pos = layout.positionInWindow()
                    catBounds = Rect(
                        pos.x,
                        pos.y,
                        pos.x + layout.size.width,
                        pos.y + layout.size.height
                    )
                }
        )

        // 🧽 Éponge draggable
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

                        // détection contact éponge/chat
                        val spongeRect = Rect(
                            spongeX,
                            spongeY,
                            spongeX + 120.dp.toPx(),
                            spongeY + 120.dp.toPx()
                        )

                        if (spongeRect.overlaps(catBounds)) {
                            proprete = (proprete + 1).coerceAtMost(100)
                            onPropreteChange(proprete)
                        }
                    }
                }
        )

        // ❌ Bouton fermer
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                tint = Color.White,
                contentDescription = "Fermer"
            )
        }

        // ✔️ Bouton Terminer — en bas au centre
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
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

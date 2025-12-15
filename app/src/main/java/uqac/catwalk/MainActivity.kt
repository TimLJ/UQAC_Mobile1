package uqac.catwalk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.sauvegarde.PlayerData
import uqac.catwalk.sauvegarde.updtMoney
import uqac.catwalk.ui.theme.CatwalkTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import uqac.catwalk.sauvegarde.DataStoreManager
import uqac.catwalk.sauvegarde.MsMoney
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.entities.Cat

data class Coords (
    val x: Dp,
    val y: Dp
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Créer une instance de votre DataStoreManager
        val dataStoreManager = DataStoreManager(applicationContext)

        // 2. Lancer une coroutine pour charger les données de manière asynchrone
        // On utilise lifecycleScope car onCreate n'est pas un composable
        lifecycleScope.launch {
            // 3. APPELER LA FONCTION ICI !
            dataStoreManager.loadPlayerData()

            // 4. Une fois les données chargées, on peut construire l'UI
            setContent {
                CatwalkTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MainContent(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Body of the main screen
@SuppressLint("Range")
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val Player by remember { mutableStateOf(PlayerData) }

    val database = AppDatabase.getDatabase(context = context)
    val catDao = database.CatDao()
    val cats by catDao.getDebloques().collectAsState(initial = emptyList())

    var imageWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }

    LaunchedEffect(imageWidth, containerWidth) {
        if (imageWidth > containerWidth) {
            val overflow = imageWidth - containerWidth
            scrollState.scrollTo(overflow / 2 - 60)
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(80.dp)
            )
        },

        content = { paddingValues ->
            Box(
                modifier = modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        containerWidth = coords.size.width
                    }
                    .horizontalScroll(scrollState)
            ) {
                Image(
                    painter = painterResource(R.drawable.background),
                    contentDescription = "Image de prairie avec une rivière, un moulin, et des oiseaux dans le ciel.",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            imageWidth = coords.size.width
                        }
                )

                Image(
                    painter = painterResource(R.drawable.floor),
                    contentDescription = "Image d'un plancher.",
                    modifier = Modifier
                        .padding(start = 100.dp, top = 80.dp)
                )

                Image(
                    painter = painterResource(R.drawable.house),
                    contentDescription = "Image d'une maison avec des oreilles de chat.",
                    modifier = Modifier
                        .padding(start = 100.dp, top = 80.dp)
                )
                cats.forEach { cat ->
                    SleepyCat(modifier, context, cat)
                }
            }
        },
        
        bottomBar = {
            AppBottomBar(
                context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(100.dp)
            )
        }
    )
}

@Composable
fun SleepyCat(modifier: Modifier = Modifier, context: Context, cat: Cat) {
    // All cats coordinates
    val catCoords = mapOf(
        1 to Coords(60.dp, 350.dp),
        2 to Coords(300.dp, 380.dp),
        3 to Coords(400.dp, 180.dp)
    )

    // All cats sleeping colors:
    val catColors = mapOf(
        1 to R.drawable.chat_blanc_noir_dodo,
        2 to R.drawable.chat_gris_dodo,
        3 to R.drawable.chat_roux_dodo
    )

    var affection by remember { mutableStateOf(cat.affection) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(start = catCoords[cat.id]?.x ?: 0.dp,
                top = catCoords[cat.id]?.y ?: 0.dp)
            .alpha(1f)
    ) {
        Text(
            text = cat.name,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f),
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .alpha(1f)
                .offset(y = 40.dp)
        )
        Button(
            onClick = {
                val intent =
                    Intent(context, CatInteractionActivity::class.java)
                intent.putExtra("catID", cat.id)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .size(120.dp)
        ) {
            Image(
                painter = painterResource(catColors[cat.id] ?: R.drawable.chat_blanc_noir_dodo) ,
                contentDescription = "Chat blanc et noir qui dort.",
            )
        }
        Row(
            modifier = Modifier
                .offset(y = -30.dp)
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
                        .size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    CatwalkTheme {
        MainContent()
    }
}
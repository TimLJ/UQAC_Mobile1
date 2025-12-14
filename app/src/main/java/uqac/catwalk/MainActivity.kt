package uqac.catwalk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import uqac.catwalk.ui.theme.CatwalkTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import uqac.catwalk.sauvegarde.DataStoreManager
import uqac.catwalk.sauvegarde.MsMoney
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource

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

// Top bar of the app containing a button for the achievements, a coin image and the amount of coins the player has
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(colorResource(R.color.light_yellow)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Bouton succès
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, AchievementsActivity::class.java)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Succès",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Barre d'expérience
        Button(
            onClick = {
                context.startActivity(
                    Intent(context, AchievementsActivity::class.java)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .weight(3f)
                .height(70.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(R.drawable.exp_bar),
                    contentDescription = "Barre d'expérience",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(R.drawable.lvl1),
                    contentDescription = "Niveau 1",
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopStart)
                )
            }
        }

        // Argent
        Box(
            modifier = Modifier
                .weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = MsMoney.toString(),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 30.dp)
                        .border(2.dp, colorResource(R.color.orange))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Image(
                    painter = painterResource(R.drawable.paw_coin),
                    contentDescription = "Pièce",
                    modifier = Modifier
                        .size(45.dp)
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}


// Bottom bar of the app containg buttons for the shop, the list of cats, and starting a walk
@Composable
fun AppBottomBar(
    context: Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Bottom navigation bar taking 1/8 of screen height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(colorResource(R.color.light_yellow))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Shop section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, ShopActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Boutique",
                        tint = colorResource(R.color.black)
                    )
                    Text("Boutique", color = colorResource(R.color.black))
                }
            }

            VerticalDivider(
                color = colorResource(R.color.black),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxHeight()
            )

            // Cat section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, CatListActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.cat_icon),
                        contentDescription = "Icone de chat noir",
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Text("Chats", color = colorResource(R.color.black))
                }
            }

            VerticalDivider(
                color = colorResource(R.color.black),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxHeight()
            )

            // Walk section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        val intent = Intent(context, WalkActivity::class.java)
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.walk_icon),
                        contentDescription = "Chat noir de profil qui marche.",
                        modifier = Modifier
                            .size(28.dp)
                            .offset(y = -4.dp)
                    )
                    Text(
                        "Marche",
                        color = colorResource(R.color.black),
                        modifier = Modifier
                            .offset(y = -2.dp)
                    )
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
                    .horizontalScroll(scrollState)
            ) {
                Image(
                    painter = painterResource(R.drawable.background),
                    contentDescription = "Image de prairie avec une rivière, un moulin, et des oiseaux dans le ciel.",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxHeight()
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

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    CatwalkTheme {
        MainContent()
    }
}
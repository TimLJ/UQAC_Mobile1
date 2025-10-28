package uqac.catwalk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.ui.theme.CatwalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

// Top bar of the app containing a button for the achievements, a coin image and the amount of coins the player has
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    coinAmount: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .background(color = Color.Cyan)
                .fillMaxWidth()
                .fillMaxHeight()
                .drawBehind {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, AchievementsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .width(250.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Go to Lv Activity"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = coinAmount,
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        modifier = Modifier
                            .size(height = 40.dp, width = 60.dp)
                            .offset(40.dp)
                            .border(BorderStroke(2.dp, Color.Black))
                            .background(color = Color.Yellow)
                            .offset(x = 5.dp, y = 5.dp)

                    )
                    Image(
                        painter = painterResource(R.drawable.money_icon),
                        contentDescription = "Image de pièce chat",
                        modifier = Modifier
                            .background(color = Color.White)
                            .size(50.dp)
                    )
                }
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
                .background(MaterialTheme.colorScheme.primary)
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
                        contentDescription = "Shop",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Shop", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
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
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Cat",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Cat", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            VerticalDivider(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
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
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Walk",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("Walk", color = MaterialTheme.colorScheme.onPrimary)
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

    Scaffold(
        topBar = {
            MainTopBar(
                56.toString(),
                context,
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
            ) {
                Text(
                    text = "Fond de la maison des chats",
                    modifier = Modifier.align(Alignment.Center)
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
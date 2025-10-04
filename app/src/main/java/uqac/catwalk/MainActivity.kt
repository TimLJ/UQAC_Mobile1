package uqac.catwalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = {
                val intent = Intent(context, LvActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Go to Lv Activity"
            )
        }
        
        Text(
            text = "Welcome to Catwalk!",
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom navigation bar taking 1/8 of screen height
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(1f / 8f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Shop button
            Button(
                onClick = {
                    val intent = Intent(context, ShopActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Shop"
                    )
                    Text("Shop")
                }
            }

            // Cat button
            Button(
                onClick = {
                    val intent = Intent(context, CatActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Cat"
                    )
                    Text("Cat")
                }
            }

            // Walk button
            Button(
                onClick = {
                    val intent = Intent(context, WalkActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Walk"
                    )
                    Text("Walk")
                }
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
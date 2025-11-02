package uqac.catwalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uqac.catwalk.ui.theme.CatwalkTheme

class CatInteractionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CatInteractionContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CatInteractionContent(modifier: Modifier = Modifier) {
    var proprete by remember { mutableStateOf(80) }
    var amusement by remember { mutableStateOf(60) }
    var affection by remember { mutableStateOf(2) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ❤️ Barre d’affection (coeurs)
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

        // 🐱 Image du chat
        Image(
            painter = painterResource(R.drawable.chat),
            contentDescription = "Chat",
            modifier = Modifier
                .size(260.dp)
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )

        // ⚡ Barres côte à côte
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Propreté", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = proprete / 100f,
                    modifier = Modifier
                        .width(130.dp)
                        .height(10.dp)
                        .padding(top = 4.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFC8E6C9)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Amusement", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = amusement / 100f,
                    modifier = Modifier
                        .width(130.dp)
                        .height(10.dp)
                        .padding(top = 4.dp),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFFFE0B2)
                )
            }
        }

        // ⚙️ Footer : boutons d’action
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Button(onClick = { amusement = (amusement + 10).coerceAtMost(100) }) {
                Text("Jouer")
            }
            Button(onClick = { proprete = (proprete + 10).coerceAtMost(100) }) {
                Text("Laver")
            }
            Button(onClick = { affection = (affection + 1).coerceAtMost(3) }) {
                Text("Caresser")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatInteractionPreview() {
    CatwalkTheme {
        CatInteractionContent()
    }
}

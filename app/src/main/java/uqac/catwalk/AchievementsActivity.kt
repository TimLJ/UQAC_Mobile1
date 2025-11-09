package uqac.catwalk

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.sauvegarde.entities.Achievement
import uqac.catwalk.ui.theme.CatwalkTheme

class AchievementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LvContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Top bar for every screen that is not the main screen
@Composable
fun AppTopBar(coinAmount: String,
              context: Context,
              modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
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
            // Bouton Home en haut à gauche
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(start = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Retour à l'accueil"
                )
            }

            Button(
                onClick = {
                    val intent = Intent(context, AchievementsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth(1f / 1.5f)
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

@Composable
fun LvContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Liste des niveaux (15 pour l'exemple)
    val database = AppDatabase.getDatabase(context = context)
    val AchievementDao = database.AchievementDao()
    val achievements by AchievementDao.getAllAchievements().collectAsState(initial = emptyList())
//    val levels = listOf(
//        "Niveau 1 - Débutant", "Niveau 2 - Novice", "Niveau 3 - Apprenti",
//        "Niveau 4 - Intermédiaire", "Niveau 5 - Avancé", "Niveau 6 - Expert",
//        "Niveau 7 - Maître", "Niveau 8 - Grand Maître", "Niveau 9 - Légende",
//        "Niveau 10 - Mythique", "Niveau 11 - Divin", "Niveau 12 - Transcendant",
//        "Niveau 13 - Éternel", "Niveau 14 - Infini", "Niveau 15 - Ultime"
//    )

    Scaffold (
        topBar = {
            AppTopBar(
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
                    .padding(
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                        top = 80.dp,
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = 100.dp
                    )
                    .fillMaxSize()
                    .background(color = Color.LightGray)
            ) {
                // Liste scrollable des niveaux
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 16.dp,
                                bottom = 16.dp
                            ),
                        textAlign = TextAlign.Center
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(achievements) { achievement ->
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .height(120.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = achievement.name,
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = achievement.description,
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = when {
                                            achievement.obtenu -> "✅ Obtenu"
                                            achievement.débloqué -> "🔓 Disponible"
                                            else -> "🔒 Verrouillé"
                                        },
                                        textAlign = TextAlign.Center,
                                        color = when {
                                            achievement.obtenu -> Color.Green
                                            achievement.débloqué -> Color.Blue
                                            else -> Color.Gray
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
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

@Preview(showBackground = true)
@Composable
fun LvContentPreview() {
    CatwalkTheme {
        LvContent()
    }
}
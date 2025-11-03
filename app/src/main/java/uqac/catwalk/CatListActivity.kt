package uqac.catwalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.sauvegarde.AppDatabase
import uqac.catwalk.ui.theme.CatwalkTheme

class CatListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CatContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Liste des noms de chats (36 noms pour remplir la grille 3x12)
    val database = AppDatabase.getDatabase(context = context)
    val catDao = database.CatDao()
    val cats by catDao.getDébloqués().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                coinAmount = 56.toString(),
                context = context,
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
            ) {
                // Grillage de chats
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Nos Chats 🐱",
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

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cats) { cat ->
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .height(80.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(context, CatInteractionActivity::class.java)
                                        intent.putExtra("catName", catName)
                                        context.startActivity(intent)
                                    },
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
                                        text = cat.name,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(100.dp)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CatContentPreview() {
    CatwalkTheme {
        CatContent()
    }
}

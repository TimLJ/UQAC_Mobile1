package uqac.catwalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.colorResource
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

    val database = AppDatabase.getDatabase(context = context)
    val catDao = database.CatDao()
    val cats by catDao.getDebloques().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            AppTopBar(
                context = context,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(80.dp),
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
                    .background(color = colorResource(R.color.yellow_white))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Mes Chats",
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(bottom = 10.dp)
                                        .height(110.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent =
                                                Intent(context, CatInteractionActivity::class.java)
                                            intent.putExtra("catID", cat.id)
                                            context.startActivity(intent)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = catPainter(cat.color),
                                        contentDescription = cat.name,
                                        modifier = Modifier
                                            .height(80.dp)
                                            .fillMaxWidth(),
                                    )
                                    Text(
                                        text = cat.name,
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxWidth()
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

package uqac.catwalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import uqac.catwalk.ui.theme.CatwalkTheme
import uqac.catwalk.sauvegarde.updtMoney
import uqac.catwalk.sauvegarde.addDistance


class EndWalkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WalkContent(
                        modifier = Modifier.padding(innerPadding),
                        intent = intent,
                    )
                }
            }
        }
    }
}

@Composable
fun WalkContent(modifier: Modifier = Modifier, intent:  Intent) {
    val context = LocalContext.current
    val distance = intent.getDoubleExtra("progress", 0.0)
    val coroutineScope = rememberCoroutineScope()
    val affection = (distance / 2500).toInt() * 0.3
    val pièces = (distance / 50).toInt()




    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Résumé de la ballade",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Pas
        Text(
            text = "Distance: ${String.format("%.0f", distance)} m ",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Pièces
        Text(
            text = "Pièces: $pièces",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        // Affection du chat
        //Text(
        //    text = "Affection gagnée : ${String.format("%.0f",affection)} coeur",
        //    fontSize = 24.sp,
        //    style = MaterialTheme.typography.headlineSmall,
        //    color = MaterialTheme.colorScheme.tertiary
        //)
        //A ajouter quand on poura définir un chat comme favori pour la ballade

        // Bouton de retour
        Button(
            onClick = {
                coroutineScope.launch { 
                    addDistance(distance.toInt(), context)
                    updtMoney(pièces, context)
                    //ajouter l'affection du chat
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
                .height(60.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Retour"
            )
            Text(text = "Retour", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalkContentPreview() {
    CatwalkTheme {
        WalkContent(intent = Intent())
    }
}
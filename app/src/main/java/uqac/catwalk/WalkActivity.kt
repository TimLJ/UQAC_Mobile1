package uqac.catwalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uqac.catwalk.ui.theme.CatwalkTheme
import kotlin.random.Random

class WalkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatwalkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProgressContent(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(colorResource(R.color.yellow_white))
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressContent(
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progress by remember { mutableIntStateOf(Random.nextInt(4000, 10001)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp,16.dp,16.dp,0.dp)
            .background(color = colorResource(R.color.yellow_white)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Text
        Text(
            text = "Ballade en cours...",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Temporary image (using launcher icon as placeholder)
        Image(
            painter = painterResource(R.drawable.walk_icon),
            contentDescription = "Chat noir de profil qui marche.",
            modifier = Modifier
                .size(150.dp)
        )

        // Progress bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Marché ${(progress)} pas",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LinearProgressIndicator(
            progress = { progress/10000.toFloat() },
            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
        Text(
            text="Prochain objectif à 10 000 pas",
        )

        // Stop button
        Button(
            onClick = {
                val intent = Intent(context, EndWalkActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.red)
            ),
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
                .height(60.dp),
        ) {
            Text(text = "STOP", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressContentPreview() {
    CatwalkTheme {
        ProgressContent()
    }
}
package uqac.catwalk.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun CongratsDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
        alpha.animateTo(1f, tween(150))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Super 😻")
            }
        },
        title = { Text("Bravo 🎉") },
        text = { Text("Ton chat est propre et heureux !") },
        modifier = androidx.compose.ui.Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
    )
}

package uqac.catwalk.walk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity

@Composable
fun StepProgressBar(
    progressMeters: Float,
    steps: List<Int> = listOf(1000, 5000, 10000),
    modifier: Modifier = Modifier.fillMaxWidth(),
    height: Dp = 50.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    stepMarkerColor: Color = MaterialTheme.colorScheme.secondary,
    knobBorderColor: Color = MaterialTheme.colorScheme.secondary,
    labelColor: Color = Color.DarkGray,
    contentPadding: Dp = 8.dp
) {
    val max = steps.last().coerceAtLeast(1).toFloat()
    val progressFrac = (progressMeters.coerceIn(0f, max)) / max

    val density = LocalDensity.current

    val knobRadiusDp = 8.dp
    val knobInnerRadiusDp = 5.dp
    val stepLineWidthDp = 2.dp
    val stepLineHeightFactor = 0.6f
    val labelOffsetDp = 14.dp

    Canvas(
        modifier = modifier
            .height(height + labelOffsetDp)
            .padding(horizontal = contentPadding)
    ) {
        val w = size.width
        val barHeight = height.toPx()

        val knobRadius = with(density) { knobRadiusDp.toPx() }
        val knobInnerRadius = with(density) { knobInnerRadiusDp.toPx() }
        val stepLineWidth = with(density) { stepLineWidthDp.toPx() }
        val labelOffset = with(density) { labelOffsetDp.toPx() }

        // Zone utilisable (évite tout débordement)
        val startX = knobRadius
        val endX = w - knobRadius
        val usableWidth = endX - startX

        /* -------- Background bar -------- */
        drawRoundRect(
            color = backgroundColor,
            size = Size(w, barHeight),
            cornerRadius = CornerRadius(barHeight / 2)
        )

        /* -------- Progress bar -------- */
        val progressWidth = usableWidth * progressFrac

        if (progressWidth > 0f) {
            drawRoundRect(
                color = progressColor,
                topLeft = Offset(startX, 0f),
                size = Size(progressWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )
        }

        /* -------- Step markers (barres verticales) -------- */
        val stepLineHeight = barHeight * stepLineHeightFactor
        val stepTop = (barHeight - stepLineHeight) / 2
        val stepBottom = stepTop + stepLineHeight

        val textPaint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            color = labelColor.toArgb()
            isAntiAlias = true
        }

        steps.forEach { step ->
            val frac = step / max
            val x = startX + frac * usableWidth

            // vertical marker
            drawLine(
                color = stepMarkerColor,
                start = Offset(x, stepTop),
                end = Offset(x, stepBottom),
                strokeWidth = stepLineWidth
            )

            // label
            drawContext.canvas.nativeCanvas.drawText(
                "${step / 1000} km",
                x,
                barHeight + labelOffset,
                textPaint
            )
        }

        /* -------- Current progress knob -------- */
        val knobX = (startX + usableWidth * progressFrac)
            .coerceIn(startX, endX)

        drawCircle(
            color = knobBorderColor,
            radius = knobRadius,
            center = Offset(knobX, barHeight / 2)
        )

        drawCircle(
            color = Color.White,
            radius = knobInnerRadius,
            center = Offset(knobX, barHeight / 2)
        )
    }
}

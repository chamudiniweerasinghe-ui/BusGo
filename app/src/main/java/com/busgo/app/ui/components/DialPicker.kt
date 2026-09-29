package com.busgo.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.busgo.app.data.mock.MockData
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Semi-circular number dial from the reference ("average cycle length").
 * Drag left/right on it, or tap a neighbouring number, to change the value.
 */
@Composable
fun DialPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    unit: String = ""
) {
    val latestValue by rememberUpdatedState(value)
    val latestOnChange by rememberUpdatedState(onValueChange)
    val stepPx = with(LocalDensity.current) { 40.dp.toPx() }
    var drag by remember { mutableFloatStateOf(0f) }
    val degPerValue = 21f // 3 ticks of 7° – keeps the major ticks lined up after every step

    Box(
        modifier
            .fillMaxWidth()
            .height(360.dp)
            .pointerInput(range) {
                detectHorizontalDragGestures(
                    onDragEnd = { drag = 0f },
                    onDragCancel = { drag = 0f }
                ) { change, delta ->
                    change.consume()
                    drag += delta
                    val steps = (drag / stepPx).toInt()
                    if (steps != 0) {
                        val next = (latestValue - steps).coerceIn(range.first, range.last)
                        if (next != latestValue) latestOnChange(next)
                        drag -= steps * stepPx
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.width * 0.78f
            val center = Offset(size.width / 2f, size.height * 0.6f + r)
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.9f), Blush.copy(alpha = 0.35f), Lavender.copy(alpha = 0.3f)),
                    startY = center.y - r,
                    endY = size.height
                ),
                radius = r,
                center = center
            )
            drawCircle(Hairline, r, center, style = Stroke(1.dp.toPx()))

            val offsetDeg = (drag / stepPx) * degPerValue
            val inset = 10.dp.toPx()
            for (i in -30..30) {
                val deg = -90f + i * 7f + offsetDeg
                val rad = Math.toRadians(deg.toDouble())
                val major = i % 3 == 0
                val len = if (major) 18.dp.toPx() else 9.dp.toPx()
                val c = kotlin.math.cos(rad).toFloat()
                val s = kotlin.math.sin(rad).toFloat()
                drawLine(
                    color = Muted.copy(alpha = if (major) 0.35f else 0.18f),
                    start = Offset(center.x + (r - inset) * c, center.y + (r - inset) * s),
                    end = Offset(center.x + (r - inset - len) * c, center.y + (r - inset - len) * s),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            val top = Offset(center.x, center.y - r)
            drawLine(
                color = Rose,
                start = top + Offset(0f, 8.dp.toPx()),
                end = top + Offset(0f, 34.dp.toPx()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val neighbourStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp)
        if (value - 1 >= range.first) {
            Text(
                "${value - 1}", style = neighbourStyle, color = Muted.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-130).dp, y = 160.dp)
                    .rotate(-22f)
                    .clip(CircleShape)
                    .clickable { onValueChange(value - 1) }
                    .padding(8.dp)
            )
        }
        if (value + 1 <= range.last) {
            Text(
                "${value + 1}", style = neighbourStyle, color = Muted.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 130.dp, y = 160.dp)
                    .rotate(22f)
                    .clip(CircleShape)
                    .clickable { onValueChange(value + 1) }
                    .padding(8.dp)
            )
        }
        Column(
            Modifier.align(Alignment.TopCenter).padding(top = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.9f)) togetherWith fadeOut(tween(120))
                },
                label = "dial-value"
            ) { v ->
                Text("$v", style = MaterialTheme.typography.displayLarge, color = Ink)
            }
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}

package com.busgo.app.ui.components.map

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
 * Stylised map with the bus route, stops and a moving bus marker.
 *
 * This keeps the UI runnable with no API keys. To show a real map later,
 * swap this for an osmdroid MapView (OpenStreetMap) inside AndroidView and
 * feed it the positions coming from your Socket.IO stream.
 */
private val RoutePoints = listOf(
    Offset(0.14f, 0.12f), Offset(0.32f, 0.21f), Offset(0.26f, 0.34f),
    Offset(0.48f, 0.42f), Offset(0.68f, 0.50f), Offset(0.60f, 0.61f), Offset(0.82f, 0.70f)
)

@Composable
fun RouteMapCanvas(
    progress: Float,
    stopNames: List<String>,
    userStopIndex: Int,
    modifier: Modifier = Modifier
) {
    val busPainter = rememberVectorPainter(Icons.Filled.DirectionsBus)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = InkSoft)
    val pulse = rememberInfiniteTransition(label = "pulse").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Restart), label = "p"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(MapPaper)

        // water + parks
        drawOval(Color(0xFFD9E6EC), topLeft = Offset(w * 0.62f, -h * 0.05f), size = Size(w * 0.6f, h * 0.24f))
        drawRoundRect(Color(0xFFE3EBD6), Offset(w * 0.04f, h * 0.46f), Size(w * 0.32f, h * 0.15f), CornerRadius(40f))
        drawRoundRect(Color(0xFFE3EBD6), Offset(w * 0.72f, h * 0.33f), Size(w * 0.24f, h * 0.11f), CornerRadius(40f))

        // road grid
        val road = Color.White
        val roadWidth = 5.dp.toPx()
        for (i in 0..6) {
            val y = h * (0.06f + i * 0.14f)
            drawLine(road, Offset(0f, y), Offset(w, y + h * 0.05f), strokeWidth = roadWidth)
        }
        for (i in 0..4) {
            val x = w * (0.1f + i * 0.22f)
            drawLine(road, Offset(x, 0f), Offset(x - w * 0.08f, h), strokeWidth = roadWidth)
        }

        // route
        val pts = RoutePoints.map { Offset(it.x * w, it.y * h) }
        val full = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val routeWidth = 7.dp.toPx()
        drawPath(full, Color.White, style = Stroke(routeWidth + 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(full, Rose.copy(alpha = 0.35f), style = Stroke(routeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val (travelled, busPos) = pathUpTo(pts, progress)
        drawPath(travelled, Rose, style = Stroke(routeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // stops + labels
        pts.forEachIndexed { i, p ->
            drawCircle(Color.White, 6.dp.toPx(), p)
            drawCircle(Ink, 6.dp.toPx(), p, style = Stroke(2.dp.toPx()))
            stopNames.getOrNull(i)?.let { name ->
                drawText(
                    textMeasurer = textMeasurer,
                    text = name,
                    topLeft = p + Offset(10.dp.toPx(), -20.dp.toPx()),
                    style = labelStyle
                )
            }
        }

        // passenger's stop
        pts.getOrNull(userStopIndex)?.let { u ->
            drawCircle(Coral.copy(alpha = 0.2f), 20.dp.toPx(), u)
            drawCircle(Coral, 8.dp.toPx(), u)
            drawCircle(Color.White, 3.dp.toPx(), u)
        }

        // bus marker with pulse
        val p = pulse.value
        drawCircle(Rose.copy(alpha = 0.35f * (1f - p)), 20.dp.toPx() + 24.dp.toPx() * p, busPos)
        drawCircle(Ink, 17.dp.toPx(), busPos)
        val icon = 18.dp.toPx()
        translate(busPos.x - icon / 2, busPos.y - icon / 2) {
            with(busPainter) {
                draw(Size(icon, icon), colorFilter = ColorFilter.tint(Color.White))
            }
        }
    }
}

/** Returns the part of the polyline travelled so far and the current point on it. */
private fun pathUpTo(pts: List<Offset>, t: Float): Pair<Path, Offset> {
    val lengths = pts.zipWithNext { a, b -> (b - a).getDistance() }
    var remaining = lengths.sum() * t.coerceIn(0f, 1f)
    val path = Path().apply { moveTo(pts[0].x, pts[0].y) }
    for (i in lengths.indices) {
        if (remaining <= lengths[i]) {
            val f = if (lengths[i] == 0f) 0f else remaining / lengths[i]
            val p = androidx.compose.ui.geometry.lerp(pts[i], pts[i + 1], f)
            path.lineTo(p.x, p.y)
            return path to p
        }
        remaining -= lengths[i]
        path.lineTo(pts[i + 1].x, pts[i + 1].y)
    }
    return path to pts.last()
}

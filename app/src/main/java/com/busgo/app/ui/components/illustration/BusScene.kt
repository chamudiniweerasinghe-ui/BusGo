package com.busgo.app.ui.components.illustration

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
import com.busgo.app.ui.components.map.*

/**
 * Animated illustration: a bus driving towards you along a road, with swaying trees,
 * moving road markings and (optionally) a night sky with twinkling stars and drifting clouds.
 *
 * [approach] 0f = bus is a dot on the horizon, 1f = bus is right in front of you.
 * Animate it from 0 to 1 to make the bus "arrive" (used on the splash screen).
 */
@Composable
fun BusScene(
    modifier: Modifier = Modifier,
    approach: Float = 1f,
    showSky: Boolean = true,
    headlights: Boolean = true
) {
    val t = rememberInfiniteTransition(label = "scene")
    val road by t.animateFloat(0f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "road")
    val sway by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sway")
    val bob by t.animateFloat(0f, 1f, infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse), label = "bob")
    val drift by t.animateFloat(0f, 1f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "drift")
    val twinkle by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "twinkle")
    val stars = remember {
        val rnd = kotlin.random.Random(7)
        List(30) { Triple(rnd.nextFloat(), rnd.nextFloat() * 0.36f, rnd.nextFloat()) }
    }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val horizon = h * 0.42f
        val cx = w / 2f

        // ---------- sky ----------
        if (showSky) {
            stars.forEach { (sx, sy, phase) ->
                val a = (0.25f + 0.75f * kotlin.math.abs(kotlin.math.sin((twinkle + phase) * 3.1416f)))
                drawCircle(Color.White.copy(alpha = a * 0.8f), (1f + phase * 1.5f).dp.toPx(), Offset(sx * w, sy * h))
            }
            val moon = Offset(w * 0.84f, h * 0.31f)
            drawCircle(
                Brush.radialGradient(listOf(Color(0x55FDF2D6), Color.Transparent), center = moon, radius = 60.dp.toPx()),
                60.dp.toPx(), moon
            )
            drawCircle(Color(0xFFFDF2D6), 16.dp.toPx(), moon)
            listOf(0.1f to 0.18f, 0.55f to 0.27f, 0.85f to 0.33f).forEachIndexed { i, (bx, by) ->
                val x = (((bx + drift * (0.6f + i * 0.2f)) % 1.3f) - 0.15f) * w
                val cw = w * (0.28f - i * 0.04f)
                drawOval(Color.White.copy(alpha = 0.08f), Offset(x, h * by), Size(cw, cw * 0.28f))
                drawOval(Color.White.copy(alpha = 0.06f), Offset(x + cw * 0.2f, h * by - cw * 0.1f), Size(cw * 0.6f, cw * 0.3f))
            }
        }

        // ---------- hills + ground ----------
        drawOval(Color(0xFF3E6E57), Offset(-w * 0.3f, horizon - h * 0.09f), Size(w * 0.9f, h * 0.3f))
        drawOval(Color(0xFF4B805F), Offset(w * 0.35f, horizon - h * 0.11f), Size(w * 1.0f, h * 0.34f))
        drawRect(
            Brush.verticalGradient(listOf(Color(0xFF6FAF6E), Color(0xFF8FCB7E)), startY = horizon, endY = h),
            Offset(0f, horizon), Size(w, h - horizon)
        )

        // ---------- road ----------
        val topHalf = w * 0.03f
        val bottomHalf = w * 0.5f
        fun halfAt(y: Float) = topHalf + (bottomHalf - topHalf) * ((y - horizon) / (h - horizon)).coerceIn(0f, 1f)
        val roadPath = Path().apply {
            moveTo(cx - topHalf, horizon)
            lineTo(cx + topHalf, horizon)
            lineTo(cx + bottomHalf, h)
            lineTo(cx - bottomHalf, h)
            close()
        }
        drawPath(roadPath, Color(0xFF55546E))
        drawLine(Color.White.copy(alpha = 0.5f), Offset(cx - topHalf, horizon), Offset(cx - bottomHalf, h), strokeWidth = 2.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.5f), Offset(cx + topHalf, horizon), Offset(cx + bottomHalf, h), strokeWidth = 2.dp.toPx())
        for (i in 0 until 7) {
            val p = (i + road) / 7f
            val f = p * p
            val y = horizon + (h - horizon) * f
            val len = (h - horizon) * 0.05f * (0.3f + f * 1.6f)
            drawLine(
                Color.White.copy(alpha = 0.3f + 0.6f * f),
                Offset(cx, y), Offset(cx, (y + len).coerceAtMost(h)),
                strokeWidth = (1f + 5f * f).dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // ---------- trees (far to near) ----------
        listOf(0.12f, 0.3f, 0.55f, 0.85f).forEach { p ->
            val y = horizon + (h - horizon) * p
            val s = (6f + 44f * p).dp.toPx()
            for (side in listOf(-1f, 1f)) {
                val x = cx + side * (halfAt(y) + s * 0.9f + w * 0.03f * p)
                rotate(degrees = sway * (2f + p * 2f) * side, pivot = Offset(x, y)) {
                    drawTree(x, y, s)
                }
            }
        }

        // ---------- the bus ----------
        val e = approach.coerceIn(0f, 1f)
        val busBottom = horizon + (h * 0.95f - horizon) * e
        val busWidth = halfAt(busBottom) * 2f * 0.55f
        val lift = bob * 2.dp.toPx() * e

        if (headlights && e > 0.05f) {
            listOf(-1f, 1f).forEach { side ->
                val lx = cx + side * busWidth * 0.34f
                val ly = busBottom - busWidth * 1.05f * 0.33f
                val beam = Path().apply {
                    moveTo(lx - busWidth * 0.05f, ly)
                    lineTo(lx + busWidth * 0.05f, ly)
                    lineTo(lx + side * busWidth * 0.2f + busWidth * 0.35f, h)
                    lineTo(lx + side * busWidth * 0.2f - busWidth * 0.35f, h)
                    close()
                }
                drawPath(beam, Color(0xFFFFF3B0).copy(alpha = 0.12f * e))
            }
        }
        drawOval(
            Color.Black.copy(alpha = 0.18f),
            Offset(cx - busWidth * 0.55f, busBottom - busWidth * 0.05f),
            Size(busWidth * 1.1f, busWidth * 0.12f)
        )
        drawBusFront(cx, busBottom - lift, busWidth)
    }
}

private fun DrawScope.drawTree(x: Float, base: Float, s: Float) {
    drawRect(Color(0xFF7A5236), Offset(x - s * 0.09f, base - s * 0.55f), Size(s * 0.18f, s * 0.55f))
    drawCircle(Color(0xFF3F8F5A), s * 0.42f, Offset(x - s * 0.28f, base - s * 0.62f))
    drawCircle(Color(0xFF3F8F5A), s * 0.42f, Offset(x + s * 0.28f, base - s * 0.62f))
    drawCircle(Color(0xFF4FA869), s * 0.48f, Offset(x, base - s * 0.9f))
    drawCircle(Color(0xFF5DB876), s * 0.22f, Offset(x - s * 0.12f, base - s * 1.05f))
}

/** Front view of a bus. (cx, bottom) = bottom-centre of the wheels, bw = body width. */
private fun DrawScope.drawBusFront(cx: Float, bottom: Float, bw: Float) {
    if (bw < 2f) return
    val bh = bw * 1.05f
    val left = cx - bw / 2
    val top = bottom - bh
    val r = CornerRadius(bw * 0.12f)

    // wheels
    val ww = bw * 0.17f
    val wh = bw * 0.16f
    drawRoundRect(Tyre, Offset(left + bw * 0.08f, bottom - wh), Size(ww, wh), CornerRadius(ww * 0.3f))
    drawRoundRect(Tyre, Offset(left + bw * 0.75f, bottom - wh), Size(ww, wh), CornerRadius(ww * 0.3f))

    // mirrors
    val stroke = bw * 0.025f
    drawLine(Tyre, Offset(left, top + bh * 0.2f), Offset(left - bw * 0.08f, top + bh * 0.24f), strokeWidth = stroke)
    drawLine(Tyre, Offset(left + bw, top + bh * 0.2f), Offset(left + bw + bw * 0.08f, top + bh * 0.24f), strokeWidth = stroke)
    drawRoundRect(Tyre, Offset(left - bw * 0.12f, top + bh * 0.23f), Size(bw * 0.06f, bh * 0.12f), CornerRadius(bw * 0.02f))
    drawRoundRect(Tyre, Offset(left + bw + bw * 0.06f, top + bh * 0.23f), Size(bw * 0.06f, bh * 0.12f), CornerRadius(bw * 0.02f))

    // body
    val bodyBottom = bottom - wh * 0.55f
    drawRoundRect(BusYellow, Offset(left, top), Size(bw, bodyBottom - top), r)
    drawRoundRect(BusYellowDark, Offset(left, top), Size(bw, bh * 0.15f), r)

    // destination sign
    drawRoundRect(Navy, Offset(left + bw * 0.22f, top + bh * 0.035f), Size(bw * 0.56f, bh * 0.08f), CornerRadius(bw * 0.02f))
    drawRoundRect(Orange, Offset(left + bw * 0.3f, top + bh * 0.065f), Size(bw * 0.4f, bh * 0.022f), CornerRadius(bw * 0.01f))

    // windscreen + glare + divider
    val gTop = top + bh * 0.17f
    val gH = bh * 0.36f
    drawRoundRect(Glass, Offset(left + bw * 0.07f, gTop), Size(bw * 0.86f, gH), CornerRadius(bw * 0.06f))
    val glare = Path().apply {
        moveTo(left + bw * 0.14f, gTop + gH)
        lineTo(left + bw * 0.34f, gTop)
        lineTo(left + bw * 0.44f, gTop)
        lineTo(left + bw * 0.24f, gTop + gH)
        close()
    }
    drawPath(glare, Color.White.copy(alpha = 0.45f))
    // driver
    drawCircle(Navy.copy(alpha = 0.55f), bw * 0.07f, Offset(left + bw * 0.7f, gTop + gH * 0.45f))
    drawRoundRect(Navy.copy(alpha = 0.55f), Offset(left + bw * 0.6f, gTop + gH * 0.62f), Size(bw * 0.2f, gH * 0.38f), CornerRadius(bw * 0.05f))
    drawRect(BusYellowDark, Offset(cx - bw * 0.012f, gTop), Size(bw * 0.024f, gH))

    // headlights with glow
    listOf(0.17f, 0.83f).forEach { fx ->
        val c = Offset(left + bw * fx, top + bh * 0.67f)
        drawCircle(Brush.radialGradient(listOf(Color(0x88FFF3B0), Color.Transparent), center = c, radius = bw * 0.16f), bw * 0.16f, c)
        drawCircle(Color.White, bw * 0.07f, c)
        drawCircle(Color(0xFFFFF3B0), bw * 0.045f, c)
    }

    // grille
    drawRoundRect(Tyre, Offset(left + bw * 0.3f, top + bh * 0.6f), Size(bw * 0.4f, bh * 0.14f), CornerRadius(bw * 0.03f))
    for (i in 1..3) {
        val gy = top + bh * 0.6f + bh * 0.035f * i
        drawLine(Color.White.copy(alpha = 0.2f), Offset(left + bw * 0.34f, gy), Offset(left + bw * 0.66f, gy), strokeWidth = bw * 0.01f)
    }

    // bumper + plate
    drawRoundRect(Navy, Offset(left - bw * 0.02f, top + bh * 0.78f), Size(bw * 1.04f, bh * 0.09f), CornerRadius(bw * 0.04f))
    drawRoundRect(Color.White, Offset(cx - bw * 0.12f, top + bh * 0.795f), Size(bw * 0.24f, bh * 0.06f), CornerRadius(bw * 0.012f))
}

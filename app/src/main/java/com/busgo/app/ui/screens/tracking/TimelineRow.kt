package com.busgo.app.ui.screens.tracking

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

enum class StopState { PASSED, CURRENT, YOUR_STOP, UPCOMING }

/** One row of the stop timeline (reference "Status" tab). */
@Composable
fun TimelineRow(time: String, title: String, subtitle: String, state: StopState, isLast: Boolean) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(36.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            TimelineIcon(state)
            if (!isLast) {
                val lineColor = if (state == StopState.PASSED) Orange else Hairline
                Canvas(Modifier.weight(1f).width(2.dp)) {
                    drawLine(
                        lineColor, Offset(size.width / 2, 4f), Offset(size.width / 2, size.height - 4f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        HSpace(10.dp)
        Text(
            time,
            style = MaterialTheme.typography.labelMedium,
            color = if (state == StopState.PASSED) Muted else Ink,
            modifier = Modifier.width(46.dp).padding(top = 8.dp)
        )
        Column(Modifier.weight(1f).padding(top = 6.dp, bottom = 20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = when (state) {
                    StopState.CURRENT, StopState.YOUR_STOP -> Ink
                    StopState.PASSED -> Muted
                    StopState.UPCOMING -> InkSoft
                }
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (state == StopState.CURRENT) OrangeDeep else Muted)
        }
    }
}

@Composable
private fun TimelineIcon(state: StopState) {
    when (state) {
        StopState.CURRENT -> {
            val pulse by rememberInfiniteTransition(label = "tl").animateFloat(
                1f, 1.6f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "pulse"
            )
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                            alpha = 1.6f - pulse
                        }
                        .background(Orange.copy(alpha = 0.35f), CircleShape)
                )
                Box(Modifier.size(32.dp).background(Orange, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        StopState.YOUR_STOP -> IconDot(Icons.Outlined.Place, OrangeSoft, Orange)
        StopState.PASSED -> IconDot(Icons.Outlined.Check, Orange, Color.White)
        StopState.UPCOMING -> IconDot(Icons.Outlined.Place, CreamDeep, Muted)
    }
}

@Composable
private fun IconDot(icon: ImageVector, bg: Color, tint: Color) {
    Box(Modifier.size(32.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
    }
}

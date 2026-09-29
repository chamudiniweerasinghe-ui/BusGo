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
import com.busgo.app.ui.components.map.*

/** Dashed line between two points with a little bus driving along it. */
@Composable
fun AnimatedRouteLine(
    modifier: Modifier = Modifier,
    lineColor: Color = Muted.copy(alpha = 0.5f),
    endpointColor: Color = Ink,
    busTint: Color = Orange,
    iconBackground: Color = Color.White
) {
    val p by rememberInfiniteTransition(label = "route").animateFloat(
        0f, 1f, infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "p"
    )
    BoxWithConstraints(modifier.height(22.dp)) {
        val icon = 20.dp
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2
            val r = 3.dp.toPx()
            drawCircle(endpointColor, r, Offset(r, y))
            drawLine(
                lineColor, Offset(r * 3, y), Offset(size.width - r * 3, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            drawCircle(endpointColor, r, Offset(size.width - r, y), style = Stroke(1.5.dp.toPx()))
        }
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth - icon) * p)
                .size(icon)
                .background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = busTint, modifier = Modifier.size(16.dp))
        }
    }
}

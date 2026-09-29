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

/**
 * The 2x2 orange squares from the reference header, used as the BusGo mark.
 * With [animated] = true the squares glow one after another (loading / live feel).
 */
@Composable
fun GridLogo(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = Orange, animated: Boolean = false) {
    val gap = size * 0.14f
    val cell = (size - gap) / 2
    val phase = if (animated) {
        rememberInfiniteTransition(label = "grid").animateFloat(
            0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "phase"
        ).value
    } else 0f
    Column(modifier.size(size), verticalArrangement = Arrangement.spacedBy(gap)) {
        repeat(2) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                repeat(2) { c ->
                    // clockwise order: 0 1 / 3 2
                    val k = if (r == 0) c else 3 - c
                    val a = if (animated) {
                        val x = ((phase - k / 4f) % 1f + 1f) % 1f
                        0.35f + 0.65f * (1f - x)
                    } else 1f
                    Box(
                        Modifier
                            .size(cell)
                            .graphicsLayer { alpha = a }
                            .clip(RoundedCornerShape(cell * 0.28f))
                            .background(color)
                    )
                }
            }
        }
    }
}

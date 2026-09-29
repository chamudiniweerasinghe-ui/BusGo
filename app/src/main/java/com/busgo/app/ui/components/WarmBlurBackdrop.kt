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
 * The blurred, glowing orange/red backdrop from the reference welcome screen.
 * Built from layered radial gradients (no image asset needed) and drifts slowly.
 */
@Composable
fun WarmBlurBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(
                0f to Color(0xFFF4D6D6),
                0.35f to Color(0xFFF0BFB4),
                0.7f to Color(0xFFC0583D),
                1f to Color(0xFF3B1A13)
            )
        )
        glowBlob(Offset(w * (0.25f + 0.08f * drift), h * 0.40f), w * 0.75f, Color(0xFFE8663C), 0.85f)
        glowBlob(Offset(w * (0.85f - 0.06f * drift), h * 0.33f), w * 0.55f, Color(0xFFF2934F), 0.80f)
        glowBlob(Offset(w * 0.55f, h * (0.55f + 0.04f * drift)), w * 0.60f, Color(0xFFC4412E), 0.75f)
        glowBlob(Offset(w * 0.12f, h * 0.62f), w * 0.45f, Color(0xFFEBA462), 0.55f)
        glowBlob(Offset(w * 0.70f, h * 0.18f), w * 0.50f, Color(0xFFF7D9C4), 0.60f)
        // darken the bottom so white text stays readable
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                0.55f to Color.Transparent,
                1f to Color(0xE62A120D)
            )
        )
    }
}

private fun DrawScope.glowBlob(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

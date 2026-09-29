package com.busgo.app.ui.screens.splash

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

import androidx.compose.animation.core.Animatable
import com.busgo.app.ui.components.illustration.BusScene

/**
 * Live start-up screen: a bus drives towards you out of the night while the logo fades in.
 * Swap the fixed timing for real start-up work (e.g. checking a saved login) later.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val approach = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { textAlpha.animateTo(1f, tween(700, delayMillis = 250)) }
        approach.animateTo(1f, tween(2600, easing = FastOutSlowInEasing))
        delay(600)
        onFinished()
    }

    Box(Modifier.fillMaxSize().background(BusGoGradients.Header)) {
        BusScene(Modifier.fillMaxSize(), approach = approach.value, showSky = true, headlights = true)
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
                .graphicsLayer {
                    alpha = textAlpha.value
                    translationY = (1f - textAlpha.value) * 30f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridLogo(size = 34.dp, animated = true)
            VSpace(16.dp)
            Text("BUS GO", style = MaterialTheme.typography.displayMedium, color = Color.White)
            VSpace(8.dp)
            LoadingText("Your bus is arriving")
        }
    }
}

/** Text followed by three dots that light up one after another. */
@Composable
private fun LoadingText(text: String) {
    val phase by rememberInfiniteTransition(label = "dots").animateFloat(
        0f, 3f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "phase"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        HSpace(6.dp)
        repeat(3) { i ->
            val active = phase.toInt() == i
            Box(
                Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp)
                    .background(if (active) Orange else Color.White.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

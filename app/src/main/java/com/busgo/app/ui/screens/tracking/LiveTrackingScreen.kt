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

@Composable
fun LiveTrackingScreen(bus: Bus, onBack: () -> Unit, onBook: () -> Unit) {
    var routeStops by remember { mutableStateOf<List<Stop>>(MockData.routeStops) }

    LaunchedEffect(bus.id) {
        com.busgo.app.data.BusNetworkManager.getBusStops(bus.id) { fetchedStops ->
            if (fetchedStops.isNotEmpty()) {
                routeStops = fetchedStops
            }
        }
    }

    // Simulated GPS movement (the "GPS simulator" idea from the proposal).
    val progress by rememberInfiniteTransition(label = "bus").animateFloat(
        initialValue = 0.12f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Reverse),
        label = "progress"
    )
    var secondsAgo by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsAgo = if (secondsAgo >= 4) 1 else secondsAgo + 1
        }
    }

    Column(Modifier.fillMaxSize().background(Cream)) {
        // navy header like the reference tracking screen
        Row(
            Modifier
                .fillMaxWidth()
                .background(BusGoGradients.Header)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack)
            HSpace(12.dp)
            BusAvatar(size = 40.dp, container = Orange)
            HSpace(10.dp)
            Column(Modifier.weight(1f)) {
                Text(bus.number, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(bus.operator, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            LiveBadge(text = "ON ROUTE", onDark = true)
        }

        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val mapHeight = maxHeight * 0.46f
            Box(Modifier.fillMaxWidth().height(mapHeight)) {
                RouteMapCanvas(
                    progress = progress,
                    stopNames = routeStops.map { it.name },
                    userStopIndex = 4,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)
                ) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text("${bus.etaMinutes} min", style = MaterialTheme.typography.titleLarge, color = OrangeDeep)
                        Text("to your stop", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }
            TrackingSheet(
                bus, secondsAgo, onBook,
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(maxHeight * 0.58f)
            )
        }
    }
}

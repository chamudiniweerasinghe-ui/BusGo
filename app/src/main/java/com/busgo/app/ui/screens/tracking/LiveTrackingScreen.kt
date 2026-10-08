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
import androidx.compose.ui.platform.LocalContext
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
import com.busgo.app.data.BusNetworkManager
import com.busgo.app.data.DriverNetworkManager
import com.busgo.app.data.mock.MockData
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun LiveTrackingScreen(bus: Bus, onBack: () -> Unit, onBook: () -> Unit) {
    val context = LocalContext.current
    var routeStops by remember { mutableStateOf<List<Stop>>(MockData.routeStops) }
    var busLocation by remember { mutableStateOf<BusLocation?>(null) }
    var secondsAgo by remember { mutableIntStateOf(1) }

    val numericBusId = remember(bus.id) {
        bus.id.replace("\\D".toRegex(), "").toIntOrNull() ?: 1
    }

    // Load route stops from backend
    LaunchedEffect(bus.id) {
        BusNetworkManager.getBusStops(bus.id) { fetchedStops ->
            if (fetchedStops.isNotEmpty()) {
                routeStops = fetchedStops
            }
        }
    }

    // Poll backend every 5 seconds for real driver GPS location
    LaunchedEffect(numericBusId) {
        while (isActive) {
            DriverNetworkManager.getBusLocation(context, busId = numericBusId) { success, location, _ ->
                if (success && location != null) {
                    busLocation = location
                    secondsAgo = 1
                }
            }
            delay(5000L)
        }
    }

    // Increment seconds ago timer
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000L)
            secondsAgo = if (secondsAgo >= 60) 1 else secondsAgo + 1
        }
    }

    // Calculate map progress from real GPS coordinates or fallback to bus progress
    val routeProgress: Float = remember(busLocation) {
        val loc = busLocation
        if (loc != null && loc.latitude != 0.0) {
            // Map Kurunegala (lat ~7.28) to Colombo Fort (lat ~6.93)
            val startLat = 7.28
            val endLat = 6.93
            val currentLat = loc.latitude.coerceIn(endLat, startLat)
            ((startLat - currentLat) / (startLat - endLat)).toFloat().coerceIn(0.05f, 0.95f)
        } else {
            0.25f
        }
    }

    val updatedBus = remember(bus, busLocation) {
        val loc = busLocation
        if (loc != null) {
            bus.copy(speedKmh = loc.speed.toInt())
        } else {
            bus
        }
    }

    Column(Modifier.fillMaxSize().background(Cream)) {
        // navy header
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
                Text(updatedBus.number, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(updatedBus.operator, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            LiveBadge(text = if (busLocation != null) "LIVE GPS" else "ON ROUTE", onDark = true)
        }

        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val mapHeight = maxHeight * 0.46f
            Box(Modifier.fillMaxWidth().height(mapHeight)) {
                RouteMapCanvas(
                    progress = routeProgress,
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
                        Text("${updatedBus.etaMinutes} min", style = MaterialTheme.typography.titleLarge, color = OrangeDeep)
                        Text("to your stop", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }
            TrackingSheet(
                updatedBus, secondsAgo, onBook,
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(maxHeight * 0.58f)
            )
        }
    }
}

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

/** White sheet under the map with Status / History / Bus info tabs (reference layout). */
@Composable
fun TrackingSheet(
    bus: Bus,
    secondsAgo: Int,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Status", "History", "Bus info")
    val stops = MockData.routeStops
    val currentIndex = 2
    val userIndex = 4
    val times = remember(bus.id) { stopTimes(bus, stops) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        shadowElevation = 18.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(44.dp)
                    .height(4.dp)
                    .background(Navy.copy(alpha = 0.8f), CircleShape)
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                tabs.forEachIndexed { i, label ->
                    val selected = tab == i
                    val lineWidth by animateDpAsState(if (selected) 60.dp else 0.dp, label = "tab-line")
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { tab = i }
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Orange else Muted)
                        VSpace(10.dp)
                        Box(Modifier.width(lineWidth).height(3.dp).background(Orange, RoundedCornerShape(2.dp)))
                    }
                }
            }
            HairlineDivider()

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                when (tab) {
                    0 -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveDot()
                            HSpace(6.dp)
                            Text(
                                "Updated ${secondsAgo}s ago",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted
                            )
                        }
                        VSpace(14.dp)
                        stops.forEachIndexed { i, stop ->
                            val state = when {
                                i < currentIndex -> StopState.PASSED
                                i == currentIndex -> StopState.CURRENT
                                i == userIndex -> StopState.YOUR_STOP
                                else -> StopState.UPCOMING
                            }
                            val subtitle = when (state) {
                                StopState.PASSED -> "Departed"
                                StopState.CURRENT -> "Bus is here now"
                                StopState.YOUR_STOP -> "Your pickup, about ${bus.etaMinutes} min"
                                StopState.UPCOMING -> "${stop.kmFromStart.toInt()} km from ${stops.first().name}"
                            }
                            TimelineRow(times[i], stop.name, subtitle, state, isLast = i == stops.lastIndex)
                        }
                    }
                    1 -> {
                        listOf(
                            Triple("05:10", "Colombo Fort to Kurunegala", "Completed, on time"),
                            Triple("Yesterday", "Kurunegala to Colombo Fort", "Completed, 8 min late"),
                            Triple("Yesterday", "Colombo Fort to Kurunegala", "Completed, on time")
                        ).forEach { (time, route, status) ->
                            Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(CreamDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = Navy, modifier = Modifier.size(20.dp))
                                }
                                HSpace(12.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(route, style = MaterialTheme.typography.titleMedium, color = Ink)
                                    Text(status, style = MaterialTheme.typography.bodySmall, color = Muted)
                                }
                                Text(time, style = MaterialTheme.typography.labelMedium, color = Muted)
                            }
                            HairlineDivider()
                        }
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatTile(Icons.Outlined.Speed, "${bus.speedKmh} km/h", "Speed", Modifier.weight(1f))
                            StatTile(Icons.Outlined.EventSeat, "${bus.seatsLeft}/${bus.totalSeats}", "Seats free", Modifier.weight(1f))
                            StatTile(Icons.Outlined.Place, bus.nextStop, "Next stop", Modifier.weight(1f))
                        }
                        VSpace(16.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BusAvatar(size = 44.dp)
                            HSpace(12.dp)
                            Column(Modifier.weight(1f)) {
                                Text(bus.operator, style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text("${bus.category.label} bus, Route ${bus.routeNo}", style = MaterialTheme.typography.bodySmall, color = Muted)
                            }
                        }
                        if (bus.amenities.isNotEmpty()) {
                            VSpace(14.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                bus.amenities.forEach { TagChip(it) }
                            }
                        }
                    }
                }
            }
            PillButton(
                "BOOK A SEAT", onBook,
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

/** Rough arrival time at each stop, from the departure time and distance. */
fun stopTimes(bus: Bus, stops: List<Stop>): List<String> {
    val start = runCatching { java.time.LocalTime.parse(bus.departure) }.getOrDefault(java.time.LocalTime.of(6, 0))
    return stops.map { start.plusMinutes((it.kmFromStart * 1.5).toLong()).toString() }
}

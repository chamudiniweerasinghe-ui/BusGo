package com.busgo.app.ui.screens.search

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
fun SearchResultsScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onTrack: (Bus) -> Unit,
    onBook: (Bus) -> Unit
) {
    var sort by rememberSaveable { mutableStateOf("Earliest") }
    var busesList by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(draft.from, draft.to, draft.category) {
        isLoading = true
        com.busgo.app.data.BusNetworkManager.searchBuses(
            origin = draft.from,
            destination = draft.to,
            category = draft.category?.label
        ) { results ->
            busesList = if (results.isNotEmpty()) results else MockData.buses.filter {
                draft.category == null || it.category == draft.category
            }
            isLoading = false
        }
    }

    val buses = remember(busesList, draft.category, sort) {
        val currentList = if (busesList.isNotEmpty()) busesList else MockData.buses
        val filtered = currentList.filter { draft.category == null || it.category == draft.category }
        when (sort) {
            "Cheapest" -> filtered.sortedBy { it.ratePerKm }
            "Most seats" -> filtered.sortedByDescending { it.seatsLeft }
            else -> filtered.sortedBy { it.departure }
        }
    }

    Column(Modifier.fillMaxSize().background(Cream)) {
        NavyHeader(title = "Available buses", onBack = onBack, actions = { LiveBadge(onDark = true) }) {
            VSpace(18.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("From", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text(draft.from, style = MaterialTheme.typography.headlineSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                AnimatedRouteLine(
                    Modifier.width(80.dp).padding(horizontal = 8.dp),
                    lineColor = Color.White.copy(alpha = 0.4f),
                    endpointColor = Color.White,
                    busTint = Color.White,
                    iconBackground = Orange
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("To", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text(draft.to, style = MaterialTheme.typography.headlineSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            VSpace(12.dp)
            Text(
                "${draft.date.pretty()}, ${buses.size} buses found",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Earliest", "Cheapest", "Most seats")) { option ->
                        SelectChip(option, sort == option, { sort = option })
                    }
                }
            }
            items(buses, key = { it.id }) { bus ->
                BusCard(bus, onTrack = { onTrack(bus) }, onBook = { onBook(bus) })
            }
            if (buses.isEmpty()) {
                item {
                    EmptyState(Icons.Outlined.DirectionsBus, "No buses match these filters", "Try another bus type or a different date.")
                }
            }
        }
    }
}

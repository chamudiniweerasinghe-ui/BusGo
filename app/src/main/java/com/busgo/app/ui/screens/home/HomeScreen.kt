package com.busgo.app.ui.screens.home

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    draft: BookingDraft,
    onDraftChange: (BookingDraft) -> Unit,
    onSearch: () -> Unit,
    onOpenAlerts: () -> Unit,
    onTrackBus: (Bus) -> Unit
) {
    var departure by rememberSaveable { mutableFloatStateOf(0.35f) }
    val today = remember { LocalDate.now() }
    val dates = remember { (0..6).map { today.plusDays(it.toLong()) } }
    val liveBuses = remember { MockData.buses.filter { it.speedKmh > 0 } }

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {
        NavyHeader(
            title = "Home",
            leading = { GridLogo() },
            actions = { AnimatedBell(onOpenAlerts) }
        ) {
            VSpace(18.dp)
            Text("Where are you heading today?", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            VSpace(6.dp)
            Text(
                "Search long-distance buses and watch them move live.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            VSpace(20.dp)
            RouteInputCard(
                from = draft.from,
                to = draft.to,
                onFromChange = { onDraftChange(draft.copy(from = it)) },
                onToChange = { onDraftChange(draft.copy(to = it)) },
                onSwap = { onDraftChange(draft.copy(from = draft.to, to = draft.from)) }
            )
        }

        Column(Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
            SectionTitle("When")
            VSpace(12.dp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dates) { d ->
                    SelectChip(d.chipLabel(today), draft.date == d, { onDraftChange(draft.copy(date = d)) })
                }
                item { SelectChip("Pick a date", false, { }, icon = Icons.Outlined.CalendarToday) }
            }

            VSpace(22.dp)
            SectionTitle("Bus type")
            VSpace(12.dp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectChip("Any", draft.category == null, { onDraftChange(draft.copy(category = null)) })
                BusCategory.entries.forEach { c ->
                    SelectChip(c.label, draft.category == c, { onDraftChange(draft.copy(category = c)) })
                }
            }

            VSpace(22.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Departure time", Modifier.weight(1f))
                Text(departureLabel(departure), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            VSpace(16.dp)
            GradientSlider(departure, { departure = it }, listOf("Early", "Midday", "Night"))

            VSpace(28.dp)
            PillButton("SEARCH BUSES", onSearch, leadingIcon = Icons.Outlined.Search)

            VSpace(32.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Live near you")
                HSpace(8.dp)
                LiveBadge()
                Spacer(Modifier.weight(1f))
                Text("${liveBuses.size} buses", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            VSpace(14.dp)
            liveBuses.forEach { bus ->
                LiveBusRow(bus) { onTrackBus(bus) }
                VSpace(12.dp)
            }
        }

        // peach section, like the reference "Guardian Profile" block
        Column(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Peach.copy(alpha = 0.55f))
                .padding(20.dp)
                .padding(bottom = 110.dp)
        ) {
            SectionTitle("Recent searches")
            VSpace(14.dp)
            listOf("Kurunegala" to "Colombo Fort", "Kurunegala" to "Kandy").forEach { (f, t) ->
                SoftCard(
                    onClick = { onDraftChange(draft.copy(from = f, to = t)) },
                    padding = 16.dp,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(OrangeSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.History, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
                        }
                        HSpace(12.dp)
                        Column(Modifier.weight(1f)) {
                            Text(f, style = MaterialTheme.typography.titleMedium, color = Ink)
                            Text("to $t", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Muted)
                    }
                }
            }
        }
    }
}

private fun departureLabel(v: Float): String = when {
    v < 0.33f -> "Before 10 AM"
    v < 0.66f -> "10 AM to 4 PM"
    else -> "After 4 PM"
}

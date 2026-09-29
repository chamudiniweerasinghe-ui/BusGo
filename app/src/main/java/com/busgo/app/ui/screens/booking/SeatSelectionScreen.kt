package com.busgo.app.ui.screens.booking

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

/** Step 3 — the 2 + 2 seat map. */
@Composable
fun SeatSelectionScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onContinue: (List<Int>) -> Unit
) {
    val bus = draft.bus ?: MockData.buses.first()
    val seats = remember(bus.id) { MockData.seatsFor(bus) }
    var selected by remember { mutableStateOf(draft.seats) }
    val needed = draft.seatCount

    fun toggle(n: Int) {
        selected = when {
            n in selected -> selected - n
            selected.size < needed -> selected + n
            else -> selected.drop(1) + n // replace the oldest pick
        }
    }

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            StepTopBar(progress = 0.75f, onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                ScreenHeadline(
                    "Pick your seats",
                    "Choose $needed ${if (needed == 1) "seat" else "seats"}. We'll hold them for 10 minutes while you pay."
                )
                VSpace(24.dp)
                SeatLegend()
                VSpace(20.dp)
                SoftCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Front of bus", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Spacer(Modifier.weight(1f))
                        Text("Driver", style = MaterialTheme.typography.bodySmall, color = Muted)
                        HSpace(6.dp)
                        Icon(Icons.Outlined.AirlineSeatReclineNormal, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                    }
                    VSpace(12.dp)
                    HairlineDivider()
                    VSpace(12.dp)
                    seats.chunked(4).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.take(2).forEach { s -> SeatBox(s, s.number in selected) { toggle(s.number) } }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.drop(2).forEach { s -> SeatBox(s, s.number in selected) { toggle(s.number) } }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (selected.isEmpty()) "No seats yet" else "Seats ${selected.sorted().joinToString(", ")}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink
                    )
                    Text("${selected.size} of $needed selected", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                PillButton(
                    "Continue", { onContinue(selected) },
                    Modifier.width(150.dp),
                    enabled = selected.size == needed
                )
            }
        }
    }
}

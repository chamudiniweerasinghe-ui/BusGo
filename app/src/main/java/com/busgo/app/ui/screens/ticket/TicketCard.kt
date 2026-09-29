package com.busgo.app.ui.screens.ticket

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
fun TicketCard(ticket: Ticket, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.3f))
            .clip(shape)
            .background(Color.White)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagChip(ticket.bus.category.label)
                HSpace(8.dp)
                Text(ticket.bus.operator, style = MaterialTheme.typography.bodySmall, color = Muted)
                Spacer(Modifier.weight(1f))
                TagChip("Paid", container = Success.copy(alpha = 0.12f), content = Success)
            }
            VSpace(20.dp)
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(ticket.boarding.code, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp), color = Ink)
                    Text(ticket.boarding.name, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Icon(
                    Icons.Filled.DirectionsBus, contentDescription = null, tint = Rose,
                    modifier = Modifier.padding(bottom = 22.dp)
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(ticket.alighting.code, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp), color = Ink)
                    Text(ticket.alighting.name, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            VSpace(20.dp)
            Row {
                InfoBlock("Date", ticket.date.pretty(), Modifier.weight(1f))
                InfoBlock("Departs", ticket.bus.departure, Modifier.weight(1f))
            }
            VSpace(12.dp)
            Row {
                InfoBlock("Seats", ticket.seats.sorted().joinToString(", "), Modifier.weight(1f))
                InfoBlock("Bus", ticket.bus.number, Modifier.weight(1f))
            }
        }
        TicketTearLine(notchColor = Cream)
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QrCodeView(ticket.id, Modifier.size(180.dp))
            VSpace(12.dp)
            Text(ticket.id, style = MaterialTheme.typography.titleMedium, color = Ink)
            VSpace(2.dp)
            Text("Total paid ${ticket.fare.toLkr()}", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable
private fun InfoBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Ink)
    }
}

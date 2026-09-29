package com.busgo.app.ui.screens.tickets

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
fun TicketRow(ticket: Ticket, onClick: () -> Unit) {
    SoftCard(onClick = onClick, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChipSelected)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${ticket.date.dayOfMonth}", style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(
                    ticket.date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
                    style = MaterialTheme.typography.bodySmall, color = Muted
                )
            }
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    "${ticket.boarding.name} to ${ticket.alighting.name}",
                    style = MaterialTheme.typography.titleMedium, color = Ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                VSpace(2.dp)
                Text(
                    "${ticket.bus.departure}, ${ticket.bus.number}, seat ${ticket.seats.sorted().joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall, color = Muted
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

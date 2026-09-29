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
import com.busgo.app.ui.components.map.*

/** One bus in the search results — styled like the reference profile cards. */
@Composable
fun BusCard(
    bus: Bus,
    onTrack: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val live = bus.speedKmh > 0
    SoftCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BusAvatar(showLive = live)
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(bus.number, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(bus.operator, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                Text("Route ${bus.routeNo}, ${bus.category.label}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("from", style = MaterialTheme.typography.bodySmall, color = Muted)
                Text(bus.fullFare.toLkr(), style = MaterialTheme.typography.titleLarge, color = OrangeDeep)
            }
        }
        VSpace(18.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(bus.departure, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(bus.from, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Column(
                Modifier.weight(1f).padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(bus.duration, style = MaterialTheme.typography.bodySmall, color = Muted)
                AnimatedRouteLine(Modifier.fillMaxWidth())
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(bus.arrival, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(bus.to, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        VSpace(14.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Cream)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (live) {
                LiveDot()
                HSpace(6.dp)
                Text(
                    "On the road, ${bus.etaMinutes} min to your stop",
                    style = MaterialTheme.typography.bodySmall, color = InkSoft, modifier = Modifier.weight(1f)
                )
            } else {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                HSpace(6.dp)
                Text(
                    "Leaves the depot at ${bus.departure}",
                    style = MaterialTheme.typography.bodySmall, color = InkSoft, modifier = Modifier.weight(1f)
                )
            }
            Text(
                "${bus.seatsLeft} seats left",
                style = MaterialTheme.typography.labelMedium,
                color = if (bus.seatsLeft < 8) OrangeDeep else Ink
            )
        }
        VSpace(14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton(
                "Track live", onTrack, Modifier.weight(1f),
                style = PillStyle.Outline, leadingIcon = Icons.Outlined.MyLocation, height = 44.dp
            )
            PillButton("Book seat", onBook, Modifier.weight(1f), height = 44.dp)
        }
    }
}

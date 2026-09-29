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

/** Segment-based fare calculation shown during booking and on the payment screen. */
@Composable
fun FareBreakdownCard(draft: BookingDraft, modifier: Modifier = Modifier) {
    SoftCard(modifier) {
        Text("Fare estimate", style = MaterialTheme.typography.labelMedium, color = Muted)
        VSpace(12.dp)
        FareRow("Distance", "${kotlin.math.round(draft.distanceKm).toInt()} km")
        FareRow(
            "Rate (${draft.bus?.category?.label ?: "-"})",
            "LKR " + "%.2f".format(java.util.Locale.US, draft.bus?.ratePerKm ?: 0.0) + " / km"
        )
        FareRow("Fare per seat", draft.farePerSeat.toLkr())
        FareRow("Seats", "x ${draft.seatCount}")
        HairlineDivider(Modifier.padding(vertical = 12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Total", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(Modifier.weight(1f))
            AnimatedContent(targetState = draft.totalFare, label = "total") { total ->
                Text(total.toLkr(), style = MaterialTheme.typography.headlineMedium, color = Ink)
            }
        }
    }
}

@Composable
private fun FareRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

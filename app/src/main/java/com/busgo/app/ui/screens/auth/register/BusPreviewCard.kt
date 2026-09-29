package com.busgo.app.ui.screens.auth.register

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

/** Live preview of how the bus will look to passengers, updates as the owner types. */
@Composable
fun BusPreviewCard(form: RegisterFormState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Ink)
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TagChip(form.busCategory.label, container = Color.White.copy(alpha = 0.14f), content = Color.White)
            Spacer(Modifier.weight(1f))
            Text("${form.totalSeats} seats", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
        VSpace(16.dp)
        Text(
            form.busNumber.ifBlank { "Your bus number" }.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = if (form.busNumber.isBlank()) Color.White.copy(alpha = 0.35f) else Color.White
        )
        if (form.routeNo.isNotBlank()) {
            Text("Route ${form.routeNo}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
        VSpace(18.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PreviewTown(form.busFrom, "From", Modifier.weight(1f))
            Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Rose, modifier = Modifier.padding(horizontal = 10.dp).size(20.dp))
            PreviewTown(form.busTo, "To", Modifier.weight(1f), alignEnd = true)
        }
        VSpace(14.dp)
        Text(
            "First trip at ${form.firstDeparture}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PreviewTown(name: String, fallback: String, modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Column(modifier, horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(fallback, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.55f))
        Text(
            name.ifBlank { "..." },
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

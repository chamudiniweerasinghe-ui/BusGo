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

import com.busgo.app.ui.screens.home.RouteInputCard

/** Bus owner step: bus number, where it runs from and to, type, seats and first trip. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusDetailsStep(form: RegisterFormState) {
    val towns = listOf("Kurunegala", "Colombo Fort", "Kandy", "Dambulla", "Puttalam", "Negombo", "Anuradhapura")
    Column {
        ScreenHeadline(
            "Tell us about your bus",
            "Passengers find you by route, so use the same towns shown on the board at the front of the bus."
        )
        VSpace(24.dp)
        BusPreviewCard(form)
        VSpace(24.dp)

        SoftTextField(
            form.busNumber, { form.busNumber = it.uppercase() }, "Bus number",
            placeholder = "NB-2231", leadingIcon = Icons.Outlined.DirectionsBus
        )
        VSpace(16.dp)
        SoftTextField(
            form.routeNo, { form.routeNo = it }, "Route number (optional)",
            placeholder = "5", leadingIcon = Icons.Outlined.Map
        )

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Where does it run?")
        VSpace(12.dp)
        RouteInputCard(
            from = form.busFrom,
            to = form.busTo,
            onFromChange = { form.busFrom = it },
            onToChange = { form.busTo = it },
            onSwap = {
                val f = form.busFrom
                form.busFrom = form.busTo
                form.busTo = f
            }
        )
        VSpace(12.dp)
        Text("Or tap a town to fill it in", style = MaterialTheme.typography.bodySmall, color = Muted)
        VSpace(10.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            towns.forEach { town ->
                SelectChip(
                    town,
                    selected = town == form.busFrom || town == form.busTo,
                    onClick = {
                        when {
                            town == form.busFrom -> form.busFrom = ""
                            town == form.busTo -> form.busTo = ""
                            form.busFrom.isBlank() -> form.busFrom = town
                            else -> form.busTo = town
                        }
                    }
                )
            }
        }
        if (form.busFrom.isNotBlank() && form.busFrom.trim().equals(form.busTo.trim(), ignoreCase = true)) {
            VSpace(10.dp)
            Text("Start and end towns need to be different.", style = MaterialTheme.typography.bodySmall, color = Danger)
        }

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Bus type")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BusCategory.entries.forEach { c ->
                SelectChip(c.label, form.busCategory == c, { form.busCategory = c })
            }
        }

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Number of seats")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(32, 40, 44, 49, 54).forEach { n ->
                SelectChip("$n", form.totalSeats == n, { form.totalSeats = n })
            }
        }

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("First trip of the day")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("05:30", "06:00", "06:30", "07:00", "08:00").forEach { t ->
                SelectChip(t, form.firstDeparture == t, { form.firstDeparture = t }, icon = Icons.Outlined.Schedule)
            }
        }

        VSpace(24.dp)
        SoftCard(color = PeachSoft.copy(alpha = 0.7f), padding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Security, contentDescription = null, tint = Coral)
                HSpace(12.dp)
                Text(
                    "An admin checks new buses before they appear in passenger searches. This usually takes less than a day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
        }
    }
}

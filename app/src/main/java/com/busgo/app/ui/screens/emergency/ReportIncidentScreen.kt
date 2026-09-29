package com.busgo.app.ui.screens.emergency

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
fun ReportIncidentScreen(onBack: () -> Unit, onDone: () -> Unit) {
    var type by rememberSaveable { mutableStateOf<String?>(null) }
    var severity by rememberSaveable { mutableFloatStateOf(0.5f) }
    var note by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sent by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val incidentTypes = listOf(
        "Accident" to Icons.Outlined.Warning,
        "Breakdown" to Icons.Outlined.Build,
        "Medical" to Icons.Outlined.LocalHospital,
        "Road blocked" to Icons.Outlined.Block,
        "Other" to Icons.Outlined.MoreHoriz
    )
    val severityLabel = when {
        severity < 0.33f -> "Minor"
        severity < 0.66f -> "Serious"
        else -> "Critical"
    }

    GradientBackground(BusGoGradients.AlertTop) {
        AnimatedContent(targetState = sent, label = "incident") { isSent ->
            if (isSent) {
                IncidentSentView(onDone)
            } else {
                Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
                    BackTopBar(onBack, title = "Report incident")
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        ScreenHeadline(
                            "Report an incident",
                            "Everyone on board and their emergency contacts will be told what happened and where the bus is."
                        )
                        VSpace(28.dp)
                        HairlineDivider()
                        VSpace(20.dp)

                        SectionLabel("What happened?")
                        VSpace(12.dp)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            incidentTypes.forEach { (label, icon) ->
                                SelectChip(label, type == label, { type = label }, icon = icon)
                            }
                        }

                        VSpace(20.dp)
                        HairlineDivider()
                        VSpace(20.dp)

                        SectionLabel("How serious is it?", trailing = severityLabel)
                        VSpace(16.dp)
                        GradientSlider(
                            value = severity,
                            onValueChange = { severity = it },
                            labels = listOf("Minor", "Serious", "Critical"),
                            brush = BusGoGradients.DangerTrack,
                            dotColor = Danger
                        )

                        VSpace(20.dp)
                        HairlineDivider()
                        VSpace(20.dp)

                        SectionLabel("Last known location")
                        VSpace(12.dp)
                        LastLocationCard()
                        VSpace(20.dp)
                        SoftTextField(
                            note, { note = it }, "Add a note (optional)",
                            placeholder = "Front tyre burst, no injuries",
                            singleLine = false
                        )
                    }
                    PillButton(
                        "Send emergency alert",
                        onClick = {
                            sending = true
                            scope.launch {
                                delay(1500) // replace with POST /incidents
                                sending = false
                                sent = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        style = PillStyle.Danger,
                        enabled = type != null,
                        loading = sending,
                        leadingIcon = Icons.Outlined.Warning
                    )
                }
            }
        }
    }
}

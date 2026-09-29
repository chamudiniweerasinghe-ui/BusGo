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

/** Step 2 — boarding and alighting stops, with a live fare estimate. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SegmentScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onContinue: (Stop, Stop) -> Unit
) {
    val stops = MockData.routeStops
    var boarding by remember { mutableStateOf(draft.boarding ?: stops.first()) }
    var alighting by remember { mutableStateOf(draft.alighting ?: stops.last()) }
    val preview = draft.copy(boarding = boarding, alighting = alighting)

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            StepTopBar(progress = 0.5f, onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                ScreenHeadline(
                    "Where will you get on and off?",
                    "You only pay for the distance you actually travel, not the whole route."
                )
                VSpace(28.dp)
                HairlineDivider()
                VSpace(20.dp)

                SectionLabel("Boarding point")
                VSpace(12.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stops.dropLast(1).forEach { s ->
                        SelectChip(s.name, boarding == s, {
                            boarding = s
                            if (alighting.kmFromStart <= s.kmFromStart) alighting = stops.last()
                        })
                    }
                }

                VSpace(20.dp)
                HairlineDivider()
                VSpace(20.dp)

                SectionLabel("Getting off at")
                VSpace(12.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stops.filter { it.kmFromStart > boarding.kmFromStart }.forEach { s ->
                        SelectChip(s.name, alighting == s, { alighting = s })
                    }
                }

                VSpace(28.dp)
                FareBreakdownCard(preview)
            }
            PillButton(
                "Continue", { onContinue(boarding, alighting) },
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

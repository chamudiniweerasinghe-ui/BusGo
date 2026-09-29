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

/** Step 1 of booking — the dial picker, styled after the reference "cycle length" screen. */
@Composable
fun SeatCountScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onContinue: (Int) -> Unit
) {
    var count by rememberSaveable { mutableIntStateOf(draft.seatCount) }
    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            StepTopBar(progress = 0.25f, onBack = onBack, onSkip = { onContinue(1) })
            ScreenHeadline(
                "How many seats do you need?",
                "Book up to 6 seats together on ${draft.bus?.number ?: "this bus"}. Everyone travels on one e-ticket.",
                Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)
            )
            Spacer(Modifier.weight(1f))
            DialPicker(
                value = count,
                onValueChange = { count = it },
                range = 1..6,
                unit = if (count == 1) "seat" else "seats"
            )
            PillButton(
                "Continue", { onContinue(count) },
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

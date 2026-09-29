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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesStep(form: RegisterFormState) {
    Column {
        ScreenHeadline(
            "Tell us a bit about how you travel",
            "This helps us sort buses for you and only send alerts that matter."
        )
        VSpace(28.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Usual travel time")
        VSpace(16.dp)
        GradientSlider(
            value = form.travelTime,
            onValueChange = { form.travelTime = it },
            labels = listOf("Early", "Midday", "Night")
        )

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Preferred bus type")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BusCategory.entries.forEach { c ->
                SelectChip(c.label, c.label in form.busTypes, { form.busTypes = form.busTypes.toggle(c.label) })
            }
        }

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Seat preference")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Window", "Aisle", "No preference").forEach {
                SelectChip(it, form.seatPreference == it, { form.seatPreference = it })
            }
        }

        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)

        SectionLabel("Notify me about")
        VSpace(12.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Bus arriving", "Delays", "Route changes", "Offers").forEach {
                SelectChip(it, it in form.notifyAbout, { form.notifyAbout = form.notifyAbout.toggle(it) })
            }
        }
    }
}

package com.busgo.app.ui.screens.admin

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddItemSheet(tab: AdminTab, onDismiss: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BusCategory.NORMAL) }
    val (firstLabel, secondLabel) = when (tab) {
        AdminTab.BUSES -> "Bus number" to "Total seats"
        AdminTab.ROUTES -> "Route name" to "Stops (comma separated)"
        AdminTab.SCHEDULES -> "Departure time" to "Bus number"
        AdminTab.FARES -> "Bus type" to "Rate per km (LKR)"
        AdminTab.DRIVERS -> "Full name" to "Licence number"
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Cream) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).imePadding()) {
            Text("Add a ${tab.singular}", style = MaterialTheme.typography.headlineMedium, color = Ink)
            VSpace(20.dp)
            SoftTextField(first, { first = it }, firstLabel)
            VSpace(14.dp)
            SoftTextField(second, { second = it }, secondLabel)
            if (tab == AdminTab.BUSES) {
                VSpace(20.dp)
                SectionLabel("Category")
                VSpace(12.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BusCategory.entries.forEach { c ->
                        SelectChip(c.label, category == c, { category = c })
                    }
                }
            }
            VSpace(24.dp)
            PillButton("Save ${tab.singular}", onDismiss, enabled = first.isNotBlank())
        }
    }
}

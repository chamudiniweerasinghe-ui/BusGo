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
fun EmergencyContactStep(form: RegisterFormState) {
    Column {
        ScreenHeadline(
            "Who should we contact in an emergency?",
            "If your bus reports an incident, we'll alert this person with the bus's last known location."
        )
        VSpace(28.dp)
        SoftTextField(form.contactName, { form.contactName = it }, "Contact name",
            placeholder = "Nirmala Perera", leadingIcon = Icons.Outlined.Person)
        VSpace(16.dp)
        SoftTextField(form.contactPhone, { form.contactPhone = it }, "Contact number",
            placeholder = "+94 71 555 0192", leadingIcon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone)
        VSpace(24.dp)
        HairlineDivider()
        VSpace(20.dp)
        SectionLabel("Relationship")
        VSpace(12.dp)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Parent", "Spouse", "Sibling", "Friend", "Guardian").forEach {
                SelectChip(it, form.relationship == it, { form.relationship = it })
            }
            SelectChip("Add custom", false, { }, icon = Icons.Outlined.Add)
        }
        VSpace(24.dp)
        SoftCard(color = PeachSoft.copy(alpha = 0.7f), padding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Security, contentDescription = null, tint = Coral)
                HSpace(12.dp)
                Text(
                    "Only used for safety alerts. It's never shared with bus operators.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
        }
    }
}

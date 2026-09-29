package com.busgo.app.ui.screens.alerts

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

@Composable
fun AlertRow(alert: AlertItem) {
    val (icon, tint) = when (alert.type) {
        AlertType.DELAY -> Icons.Outlined.Schedule to Coral
        AlertType.BOOKING -> Icons.Outlined.ConfirmationNumber to Success
        AlertType.INFO -> Icons.Outlined.Info to Ink
        AlertType.EMERGENCY -> Icons.Outlined.Warning to Danger
    }
    SoftCard(padding = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).background(ChipSelected, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Row {
                    Text(alert.title, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
                    Text(alert.time, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                VSpace(4.dp)
                Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}

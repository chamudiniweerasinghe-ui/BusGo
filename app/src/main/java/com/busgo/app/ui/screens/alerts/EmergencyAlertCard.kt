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
fun EmergencyAlertCard(alert: AlertItem, onViewOnMap: () -> Unit) {
    SoftCard(color = DangerSoft) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).background(Danger, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Text(alert.title, style = MaterialTheme.typography.titleMedium, color = Ink)
                VSpace(4.dp)
                Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
                VSpace(8.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
                    HSpace(6.dp)
                    Text("Your emergency contact was notified", style = MaterialTheme.typography.bodySmall, color = InkSoft)
                }
                VSpace(4.dp)
                Text(alert.time, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        VSpace(16.dp)
        PillButton(
            "See last known location", onViewOnMap,
            style = PillStyle.Danger, leadingIcon = Icons.Outlined.Place, height = 46.dp
        )
    }
}

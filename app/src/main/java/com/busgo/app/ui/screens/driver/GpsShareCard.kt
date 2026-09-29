package com.busgo.app.ui.screens.driver

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
fun GpsShareCard(sharing: Boolean, onToggle: (Boolean) -> Unit) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(if (sharing) Success.copy(alpha = 0.12f) else ChipSelected, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.GpsFixed, contentDescription = null,
                    tint = if (sharing) Success else Muted, modifier = Modifier.size(22.dp)
                )
            }
            HSpace(14.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (sharing) "Sharing live location" else "Location sharing paused",
                        style = MaterialTheme.typography.titleMedium, color = Ink
                    )
                    if (sharing) {
                        HSpace(6.dp)
                        LiveDot()
                    }
                }
                Text(
                    if (sharing) "Passengers can see this bus on the map" else "Passengers can't see this bus right now",
                    style = MaterialTheme.typography.bodySmall, color = Muted
                )
            }
            Switch(
                checked = sharing,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Ink,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = Hairline,
                    uncheckedThumbColor = Color.White,
                    uncheckedBorderColor = Hairline
                )
            )
        }
    }
}

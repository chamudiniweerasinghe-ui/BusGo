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
fun AlertsScreen(alerts: List<AlertItem>, onViewOnMap: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var realAlerts by remember { mutableStateOf<List<AlertItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        com.busgo.app.data.AlertNetworkManager.getAlerts(context) { fetched ->
            if (fetched.isNotEmpty()) {
                realAlerts = fetched
            }
        }
    }

    val displayAlerts = if (realAlerts.isNotEmpty()) realAlerts else alerts

    Column(Modifier.fillMaxSize().background(Cream)) {
        NavyHeader(title = "Alerts", leading = { GridLogo() }, actions = { LiveBadge(onDark = true) }) {
            VSpace(10.dp)
            Text(
                "Safety notices, delays and booking updates for your trips.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(displayAlerts, key = { it.id }) { a ->
                if (a.type == AlertType.EMERGENCY) EmergencyAlertCard(a, onViewOnMap) else AlertRow(a)
            }
        }
    }
}

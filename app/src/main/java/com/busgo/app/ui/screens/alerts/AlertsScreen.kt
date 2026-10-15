package com.busgo.app.ui.screens.alerts

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.busgo.app.data.AlertNetworkManager
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlertsScreen(alerts: List<AlertItem>, onViewOnMap: () -> Unit) {
    val context = LocalContext.current
    var realAlerts by remember { mutableStateOf<List<AlertItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isClearing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AlertNetworkManager.getAlerts(context) { fetched ->
            realAlerts = fetched
            isLoading = false
        }
    }

    val displayAlerts = realAlerts

    Column(Modifier.fillMaxSize().background(Cream)) {
        NavyHeader(title = "Alerts", leading = { GridLogo() }, actions = { LiveBadge(onDark = true) }) {
            VSpace(10.dp)
            Text(
                "Safety notices, departure reminders and route alerts for your trips.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }
            } else if (displayAlerts.isEmpty()) {
                item {
                    EmptyState(Icons.Outlined.NotificationsNone, "No active alerts", "Departure reminders and route alerts will appear here.")
                }
            } else {
                items(displayAlerts, key = { it.id }) { a ->
                    if (a.type == AlertType.EMERGENCY) EmergencyAlertCard(a, onViewOnMap) else AlertRow(a)
                }
                item {
                    VSpace(8.dp)
                    PillButton(
                        text = "CLEAR ALL NOTIFICATIONS",
                        onClick = {
                            isClearing = true
                            AlertNetworkManager.clearAlerts(context) { success, msg ->
                                isClearing = false
                                if (success) {
                                    realAlerts = emptyList()
                                    Toast.makeText(context, "Notifications cleared successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        loading = isClearing,
                        enabled = !isClearing,
                        style = PillStyle.Outline,
                        leadingIcon = Icons.Outlined.DeleteSweep,
                        height = 42.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

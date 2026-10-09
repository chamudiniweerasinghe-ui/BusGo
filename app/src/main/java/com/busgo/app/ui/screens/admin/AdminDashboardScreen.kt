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
import com.busgo.app.data.AdminNetworkManager
import com.busgo.app.data.mock.MockData
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Operator / admin view: fleet stats, incidents and CRUD lists. */
@Composable
fun AdminDashboardScreen(onOpenIncident: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(AdminTab.BUSES) }
    var showAdd by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var stats by remember { mutableStateOf<AdminStats?>(null) }
    var itemsList by remember { mutableStateOf<List<AdminItem>>(emptyList()) }
    var openIncidents by remember { mutableStateOf<List<AdminIncident>>(emptyList()) }
    var isLoadingItems by remember { mutableStateOf(true) }

    // Load live stats & incidents from backend
    LaunchedEffect(refreshTrigger) {
        AdminNetworkManager.getAdminStats(context) { success, fetchedStats, _ ->
            if (success && fetchedStats != null) {
                stats = fetchedStats
            }
        }
        AdminNetworkManager.getAdminIncidents(context) { success, fetchedIncidents, _ ->
            if (success) {
                openIncidents = fetchedIncidents
            }
        }
    }

    // Load tab items from backend
    LaunchedEffect(tab, refreshTrigger) {
        isLoadingItems = true
        AdminNetworkManager.getAdminItems(context, tab) { success, fetchedItems, _ ->
            if (success) {
                itemsList = fetchedItems
            }
            isLoadingItems = false
        }
    }

    val icon = when (tab) {
        AdminTab.BUSES -> Icons.Outlined.DirectionsBus
        AdminTab.ROUTES -> Icons.Outlined.Map
        AdminTab.SCHEDULES -> Icons.Outlined.Schedule
        AdminTab.FARES -> Icons.Outlined.AttachMoney
        AdminTab.DRIVERS -> Icons.Outlined.AccountCircle
    }

    val displayedItems = if (itemsList.isNotEmpty()) itemsList else MockData.adminItems(tab)

    Box(Modifier.fillMaxSize().background(Cream)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NavyHeader(
                    title = "Operator panel",
                    leading = { GridLogo() },
                    actions = {
                        AnimatedBell(onOpenIncident)
                        HeaderIconButton(Icons.AutoMirrored.Outlined.Logout, "Log out", onLogout)
                    }
                ) {
                    VSpace(18.dp)
                    Text("Good morning, Ceylon Express", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("Here's how your fleet is doing today.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    VSpace(8.dp)
                    IncidentBanner(
                        onOpen = onOpenIncident,
                        incidentsCount = openIncidents.size,
                        incidentSummary = openIncidents.firstOrNull()?.description ?: "1 open incident"
                    )
                    VSpace(12.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Icons.Outlined.DirectionsBus, stats?.busesRunning ?: "12", "Buses running", Modifier.weight(1f))
                        StatTile(Icons.Outlined.ConfirmationNumber, stats?.bookingsToday ?: "348", "Bookings today", Modifier.weight(1f))
                    }
                    VSpace(10.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Icons.Outlined.AttachMoney, stats?.revenueToday ?: "LKR 184k", "Revenue today", Modifier.weight(1f))
                        StatTile(Icons.Outlined.EventSeat, stats?.occupancyRate ?: "82%", "Seats filled", Modifier.weight(1f))
                    }
                    VSpace(20.dp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AdminTab.entries) { t -> SelectChip(t.label, tab == t, { tab = t }) }
                    }
                }
            }
            items(displayedItems) {
                Box(Modifier.padding(horizontal = 20.dp)) { AdminListRow(it, icon) }
            }
        }
        PillButton(
            "ADD ${tab.singular.uppercase()}", { showAdd = true },
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
                .fillMaxWidth(),
            leadingIcon = Icons.Outlined.Add
        )
        if (showAdd) {
            AddItemSheet(
                tab = tab,
                onDismiss = { showAdd = false },
                onItemCreated = { refreshTrigger++ }
            )
        }
    }
}

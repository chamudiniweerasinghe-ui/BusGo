package com.busgo.app.ui.screens.admin

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

/** Operator / admin view: fleet stats, incidents, CRUD lists, and Danger Zone. */
@Composable
fun AdminDashboardScreen(onOpenIncident: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(AdminTab.BUSES) }
    var showAdd by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<AdminItem?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var stats by remember { mutableStateOf<AdminStats?>(null) }
    var itemsList by remember { mutableStateOf<List<AdminItem>>(emptyList()) }
    var openIncidents by remember { mutableStateOf<List<AdminIncident>>(emptyList()) }
    var isLoadingItems by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var isResetting by remember { mutableStateOf(false) }

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
            items(displayedItems) { item ->
                Box(Modifier.padding(horizontal = 20.dp)) {
                    AdminListRow(
                        item = item,
                        icon = icon,
                        onDelete = { itemToDelete = it }
                    )
                }
            }
            item {
                VSpace(20.dp)
                Column(Modifier.padding(horizontal = 20.dp)) {
                    SectionTitle("Danger Zone")
                    VSpace(10.dp)
                    SoftCard(color = DangerSoft, padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Warning, contentDescription = null, tint = Danger, modifier = Modifier.size(24.dp))
                                HSpace(10.dp)
                                Column(Modifier.weight(1f)) {
                                    Text("Reset Database Test Data", style = MaterialTheme.typography.titleMedium, color = Ink)
                                    Text("Clear GPS locations, open incidents, and reset seat holds in PostgreSQL.", style = MaterialTheme.typography.bodySmall, color = InkSoft)
                                }
                            }
                            PillButton(
                                text = "RESET DATABASE TEST DATA",
                                onClick = { showResetDialog = true },
                                style = PillStyle.Danger,
                                height = 40.dp
                            )
                        }
                    }
                }
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

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) itemToDelete = null },
            title = { Text("Delete ${tab.singular}?", style = MaterialTheme.typography.titleLarge, color = Ink) },
            text = { Text("Are you sure you want to delete '${item.title}'? This action is permanent.", style = MaterialTheme.typography.bodyMedium, color = InkSoft) },
            confirmButton = {
                PillButton(
                    text = "Delete",
                    onClick = {
                        isDeleting = true
                        AdminNetworkManager.deleteAdminItem(context, item.id, tab) { success, message ->
                            isDeleting = false
                            itemToDelete = null
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                refreshTrigger++
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    loading = isDeleting,
                    enabled = !isDeleting,
                    style = PillStyle.Danger,
                    height = 42.dp,
                    modifier = Modifier.width(100.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { if (!isDeleting) itemToDelete = null }, enabled = !isDeleting) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Danger Zone Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResetting) showResetDialog = false },
            title = { Text("Confirm Database Reset", style = MaterialTheme.typography.titleLarge, color = Danger) },
            text = { Text("This will clear all test GPS locations, open incidents, and reset all bus seat states in PostgreSQL. Continue?", style = MaterialTheme.typography.bodyMedium, color = InkSoft) },
            confirmButton = {
                PillButton(
                    text = "Confirm Reset",
                    onClick = {
                        isResetting = true
                        AdminNetworkManager.resetDatabase(context) { success, message ->
                            isResetting = false
                            showResetDialog = false
                            if (success) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                refreshTrigger++
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    loading = isResetting,
                    enabled = !isResetting,
                    style = PillStyle.Danger,
                    height = 42.dp,
                    modifier = Modifier.width(140.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { if (!isResetting) showResetDialog = false }, enabled = !isResetting) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

package com.busgo.app.ui.screens.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.busgo.app.data.BusNetworkManager
import com.busgo.app.data.DriverNetworkManager
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Bus account home: share GPS, verify tickets and report incidents. */
@Composable
fun DriverScreen(
    bus: Bus,
    onReportIncident: () -> Unit,
    onLogout: () -> Unit,
    onBusUpdated: (Bus) -> Unit = {}
) {
    var activeBus by remember(bus) { mutableStateOf(bus) }
    var sharing by rememberSaveable { mutableStateOf(true) }
    var showScan by remember { mutableStateOf(false) }
    var showPassengers by remember { mutableStateOf(false) }
    var showAddBus by remember { mutableStateOf(false) }
    var boarded by rememberSaveable { mutableIntStateOf(0) }
    var registeredBuses by remember { mutableStateOf<List<Bus>>(emptyList()) }
    var isLoadingBuses by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Fetch registered buses from PostgreSQL database
    LaunchedEffect(activeBus.id) {
        isLoadingBuses = true
        BusNetworkManager.getAllBuses { fetched ->
            registeredBuses = fetched
            isLoadingBuses = false
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fineGranted || coarseGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Lifecycle-safe periodic GPS updates (Target: 5s interval)
    DisposableEffect(sharing, hasLocationPermission, activeBus.id) {
        if (!sharing || !hasLocationPermission) {
            return@DisposableEffect onDispose {}
        }

        val numericBusId = activeBus.id.replace("\\D".toRegex(), "").toIntOrNull() ?: 1

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).setMinUpdateIntervalMillis(3000L).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                DriverNetworkManager.updateDriverLocation(
                    context = context,
                    busId = numericBusId,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    speed = loc.speed.toDouble() * 3.6, // m/s to km/h
                    heading = loc.bearing.toDouble()
                ) { _, _ ->
                    // Fail safely without interrupting UI
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.w("DriverScreen", "Location permission not granted", e)
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        Column(Modifier.fillMaxSize()) {
            // FIXED Navy Header at the Top (Non-scrollable)
            NavyHeader(
                title = "My bus",
                leading = { GridLogo() },
                actions = {
                    AnimatedBell(onReportIncident)
                    HeaderIconButton(Icons.AutoMirrored.Outlined.Logout, "Log out", onLogout)
                }
            ) {
                VSpace(20.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BusAvatar(size = 60.dp, container = Orange, showLive = sharing)
                    HSpace(16.dp)
                    Column(Modifier.weight(1f)) {
                        Text(activeBus.number.ifBlank { "My Bus" }, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        Text(
                            if (activeBus.routeNo.isBlank()) "${activeBus.category.label} bus" else "Route ${activeBus.routeNo}, ${activeBus.category.label} bus",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                VSpace(18.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("From", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        Text(activeBus.from.ifBlank { "Origin" }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    AnimatedRouteLine(
                        Modifier.width(90.dp).padding(horizontal = 8.dp),
                        lineColor = Color.White.copy(alpha = 0.4f),
                        endpointColor = Color.White,
                        busTint = Color.White,
                        iconBackground = Orange
                    )
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("To", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        Text(activeBus.to.ifBlank { "Destination" }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }

            // Scrollable Content Below Navy Header
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 100.dp)
            ) {
                GpsShareCard(sharing) { sharing = it }
                VSpace(12.dp)
                
                // On Board Stat Tile
                StatTile(
                    Icons.Outlined.People,
                    "$boarded/${activeBus.totalSeats}",
                    "On board",
                    Modifier.fillMaxWidth()
                )

                VSpace(20.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Registered Fleet Buses", Modifier.weight(1f))
                    Text("${registeredBuses.size} buses", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                VSpace(12.dp)

                // Scrollable Card Container for Registered Buses List
                SoftCard(
                    padding = 16.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoadingBuses) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Orange)
                        }
                    } else if (registeredBuses.isEmpty()) {
                        Text(
                            "No registered buses in database. Use Admin panel 'ADD BUS' to register new buses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            registeredBuses.forEach { regBus ->
                                val isCurrent = regBus.id == activeBus.id || regBus.number == activeBus.number
                                SoftCard(
                                    onClick = {
                                        activeBus = regBus
                                        onBusUpdated(regBus)
                                    },
                                    color = if (isCurrent) OrangeSoft.copy(alpha = 0.5f) else Cream,
                                    padding = 12.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            BusAvatar(size = 36.dp, container = if (isCurrent) Orange else Navy)
                                            HSpace(10.dp)
                                            Column(Modifier.weight(1f)) {
                                                Text(regBus.number, style = MaterialTheme.typography.titleMedium, color = Ink)
                                                Text("${regBus.operator} • Route ${regBus.routeNo}", style = MaterialTheme.typography.bodySmall, color = Muted)
                                            }
                                            TagChip(
                                                text = if (isCurrent) "OPERATING" else regBus.category.label,
                                                container = if (isCurrent) Orange else CreamDeep,
                                                content = if (isCurrent) Color.White else Ink
                                            )
                                        }
                                        HairlineDivider()
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(regBus.from, style = MaterialTheme.typography.bodySmall, color = Ink, fontWeight = FontWeight.Bold)
                                            HSpace(6.dp)
                                            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                                            HSpace(6.dp)
                                            Text(regBus.to, style = MaterialTheme.typography.bodySmall, color = Ink, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.weight(1f))
                                            Text("LKR ${Math.round(regBus.ratePerKm * regBus.routeKm)}", style = MaterialTheme.typography.titleMedium, color = OrangeDeep)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed Position Bell Pop-Up Button on Bottom Left ("Report an incident")
        Surface(
            onClick = onReportIncident,
            shape = CircleShape,
            color = Danger,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        Icons.Outlined.NotificationsActive,
                        contentDescription = "Report an incident",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Yellow)
                    )
                }
                HSpace(8.dp)
                Text(
                    text = "Report an incident",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        // Circular Round Floating QR Scan Button on Bottom Right
        Surface(
            onClick = { showScan = true },
            shape = CircleShape,
            color = Orange,
            shadowElevation = 12.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 24.dp)
                .size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan QR Code",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Dialogs & Sheets
    if (showScan) {
        ScanResultDialog(onBoard = { boarded++; showScan = false }, onDismiss = { showScan = false })
    }

    if (showPassengers) {
        PassengersDialog(
            bus = activeBus,
            boardedCount = boarded,
            onScan = { showScan = true },
            onDismiss = { showPassengers = false }
        )
    }

    if (showAddBus) {
        AddNewBusDialog(
            onBusCreated = { createdBus ->
                activeBus = createdBus
                onBusUpdated(createdBus)
            },
            onDismiss = { showAddBus = false }
        )
    }
}

/** "Add New Bus" Popup Form Dialog matching PostgreSQL buses table columns. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNewBusDialog(
    onBusCreated: (Bus) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var plateNumber by remember { mutableStateOf("") }
    var operatorName by remember { mutableStateOf("") }
    var routeNumber by remember { mutableStateOf("06") }
    var category by remember { mutableStateOf(BusCategory.NORMAL) }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("06:00") }
    var arrivalTime by remember { mutableStateOf("08:30") }
    var duration by remember { mutableStateOf("2h 30m") }
    var seatCapacityText by remember { mutableStateOf("40") }
    var fareText by remember { mutableStateOf("850") }
    var selectedAmenities by remember { mutableStateOf(setOf("A/C")) }
    var isSubmitting by remember { mutableStateOf(false) }

    val availableAmenities = listOf("A/C", "Wi-Fi", "USB Charging", "Reclining Seats", "Curtains")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = Cream,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                Icons.Outlined.DirectionsBus,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Add New Bus",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SoftTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = "Plate Number*",
                    placeholder = "e.g. NC-4589",
                    leadingIcon = Icons.Outlined.ConfirmationNumber
                )

                SoftTextField(
                    value = operatorName,
                    onValueChange = { operatorName = it },
                    label = "Operator Name",
                    placeholder = "e.g. Wayamba Express",
                    leadingIcon = Icons.Outlined.Business
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = routeNumber,
                            onValueChange = { routeNumber = it },
                            label = "Route Number",
                            placeholder = "e.g. 06",
                            leadingIcon = Icons.Outlined.Route
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = seatCapacityText,
                            onValueChange = { seatCapacityText = it },
                            label = "Seat Capacity",
                            placeholder = "e.g. 40",
                            leadingIcon = Icons.Outlined.EventSeat,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                SectionLabel("Bus Category")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BusCategory.entries.forEach { cat ->
                        SelectChip(
                            label = cat.label,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = origin,
                            onValueChange = { origin = it },
                            label = "Origin*",
                            placeholder = "e.g. Kurunegala",
                            leadingIcon = Icons.Outlined.Place
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = "Destination*",
                            placeholder = "e.g. Colombo Fort",
                            leadingIcon = Icons.Outlined.Place
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = departureTime,
                            onValueChange = { departureTime = it },
                            label = "Departure",
                            placeholder = "06:00",
                            leadingIcon = Icons.Outlined.Schedule
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = arrivalTime,
                            onValueChange = { arrivalTime = it },
                            label = "Arrival",
                            placeholder = "08:30",
                            leadingIcon = Icons.Outlined.Schedule
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Trip Duration",
                            placeholder = "2h 30m",
                            leadingIcon = Icons.Outlined.Timer
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SoftTextField(
                            value = fareText,
                            onValueChange = { fareText = it },
                            label = "Ticket Fare (LKR)",
                            placeholder = "850",
                            leadingIcon = Icons.Outlined.AttachMoney,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                SectionLabel("Amenities")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableAmenities.forEach { amenity ->
                        val isSelected = amenity in selectedAmenities
                        SelectChip(
                            label = amenity,
                            selected = isSelected,
                            onClick = {
                                selectedAmenities = if (isSelected) {
                                    selectedAmenities - amenity
                                } else {
                                    selectedAmenities + amenity
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            PillButton(
                text = "Done",
                onClick = {
                    if (plateNumber.isBlank()) {
                        Toast.makeText(context, "Please enter plate number", Toast.LENGTH_SHORT).show()
                        return@PillButton
                    }
                    if (origin.isBlank() || destination.isBlank()) {
                        Toast.makeText(context, "Please enter origin and destination", Toast.LENGTH_SHORT).show()
                        return@PillButton
                    }

                    val seats = seatCapacityText.toIntOrNull() ?: 40
                    val fareVal = fareText.toDoubleOrNull() ?: 850.0

                    isSubmitting = true
                    BusNetworkManager.createBus(
                        context = context,
                        plateNumber = plateNumber,
                        operatorName = operatorName.ifBlank { "Ceylon Express" },
                        routeNumber = routeNumber.ifBlank { "06" },
                        category = category.label,
                        origin = origin,
                        destination = destination,
                        departureTime = departureTime.ifBlank { "06:00" },
                        arrivalTime = arrivalTime.ifBlank { "08:30" },
                        duration = duration.ifBlank { "2h 30m" },
                        seatCapacity = seats,
                        fare = fareVal,
                        amenities = selectedAmenities.toList()
                    ) { success, message, createdBus ->
                        isSubmitting = false
                        if (success && createdBus != null) {
                            Toast.makeText(context, "Bus saved to database successfully!", Toast.LENGTH_SHORT).show()
                            onBusCreated(createdBus)
                            onDismiss()
                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                loading = isSubmitting,
                enabled = !isSubmitting,
                height = 42.dp
            )
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isSubmitting) onDismiss() },
                enabled = !isSubmitting
            ) {
                Text("Cancel", color = Muted)
            }
        }
    )
}

/** Passengers Manifest Dialog showing booked seats and status. */
@Composable
fun PassengersDialog(
    bus: Bus,
    boardedCount: Int,
    onScan: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                Icons.Outlined.People,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Passengers Manifest",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
                VSpace(4.dp)
                Text(
                    text = "$boardedCount / ${bus.totalSeats} seats filled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (boardedCount == 0) {
                    Text(
                        "No passengers checked in yet. Scan passenger tickets to check them in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                } else {
                    (1..boardedCount).forEach { seat ->
                        SoftCard(color = Color.White, padding = 12.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OrangeSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "#$seat",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OrangeDeep
                                    )
                                }
                                HSpace(12.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Passenger (Seat $seat)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Ink
                                    )
                                    Text(
                                        "Checked In & Scanned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Success
                                    )
                                }
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PillButton(
                text = "Scan Ticket",
                onClick = {
                    onDismiss()
                    onScan()
                },
                leadingIcon = Icons.Outlined.QrCodeScanner,
                height = 42.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Muted)
            }
        }
    )
}

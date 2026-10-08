package com.busgo.app.ui.screens.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
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
import com.busgo.app.data.DriverNetworkManager
import com.busgo.app.data.mock.MockData
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
fun DriverScreen(bus: Bus, onReportIncident: () -> Unit, onLogout: () -> Unit) {
    var sharing by rememberSaveable { mutableStateOf(true) }
    var showScan by remember { mutableStateOf(false) }
    var boarded by rememberSaveable { mutableIntStateOf(0) }

    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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
    DisposableEffect(sharing, hasLocationPermission, bus.id) {
        if (!sharing || !hasLocationPermission) {
            return@DisposableEffect onDispose {}
        }

        val numericBusId = bus.id.replace("\\D".toRegex(), "").toIntOrNull() ?: 1

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

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {
        NavyHeader(
            title = "My bus",
            leading = { GridLogo() },
            actions = {
                AnimatedBell({ })
                HeaderIconButton(Icons.AutoMirrored.Outlined.Logout, "Log out", onLogout)
            }
        ) {
            VSpace(20.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                BusAvatar(size = 60.dp, container = Orange, showLive = sharing)
                HSpace(16.dp)
                Column(Modifier.weight(1f)) {
                    Text(bus.number, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text(
                        if (bus.routeNo.isBlank()) "${bus.category.label} bus" else "Route ${bus.routeNo}, ${bus.category.label} bus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            VSpace(18.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("From", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text(bus.from, style = MaterialTheme.typography.titleMedium, color = Color.White)
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
                    Text(bus.to, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }

        Column(Modifier.padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 32.dp)) {
            GpsShareCard(sharing) { sharing = it }
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Icons.Outlined.People, "$boarded/${bus.totalSeats}", "On board", Modifier.weight(1f))
                StatTile(Icons.Outlined.Schedule, bus.departure, "First trip", Modifier.weight(1f))
                StatTile(Icons.Outlined.Speed, "${bus.speedKmh} km/h", "Speed", Modifier.weight(1f))
            }
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(Icons.Outlined.QrCodeScanner, "Scan ticket", "Check a passenger's QR", Modifier.weight(1f)) {
                    showScan = true
                }
                ActionTile(Icons.Outlined.People, "Passengers", "See who's booked", Modifier.weight(1f)) { }
            }
            VSpace(28.dp)
            PillButton("REPORT AN INCIDENT", onReportIncident, style = PillStyle.Danger, leadingIcon = Icons.Outlined.Warning)
            VSpace(10.dp)
            Text(
                "Passengers and their emergency contacts get this bus's location straight away.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (showScan) {
        ScanResultDialog(onBoard = { boarded++; showScan = false }, onDismiss = { showScan = false })
    }
}

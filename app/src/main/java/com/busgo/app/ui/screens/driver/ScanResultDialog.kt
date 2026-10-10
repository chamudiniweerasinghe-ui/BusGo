package com.busgo.app.ui.screens.driver

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
import com.busgo.app.data.TicketNetworkManager
import com.busgo.app.data.mock.MockData
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VerificationResultData(
    val statusText: String,
    val isValid: Boolean,
    val passengerName: String,
    val seatNumbers: String,
    val routeText: String,
    val travelDate: String,
    val totalPrice: String,
    val ticketNumber: String
)

/** Ticket Scanner & Verification Dialog for Drivers. */
@Composable
fun ScanResultDialog(onBoard: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var ticketNumberInput by remember { mutableStateOf("") }
    var verificationResult by remember { mutableStateOf<VerificationResultData?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var isBoarding by remember { mutableStateOf(false) }

    if (verificationResult == null) {
        // Step 1: Input or Scan Ticket Number
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Cream,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Orange,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Verify & Scan E-Ticket",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enter or scan ticket number (e.g. TKT-...)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )
                    SoftTextField(
                        value = ticketNumberInput,
                        onValueChange = { ticketNumberInput = it },
                        label = "Ticket number",
                        placeholder = "e.g. TKT-1791625152078-4176"
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "OK",
                    onClick = {
                        if (ticketNumberInput.isBlank()) return@PillButton
                        isVerifying = true
                        TicketNetworkManager.verifyTicket(context, ticketNumberInput.trim()) { success, msg, isValid, passName, seatsStr, routeText, travelDate, priceStr, ticketNum ->
                            isVerifying = false
                            if (success) {
                                verificationResult = VerificationResultData(
                                    statusText = msg,
                                    isValid = isValid,
                                    passengerName = passName ?: "Passenger",
                                    seatNumbers = seatsStr ?: "Seat assigned",
                                    routeText = routeText ?: "Route Details",
                                    travelDate = travelDate ?: "Date assigned",
                                    totalPrice = priceStr ?: "LKR 0",
                                    ticketNumber = ticketNum ?: ticketNumberInput.trim()
                                )
                            } else {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    loading = isVerifying,
                    enabled = ticketNumberInput.isNotBlank() && !isVerifying,
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !isVerifying) {
                    Text("Cancel", color = Muted)
                }
            }
        )
    } else {
        // Step 2: Verification Result Popup
        val result = verificationResult!!
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Cream,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = if (result.isValid) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = null,
                    tint = if (result.isValid) Success else Danger,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TagChip(
                        text = if (result.isValid) "VERIFIED" else "ALREADY SCANNED",
                        container = if (result.isValid) Success.copy(alpha = 0.15f) else Danger.copy(alpha = 0.15f),
                        content = if (result.isValid) Success else Danger
                    )
                    VSpace(6.dp)
                    Text(
                        text = if (result.isValid) "Ticket Verified" else "Ticket Already Scanned",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                SoftCard(color = Color.White, padding = 16.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Ticket:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.ticketNumber, style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        HairlineDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Passenger:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.passengerName, style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        HairlineDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Booking Date:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.travelDate, style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        HairlineDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.DirectionsBus, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Route:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.routeText, style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        HairlineDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EventSeat, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Seats:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.seatNumbers, style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        HairlineDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                            HSpace(8.dp)
                            Text("Total Price:", style = MaterialTheme.typography.bodySmall, color = Muted)
                            HSpace(4.dp)
                            Text(result.totalPrice, style = MaterialTheme.typography.titleMedium, color = OrangeDeep)
                        }
                    }
                }
            },
            confirmButton = {
                PillButton(
                    text = "OK",
                    onClick = {
                        if (result.isValid) {
                            isBoarding = true
                            TicketNetworkManager.boardTicket(context, result.ticketNumber) { success, msg ->
                                isBoarding = false
                                if (success) {
                                    Toast.makeText(context, "Ticket Scanned & Marked in Database!", Toast.LENGTH_SHORT).show()
                                    onBoard()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            }
                        } else {
                            onDismiss()
                        }
                    },
                    loading = isBoarding,
                    enabled = !isBoarding,
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !isBoarding) {
                    Text("Close", color = Muted)
                }
            }
        )
    }
}

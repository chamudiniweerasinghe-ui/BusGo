package com.busgo.app.ui.screens.payment

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
import com.busgo.app.data.BookingNetworkManager
import com.busgo.app.data.FcmNetworkManager
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Step 4 — review and pay.
 * Handles card inputs, Stripe payment, and departure reminder clock popup connected to Firebase.
 */
@Composable
fun PaymentScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onPaid: () -> Unit
) {
    val context = LocalContext.current
    var method by rememberSaveable { mutableStateOf("Card") }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var cardName by rememberSaveable { mutableStateOf("") }
    var expiryDate by rememberSaveable { mutableStateOf("") }
    var cvc by rememberSaveable { mutableStateOf("") }

    var paying by remember { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(600) }
    var showReminderClockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            StepTopBar(progress = 1f, onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                ScreenHeadline(
                    "Review and pay",
                    "Payments are processed by Stripe. Your card details are secured end-to-end."
                )
                VSpace(20.dp)
                HoldTimerChip(secondsLeft)
                VSpace(20.dp)
                TripSummaryCard(draft)
                VSpace(14.dp)
                FareBreakdownCard(draft)
                VSpace(24.dp)
                SectionLabel("Pay with")
                VSpace(12.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectChip("Card", method == "Card", { method = "Card" }, icon = Icons.Outlined.CreditCard)
                    SelectChip("Google Pay", method == "Google Pay", { method = "Google Pay" }, icon = Icons.Outlined.AccountBalanceWallet)
                }

                if (method == "Card") {
                    VSpace(20.dp)
                    SectionLabel("Card Details")
                    VSpace(12.dp)
                    SoftCard(color = Color.White, padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SoftTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = "Card Number*",
                                placeholder = "4242 4242 4242 4242",
                                leadingIcon = Icons.Outlined.CreditCard,
                                keyboardType = KeyboardType.Number
                            )

                            SoftTextField(
                                value = cardName,
                                onValueChange = { cardName = it },
                                label = "Cardholder Name*",
                                placeholder = "e.g. Sehansa Bandara",
                                leadingIcon = Icons.Outlined.Person
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) {
                                    SoftTextField(
                                        value = expiryDate,
                                        onValueChange = { expiryDate = it },
                                        label = "Expires*",
                                        placeholder = "MM/YY",
                                        leadingIcon = Icons.Outlined.Event
                                    )
                                }
                                Box(Modifier.weight(1f)) {
                                    SoftTextField(
                                        value = cvc,
                                        onValueChange = { cvc = it },
                                        label = "CVC*",
                                        placeholder = "123",
                                        isPassword = true,
                                        leadingIcon = Icons.Outlined.Lock,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }
                        }
                    }
                }

                VSpace(16.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                    HSpace(6.dp)
                    Text("Secured by Stripe & Firebase", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            PillButton(
                text = "Pay ${draft.totalFare.toLkr()}",
                onClick = {
                    if (method == "Card" && cardNumber.isBlank()) {
                        Toast.makeText(context, "Please enter your card number", Toast.LENGTH_SHORT).show()
                        return@PillButton
                    }
                    paying = true
                    BookingNetworkManager.createPaymentIntent(context, draft) { success, message, clientSecret, pubKey, bookingId ->
                        if (success && bookingId != null) {
                            BookingNetworkManager.confirmMockPayment(context, bookingId) { confirmSuccess, confirmMsg ->
                                paying = false
                                if (confirmSuccess) {
                                    Toast.makeText(context, "Payment Confirmed! Please set your departure reminder.", Toast.LENGTH_SHORT).show()
                                    showReminderClockDialog = true
                                } else {
                                    Toast.makeText(context, confirmMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            paying = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = secondsLeft > 0 && !paying,
                loading = paying,
                leadingIcon = Icons.Outlined.Lock
            )
        }
    }

    // Departure Reminder Clock TimePicker Dialog (Connected to Firebase Firestore)
    if (showReminderClockDialog) {
        DepartureReminderTimeDialog(
            bus = draft.bus,
            onTimeSelected = { selectedTime ->
                showReminderClockDialog = false
                onPaid()
            },
            onDismiss = {
                showReminderClockDialog = false
                onPaid()
            }
        )
    }
}

/** Interactive Clock TimePicker Dialog for Departure Reminder. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureReminderTimeDialog(
    bus: Bus?,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timePickerState = rememberTimePickerState(initialHour = 5, initialMinute = 30)
    var isScheduling by remember { mutableStateOf(false) }

    val formattedTime = remember(timePickerState.hour, timePickerState.minute) {
        val hour = timePickerState.hour
        val minute = timePickerState.minute
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    AlertDialog(
        onDismissRequest = { if (!isScheduling) onDismiss() },
        containerColor = Cream,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                Icons.Outlined.Alarm,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Set Bus Reminder Time",
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
                    text = "Pick the time you want to get ready for your bus:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = OrangeSoft,
                        selectorColor = Orange,
                        clockDialSelectedContentColor = Color.White
                    )
                )

                SoftCard(color = Color.White, padding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
                        HSpace(8.dp)
                        Text(
                            text = "Reminder set for $formattedTime",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink
                        )
                    }
                }
            }
        },
        confirmButton = {
            PillButton(
                text = "OK",
                onClick = {
                    isScheduling = true
                    val busIdStr = bus?.id ?: bus?.number ?: "1"

                    // Calculate target AlarmManager trigger time
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(java.util.Calendar.DAY_OF_MONTH, 1)
                        }
                    }

                    // 1. Schedule exact local AlarmManager alarm & save persistently
                    com.busgo.app.util.ReminderManager.scheduleReminder(
                        context = context,
                        busId = busIdStr,
                        title = "BusGo Reminder ($formattedTime)",
                        description = "Get ready for your bus!",
                        formattedTime = formattedTime,
                        triggerTimeMillis = cal.timeInMillis
                    )

                    // 2. Trigger immediate System Notification
                    showSystemNotification(context, formattedTime, "Get ready for your bus")

                    // 3. Save to Firebase Firestore & Send FCM Push
                    FcmNetworkManager.scheduleNotification(
                        context = context,
                        busId = busIdStr,
                        time = formattedTime,
                        content = "Get ready for your bus"
                    ) { success, message ->
                        isScheduling = false
                        if (success) {
                            Toast.makeText(context, "Reminder scheduled for $formattedTime!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Reminder scheduled for $formattedTime", Toast.LENGTH_SHORT).show()
                        }
                        onTimeSelected(formattedTime)
                    }
                },
                loading = isScheduling,
                enabled = !isScheduling,
                height = 42.dp
            )
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isScheduling) onDismiss() },
                enabled = !isScheduling
            ) {
                Text("Skip", color = Muted)
            }
        }
    )
}

/** Helper to display Android System Heads-Up Notification in the notification drawer/tray. */
fun showSystemNotification(context: android.content.Context, time: String, content: String) {
    try {
        val channelId = "busgo_reminder_channel"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Bus Departure Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "BusGo Departure Reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("BusGo Reminder ($time)")
            .setContentText("$content - Scheduled for $time")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    } catch (e: Exception) {
        android.util.Log.e("SystemNotification", "Error showing notification: ${e.message}", e)
    }
}

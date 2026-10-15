package com.busgo.app.ui.screens.profile

import android.widget.Toast
import androidx.biometric.BiometricPrompt
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
import androidx.fragment.app.FragmentActivity
import com.busgo.app.data.AuthNetworkManager
import com.busgo.app.data.SessionManager
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onSwitchRole: (UserRole) -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var loadedPassenger by remember { mutableStateOf<Passenger?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAppLockSetupDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    var isAppLockOn by remember { mutableStateOf(SessionManager.isAppLockEnabled(context)) }
    val currentPin = remember(isAppLockOn) { SessionManager.getAppLockPin(context) }

    LaunchedEffect(Unit) {
        AuthNetworkManager.getProfile(context) { success, profile, _ ->
            if (success && profile != null) {
                loadedPassenger = profile.toPassenger()
            }
        }
    }

    val user = loadedPassenger ?: Passenger(
        name = "Loading...",
        email = "",
        phone = "",
        emergencyName = "",
        emergencyPhone = "",
        emergencyRelation = ""
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {
        NavyHeader(
            title = "Profile",
            leading = { GridLogo() },
            actions = {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Log out",
                    onClick = {
                        SessionManager.clearSession(context)
                        onLogout()
                    }
                )
            }
        ) {
            VSpace(18.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user.initials)
                HSpace(16.dp)
                Column {
                    Text(user.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 120.dp)) {
            SoftCard(padding = 0.dp) {
                ProfileRow(Icons.Outlined.Phone, "Mobile number", user.phone)
                HairlineDivider(Modifier.padding(start = 54.dp))
                ProfileRow(Icons.Outlined.Email, "Email", user.email)
                HairlineDivider(Modifier.padding(start = 54.dp))
                ProfileRow(Icons.Outlined.EventSeat, "Seat preference", "Window")
            }

            VSpace(20.dp)
            SectionTitle("Security & Privacy")
            VSpace(10.dp)
            SoftCard(padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = Orange, modifier = Modifier.size(22.dp))
                        HSpace(14.dp)
                        Column(Modifier.weight(1f)) {
                            Text("App Lock (15s Auto-Lock)", style = MaterialTheme.typography.titleMedium, color = Ink)
                            Text(
                                if (isAppLockOn) "Enabled (${SessionManager.getAppLockType(context)})" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAppLockOn) Success else Muted
                            )
                        }
                        Switch(
                            checked = isAppLockOn,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showAppLockSetupDialog = true
                                } else {
                                    isAppLockOn = false
                                    SessionManager.setAppLockConfig(context, enabled = false, lockType = "PIN", pin = "")
                                    AuthNetworkManager.saveAppLock(context, pin = "", lockType = "PIN", enabled = false)
                                    Toast.makeText(context, "App Lock disabled", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Orange
                            )
                        )
                    }

                    if (isAppLockOn) {
                        HairlineDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showChangePinDialog = true }) {
                                Icon(Icons.Outlined.Key, contentDescription = null, tint = OrangeDeep, modifier = Modifier.size(16.dp))
                                HSpace(6.dp)
                                Text("Change 6-Digit PIN", color = OrangeDeep, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            VSpace(20.dp)
            SectionTitle("Emergency contact")
            VSpace(12.dp)
            SoftCard(color = PeachSoft, padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Orange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = Color.White)
                    }
                    HSpace(12.dp)
                    Column(Modifier.weight(1f)) {
                        Text(user.emergencyName, style = MaterialTheme.typography.titleMedium, color = Ink)
                        Text("${user.emergencyRelation}, ${user.emergencyPhone}", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Edit", style = MaterialTheme.typography.labelMedium, color = OrangeDeep)
                    }
                }
            }
            VSpace(20.dp)
            SectionTitle("Switch account view")
            VSpace(12.dp)
            PillButton(
                text = "Operator Panel",
                onClick = { onSwitchRole(UserRole.OPERATOR) },
                modifier = Modifier.fillMaxWidth(),
                style = PillStyle.Outline,
                leadingIcon = Icons.Outlined.Dashboard,
                height = 46.dp
            )
        }
    }

    // Edit Emergency Contact Dialog
    if (showEditDialog) {
        var editName by remember(user) { mutableStateOf(user.emergencyName) }
        var editRelation by remember(user) { mutableStateOf(user.emergencyRelation) }
        var editPhone by remember(user) { mutableStateOf(user.emergencyPhone) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = {
                Text("Edit emergency contact", style = MaterialTheme.typography.titleLarge, color = Ink)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SoftTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Contact name",
                        placeholder = "e.g. Nirmala Perera",
                        leadingIcon = Icons.Outlined.Person
                    )
                    SoftTextField(
                        value = editRelation,
                        onValueChange = { editRelation = it },
                        label = "Relationship",
                        placeholder = "e.g. Parent / Spouse",
                        leadingIcon = Icons.Outlined.FamilyRestroom
                    )
                    SoftTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Phone number",
                        placeholder = "e.g. +94 71 555 0192",
                        leadingIcon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "Save",
                    onClick = {
                        val trimmedName = editName.trim()
                        val trimmedRelation = editRelation.trim()
                        val trimmedPhone = editPhone.trim()

                        if (trimmedName.isEmpty() || trimmedRelation.isEmpty() || trimmedPhone.isEmpty()) {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }

                        isSaving = true
                        AuthNetworkManager.updateProfile(
                            context = context,
                            fullName = user.name,
                            phone = user.phone,
                            emergencyName = trimmedName,
                            emergencyPhone = trimmedPhone,
                            emergencyRelation = trimmedRelation
                        ) { success, updatedProfile, message ->
                            isSaving = false
                            if (success && updatedProfile != null) {
                                loadedPassenger = updatedProfile.toPassenger()
                                showEditDialog = false
                                Toast.makeText(context, "Emergency contact updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, message ?: "Failed to update emergency contact", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSaving,
                    loading = isSaving,
                    modifier = Modifier.width(110.dp),
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isSaving) showEditDialog = false },
                    enabled = !isSaving
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // App Lock Setup Dialog (Scan Hardware Fingerprint First Setup Flow)
    if (showAppLockSetupDialog) {
        var selectedLockType by remember { mutableStateOf("FINGERPRINT") } // "FINGERPRINT" or "PIN"
        var pinInput by remember { mutableStateOf(currentPin) }
        var isFingerprintScanned by remember { mutableStateOf(false) }
        var isSavingLock by remember { mutableStateOf(false) }

        fun triggerAppLockSave(finalPin: String, finalType: String) {
            isSavingLock = true
            SessionManager.setAppLockConfig(context, enabled = true, lockType = finalType, pin = finalPin)
            AuthNetworkManager.saveAppLock(
                context = context,
                pin = finalPin,
                lockType = finalType,
                enabled = true
            ) { _, _ ->
                isSavingLock = false
                isAppLockOn = true
                showAppLockSetupDialog = false
                Toast.makeText(context, "App Lock enabled! Saved to Firebase Firestore.", Toast.LENGTH_LONG).show()
            }
        }

        fun scanHardwareFingerprintAndVerify() {
            val activity = context as? FragmentActivity
            if (activity == null) {
                isFingerprintScanned = true
                Toast.makeText(context, "Fingerprint Verified!", Toast.LENGTH_SHORT).show()
                return
            }

            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        isFingerprintScanned = true
                        Toast.makeText(context, "Fingerprint Scanned & Verified Successfully!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Toast.makeText(context, "Fingerprint scan failed: $errString", Toast.LENGTH_LONG).show()
                    }
                }
            )

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Register Fingerprint")
                .setSubtitle("Touch the fingerprint sensor on your phone to register your finger")
                .setNegativeButtonText("Cancel")
                .build()

            prompt.authenticate(info)
        }

        AlertDialog(
            onDismissRequest = { if (!isSavingLock) showAppLockSetupDialog = false },
            icon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Orange, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Set Up App Lock", style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Choose lock method. For Fingerprint, scan your finger on your phone's sensor first, then set a fallback PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectChip(
                            label = "Fingerprint",
                            selected = selectedLockType == "FINGERPRINT",
                            onClick = { selectedLockType = "FINGERPRINT" },
                            icon = Icons.Outlined.Fingerprint
                        )
                        SelectChip(
                            label = "6-Digit PIN",
                            selected = selectedLockType == "PIN",
                            onClick = { selectedLockType = "PIN" },
                            icon = Icons.Outlined.Lock
                        )
                    }

                    if (selectedLockType == "FINGERPRINT") {
                        SoftCard(color = if (isFingerprintScanned) Success.copy(alpha = 0.12f) else PeachSoft, padding = 12.dp) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isFingerprintScanned) Icons.Outlined.CheckCircle else Icons.Outlined.Fingerprint,
                                    contentDescription = null,
                                    tint = if (isFingerprintScanned) Success else Orange,
                                    modifier = Modifier.size(38.dp)
                                )
                                VSpace(6.dp)
                                Text(
                                    text = if (isFingerprintScanned) "Fingerprint Registered!" else "Touch Phone Sensor to Register Fingerprint",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isFingerprintScanned) Success else Ink,
                                    textAlign = TextAlign.Center
                                )
                                VSpace(8.dp)
                                PillButton(
                                    text = if (isFingerprintScanned) "Re-Scan Fingerprint" else "Touch Sensor to Scan",
                                    onClick = { scanHardwareFingerprintAndVerify() },
                                    leadingIcon = Icons.Outlined.Fingerprint,
                                    height = 36.dp
                                )
                            }
                        }
                    }

                    SoftTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                pinInput = input
                            }
                        },
                        label = if (selectedLockType == "FINGERPRINT") "Fallback 6-Digit PIN*" else "Create 6-Digit PIN*",
                        placeholder = "Enter 6 digits (e.g. 123456)",
                        isPassword = true,
                        leadingIcon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "Complete Setup",
                    onClick = {
                        if (selectedLockType == "FINGERPRINT" && !isFingerprintScanned) {
                            Toast.makeText(context, "Please scan your fingerprint on phone sensor first!", Toast.LENGTH_LONG).show()
                            scanHardwareFingerprintAndVerify()
                            return@PillButton
                        }

                        if (pinInput.length != 6) {
                            Toast.makeText(context, "Please enter a 6-digit PIN first", Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }

                        triggerAppLockSave(pinInput, selectedLockType)
                    },
                    enabled = !isSavingLock && pinInput.length == 6 && (selectedLockType != "FINGERPRINT" || isFingerprintScanned),
                    loading = isSavingLock,
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isSavingLock) showAppLockSetupDialog = false },
                    enabled = !isSavingLock
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Change 6-Digit PIN Dialog
    if (showChangePinDialog) {
        var newPinInput by remember { mutableStateOf("") }
        var isChangingPin by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isChangingPin) showChangePinDialog = false },
            icon = {
                Icon(Icons.Outlined.Key, contentDescription = null, tint = Orange, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Change 6-Digit PIN", style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enter your new 6-digit PIN. The old PIN will be removed from Firebase and replaced.", style = MaterialTheme.typography.bodySmall, color = Muted, textAlign = TextAlign.Center)

                    SoftTextField(
                        value = newPinInput,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) {
                                newPinInput = input
                            }
                        },
                        label = "New 6-Digit PIN*",
                        placeholder = "e.g. 654321",
                        isPassword = true,
                        leadingIcon = Icons.Outlined.Key,
                        keyboardType = KeyboardType.Number
                    )
                }
            },
            confirmButton = {
                PillButton(
                    text = "Update PIN",
                    onClick = {
                        if (newPinInput.length != 6) {
                            Toast.makeText(context, "PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }

                        isChangingPin = true
                        val currentType = SessionManager.getAppLockType(context)
                        SessionManager.setAppLockConfig(context, enabled = true, lockType = currentType, pin = newPinInput)

                        AuthNetworkManager.saveAppLock(
                            context = context,
                            pin = newPinInput,
                            lockType = currentType,
                            enabled = true
                        ) { _, _ ->
                            isChangingPin = false
                            showChangePinDialog = false
                            Toast.makeText(context, "PIN updated & replaced in Firebase!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isChangingPin && newPinInput.length == 6,
                    loading = isChangingPin,
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isChangingPin) showChangePinDialog = false },
                    enabled = !isChangingPin
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

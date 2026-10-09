package com.busgo.app.ui.screens.profile

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
import com.busgo.app.data.AuthNetworkManager
import com.busgo.app.data.SessionManager
import com.busgo.app.data.mock.MockData
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

    LaunchedEffect(Unit) {
        AuthNetworkManager.getProfile(context) { success, profile, _ ->
            if (success && profile != null) {
                loadedPassenger = profile.toPassenger()
            }
        }
    }

    val user = loadedPassenger ?: MockData.passenger

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
    ) {
        NavyHeader(title = "Profile", leading = { GridLogo() }) {
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
            VSpace(24.dp)
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
            VSpace(24.dp)
            SectionTitle("Switch view (for demo)")
            VSpace(12.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("Bus", { onSwitchRole(UserRole.DRIVER) }, Modifier.weight(1f),
                    style = PillStyle.Outline, leadingIcon = Icons.Outlined.DirectionsBus, height = 46.dp)
                PillButton("Operator", { onSwitchRole(UserRole.OPERATOR) }, Modifier.weight(1f),
                    style = PillStyle.Outline, leadingIcon = Icons.Outlined.Dashboard, height = 46.dp)
            }
            VSpace(24.dp)
            PillButton("LOG OUT", onClick = {
                SessionManager.clearSession(context)
                onLogout()
            }, style = PillStyle.Dark, leadingIcon = Icons.AutoMirrored.Outlined.Logout)
        }
    }

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

                        if (trimmedName.isEmpty()) {
                            android.widget.Toast.makeText(context, "Contact name cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }
                        if (trimmedRelation.isEmpty()) {
                            android.widget.Toast.makeText(context, "Relationship cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }
                        if (trimmedPhone.isEmpty()) {
                            android.widget.Toast.makeText(context, "Phone number cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
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
                                android.widget.Toast.makeText(context, "Emergency contact updated!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, message ?: "Failed to update emergency contact", android.widget.Toast.LENGTH_LONG).show()
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
}

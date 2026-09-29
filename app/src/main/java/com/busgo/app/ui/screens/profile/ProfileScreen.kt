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
fun ProfileScreen(onSwitchRole: (UserRole) -> Unit, onLogout: () -> Unit) {
    val user = MockData.passenger
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
                    TextButton(onClick = { }) { Text("Edit", style = MaterialTheme.typography.labelMedium, color = OrangeDeep) }
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
            PillButton("LOG OUT", onLogout, style = PillStyle.Dark, leadingIcon = Icons.AutoMirrored.Outlined.Logout)
        }
    }
}

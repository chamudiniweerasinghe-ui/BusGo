package com.busgo.app.ui.screens.auth

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (UserRole) -> Unit,
    onCreateAccount: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.PASSENGER) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            BackTopBar(onBack = onBack, title = "Log in")
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                VSpace(16.dp)
                ScreenHeadline(
                    "Good to see you again",
                    "Log in to track your bus, manage your tickets and stay safe on the road."
                )
                VSpace(32.dp)
                SoftTextField(
                    email, { email = it }, "Email",
                    placeholder = "you@example.com",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email
                )
                VSpace(16.dp)
                SoftTextField(
                    password, { password = it }, "Password",
                    placeholder = "Your password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true
                )
                TextButton(onClick = { }, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot password?", style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
                HairlineDivider(Modifier.padding(vertical = 12.dp))
                SectionLabel("I'm logging in as")
                VSpace(12.dp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(UserRole.PASSENGER, UserRole.DRIVER).forEach { r ->
                        SelectChip(r.label, role == r, { role = r }, icon = roleIcon(r))
                    }
                }
                VSpace(24.dp)
            }
            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                PillButton(
                    "Log in",
                    onClick = {
                        loading = true
                        scope.launch {
                            delay(900) // fake network call
                            loading = false
                            onLogin(role)
                        }
                    },
                    loading = loading
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New to BusGo?", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    TextButton(onClick = onCreateAccount) {
                        Text("Create account", style = MaterialTheme.typography.labelMedium, color = Ink)
                    }
                }
            }
        }
    }
}

private fun roleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.PASSENGER -> Icons.Outlined.Person
    UserRole.DRIVER -> Icons.Outlined.DirectionsBus
    UserRole.OPERATOR -> Icons.Outlined.Dashboard
    UserRole.ADMIN -> Icons.Outlined.Security
}

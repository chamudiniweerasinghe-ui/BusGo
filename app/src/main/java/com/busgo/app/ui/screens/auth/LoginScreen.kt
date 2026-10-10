package com.busgo.app.ui.screens.auth

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
import com.busgo.app.data.AuthNetworkManager
import com.busgo.app.data.mock.MockData
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
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
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.PASSENGER) }
    var loading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

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
                TextButton(
                    onClick = { showForgotPasswordDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
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
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            return@PillButton
                        }
                        loading = true
                        AuthNetworkManager.login(context, email, password) { success, message, returnedRole ->
                            loading = false
                            if (success) {
                                val targetRole = when (returnedRole?.lowercase()?.trim()) {
                                    "driver" -> UserRole.DRIVER
                                    "admin" -> UserRole.ADMIN
                                    "operator" -> UserRole.OPERATOR
                                    else -> role
                                }
                                onLogin(targetRole)
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
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

    if (showForgotPasswordDialog) {
        var resetStep by remember { mutableIntStateOf(1) }
        var resetEmail by remember(email) { mutableStateOf(email) }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isResetting) showForgotPasswordDialog = false },
            title = {
                Text(
                    if (resetStep == 1) "Reset Password" else "Enter New Password",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (resetStep == 1) {
                        Text(
                            "Enter the email associated with your BusGo account to reset your password.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        SoftTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = "Email address",
                            placeholder = "you@example.com",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email
                        )
                    } else {
                        Text(
                            "Create a new secure password for $resetEmail.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        SoftTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "New Password",
                            placeholder = "Min 6 characters",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true
                        )
                        SoftTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirm New Password",
                            placeholder = "Re-enter new password",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true
                        )
                    }
                }
            },
            confirmButton = {
                PillButton(
                    text = if (resetStep == 1) "Next" else "Reset Password",
                    onClick = {
                        if (resetStep == 1) {
                            if (resetEmail.isBlank()) {
                                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
                                return@PillButton
                            }
                            isResetting = true
                            AuthNetworkManager.forgotPassword(context, resetEmail) { success, message, _ ->
                                isResetting = false
                                if (success) {
                                    resetStep = 2
                                    Toast.makeText(context, "Email verified. Enter your new password.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            if (newPassword.length < 6) {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@PillButton
                            }
                            if (newPassword != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@PillButton
                            }
                            isResetting = true
                            AuthNetworkManager.resetPassword(context, resetEmail, newPassword) { success, message ->
                                isResetting = false
                                if (success) {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    email = resetEmail
                                    showForgotPasswordDialog = false
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isResetting,
                    loading = isResetting,
                    modifier = Modifier.width(140.dp),
                    height = 42.dp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isResetting) showForgotPasswordDialog = false },
                    enabled = !isResetting
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private fun roleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.PASSENGER -> Icons.Outlined.Person
    UserRole.DRIVER -> Icons.Outlined.DirectionsBus
    UserRole.OPERATOR -> Icons.Outlined.Dashboard
    UserRole.ADMIN -> Icons.Outlined.Security
}

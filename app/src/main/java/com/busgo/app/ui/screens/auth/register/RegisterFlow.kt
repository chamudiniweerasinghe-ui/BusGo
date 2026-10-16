package com.busgo.app.ui.screens.auth.register

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import com.busgo.app.data.model.*
import com.busgo.app.ui.components.*
import com.busgo.app.ui.components.map.*
import com.busgo.app.ui.theme.*
import com.busgo.app.util.*
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class RegisterStep { TYPE, ACCOUNT, EMERGENCY, PREFERENCES, BUS_DETAILS }

/**
 * Sign-up in the style of the reference onboarding with Email OTP Verification.
 */
@Composable
fun RegisterFlow(
    onBack: () -> Unit,
    onPassengerRegistered: () -> Unit,
    onBusRegistered: (Bus) -> Unit
) {
    val form = remember { RegisterFormState() }
    var index by rememberSaveable { mutableIntStateOf(0) }

    val steps = when (form.accountType) {
        AccountType.BUS -> listOf(RegisterStep.TYPE, RegisterStep.ACCOUNT, RegisterStep.BUS_DETAILS)
        else -> listOf(RegisterStep.TYPE, RegisterStep.ACCOUNT, RegisterStep.EMERGENCY, RegisterStep.PREFERENCES)
    }
    val current = steps[index.coerceIn(0, steps.lastIndex)]
    val isLast = index == steps.lastIndex

    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var isResendingOtp by remember { mutableStateOf(false) }
    var showOtpScreen by rememberSaveable { mutableStateOf(false) }

    fun finish() {
        if (form.accountType == AccountType.BUS) onBusRegistered(form.toBus()) else onPassengerRegistered()
    }

    fun handleRequestOtp() {
        loading = true
        val role = if (form.accountType == AccountType.BUS) "driver" else "passenger"

        AuthNetworkManager.requestSignupOtp(
            context = context,
            fullName = form.fullName,
            email = form.email,
            phone = form.phone,
            password = form.password,
            role = role,
            emergencyName = form.contactName,
            emergencyPhone = form.contactPhone
        ) { success, message ->
            loading = false
            if (success) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                showOtpScreen = true
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler(enabled = showOtpScreen || index > 0) {
        if (showOtpScreen) {
            showOtpScreen = false
        } else {
            index--
        }
    }

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            StepTopBar(
                progress = if (showOtpScreen) 1f else (index + 1) / steps.size.toFloat(),
                onBack = {
                    if (showOtpScreen) {
                        showOtpScreen = false
                    } else if (index == 0) {
                        onBack()
                    } else {
                        index--
                    }
                },
                onSkip = if (!showOtpScreen && current == RegisterStep.PREFERENCES) ({ handleRequestOtp() }) else null
            )

            if (showOtpScreen) {
                // Email OTP Verification View
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    OtpVerificationStep(
                        email = form.email,
                        onVerify = { otp ->
                            isVerifyingOtp = true
                            AuthNetworkManager.verifySignupOtp(context, form.email, otp) { success, msg, _ ->
                                isVerifyingOtp = false
                                if (success) {
                                    Toast.makeText(context, "Email verified & account created!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onResend = {
                            isResendingOtp = true
                            AuthNetworkManager.resendSignupOtp(context, form.email) { success, msg, _ ->
                                isResendingOtp = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        isVerifying = isVerifyingOtp,
                        isResending = isResendingOtp
                    )
                }
            } else {
                // Regular Registration Flow Steps
                AnimatedContent(
                    targetState = index,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally { it * dir / 4 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it * dir / 4 } + fadeOut())
                    },
                    modifier = Modifier.weight(1f),
                    label = "register-step"
                ) { i ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        when (steps[i.coerceIn(0, steps.lastIndex)]) {
                            RegisterStep.TYPE -> AccountTypeStep(form)
                            RegisterStep.ACCOUNT -> AccountStep(form)
                            RegisterStep.EMERGENCY -> EmergencyContactStep(form)
                            RegisterStep.PREFERENCES -> PreferencesStep(form)
                            RegisterStep.BUS_DETAILS -> BusDetailsStep(form)
                        }
                    }
                }
                val canContinue = when (current) {
                    RegisterStep.TYPE -> form.accountType != null
                    RegisterStep.ACCOUNT -> form.accountValid
                    RegisterStep.EMERGENCY -> form.contactValid
                    RegisterStep.PREFERENCES -> true
                    RegisterStep.BUS_DETAILS -> form.busValid
                }
                PillButton(
                    text = when {
                        !isLast -> "Continue"
                        form.accountType == AccountType.BUS -> "Register bus"
                        else -> "Create account"
                    },
                    onClick = { if (isLast) handleRequestOtp() else index++ },
                    enabled = canContinue && !loading,
                    loading = loading,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun OtpVerificationStep(
    email: String,
    onVerify: (String) -> Unit,
    onResend: () -> Unit,
    isVerifying: Boolean,
    isResending: Boolean
) {
    var otpInput by rememberSaveable { mutableStateOf("") }
    var cooldownSeconds by rememberSaveable { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (cooldownSeconds > 0) {
            delay(1000)
            cooldownSeconds--
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenHeadline(
            "Verify Your Email",
            "We sent a 6-digit verification code to $email. Please enter it below to complete signup."
        )

        SoftCard(padding = 20.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    tint = Orange,
                    modifier = Modifier.size(48.dp)
                )

                SoftTextField(
                    value = otpInput,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            otpInput = input
                        }
                    },
                    label = "6-Digit Verification Code",
                    placeholder = "e.g. 123456",
                    leadingIcon = Icons.Outlined.Lock,
                    keyboardType = KeyboardType.Number
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cooldownSeconds > 0) "Resend code in ${cooldownSeconds}s" else "Didn't receive code?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )

                    TextButton(
                        onClick = {
                            onResend()
                            cooldownSeconds = 60
                        },
                        enabled = cooldownSeconds == 0 && !isResending
                    ) {
                        Text(
                            text = if (isResending) "Resending..." else "Resend OTP",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (cooldownSeconds == 0) OrangeDeep else Muted
                        )
                    }
                }
            }
        }

        PillButton(
            text = "Verify & Complete Signup",
            onClick = { onVerify(otpInput) },
            enabled = otpInput.length == 6 && !isVerifying,
            loading = isVerifying,
            modifier = Modifier.fillMaxWidth(),
            height = 46.dp
        )
    }
}

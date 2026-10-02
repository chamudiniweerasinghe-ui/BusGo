package com.busgo.app.ui.screens.auth.register

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

private enum class RegisterStep { TYPE, ACCOUNT, EMERGENCY, PREFERENCES, BUS_DETAILS }

/**
 * Sign-up in the style of the reference onboarding.
 * Passenger: type > account > emergency contact > travel preferences.
 * Bus:       type > owner/driver account > bus details (number, from, to...).
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

    val context = androidx.compose.ui.platform.LocalContext.current
    var loading by remember { mutableStateOf(false) }

    fun finish() {
        if (form.accountType == AccountType.BUS) onBusRegistered(form.toBus()) else onPassengerRegistered()
    }

    fun handleFinish() {
        loading = true
        val role = if (form.accountType == AccountType.BUS) "driver" else "passenger"
        com.busgo.app.data.AuthNetworkManager.register(
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
                android.widget.Toast.makeText(context, "Registration successful!", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            } else {
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    androidx.activity.compose.BackHandler(enabled = index > 0) { index-- }

    GradientBackground(BusGoGradients.WarmTop) {
        Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            StepTopBar(
                progress = (index + 1) / steps.size.toFloat(),
                onBack = { if (index == 0) onBack() else index-- },
                onSkip = if (current == RegisterStep.PREFERENCES) ({ handleFinish() }) else null
            )
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
                onClick = { if (isLast) handleFinish() else index++ },
                enabled = canContinue && !loading,
                loading = loading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

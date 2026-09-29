package com.busgo.app.ui.screens.payment

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

/**
 * Step 4 — review and pay.
 * In the real app the "Pay" button opens Stripe's PaymentSheet so card
 * details are handled entirely by Stripe (see NFR: Security).
 */
@Composable
fun PaymentScreen(
    draft: BookingDraft,
    onBack: () -> Unit,
    onPaid: () -> Unit
) {
    var method by rememberSaveable { mutableStateOf("Card") }
    var paying by remember { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(600) }
    val scope = rememberCoroutineScope()

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
                    "Payments are processed by Stripe. Your card details never reach our servers."
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
                VSpace(16.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
                    HSpace(6.dp)
                    Text("Secured by Stripe", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            PillButton(
                text = "Pay ${draft.totalFare.toLkr()}",
                onClick = {
                    paying = true
                    scope.launch {
                        delay(1600) // replace with Stripe PaymentSheet result
                        paying = false
                        onPaid()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                enabled = secondsLeft > 0,
                loading = paying,
                leadingIcon = Icons.Outlined.Lock
            )
        }
    }
}

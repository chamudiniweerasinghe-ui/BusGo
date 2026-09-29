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

/** First sign-up step: two square tiles side by side — Passenger or Bus. */
@Composable
fun AccountTypeStep(form: RegisterFormState) {
    Column {
        ScreenHeadline(
            "How will you use BusGo?",
            "Pick one to continue. Each account type gets its own screens."
        )
        VSpace(28.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AccountTypeTile(
                icon = Icons.Outlined.Person,
                title = "Passenger",
                subtitle = "Search, book & pay",
                selected = form.accountType == AccountType.PASSENGER,
                onClick = { form.accountType = AccountType.PASSENGER },
                modifier = Modifier.weight(1f)
            )
            AccountTypeTile(
                icon = Icons.Filled.DirectionsBus,
                title = "Bus",
                subtitle = "Register & go live",
                selected = form.accountType == AccountType.BUS,
                onClick = { form.accountType = AccountType.BUS },
                modifier = Modifier.weight(1f)
            )
        }
        VSpace(24.dp)
        AnimatedContent(targetState = form.accountType, label = "type-info") { type ->
            when (type) {
                null -> Text(
                    "Tap a tile to see what you can do.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
                AccountType.PASSENGER -> FeatureList(
                    "As a passenger you can",
                    listOf(
                        Icons.Outlined.Search to "Find buses between any two towns",
                        Icons.Outlined.MyLocation to "Watch your bus move live on the map",
                        Icons.Outlined.EventSeat to "Pick your seats and pay securely",
                        Icons.Outlined.ConfirmationNumber to "Board with a QR e-ticket"
                    )
                )
                AccountType.BUS -> FeatureList(
                    "As a bus you can",
                    listOf(
                        Icons.Outlined.DirectionsBus to "Register your bus number and route",
                        Icons.Outlined.GpsFixed to "Share your live location with passengers",
                        Icons.Outlined.QrCodeScanner to "Scan and check passenger tickets",
                        Icons.Outlined.Warning to "Send an emergency alert in one tap"
                    )
                )
            }
        }
    }
}

@Composable
private fun AccountTypeTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val bg by animateColorAsState(if (selected) Navy else Color.White, label = "tile-bg")
    val scale by animateFloatAsState(if (selected) 1.03f else 1f, label = "tile-scale")
    // "live" icon: the selected tile's icon keeps gently bouncing
    val bounce by rememberInfiniteTransition(label = "tile").animateFloat(
        0f, 1f, infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bounce"
    )

    Box(
        modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 14.dp else 2.dp, shape, spotColor = Navy)
            .clip(shape)
            .background(bg)
            .then(if (!selected) Modifier.border(1.dp, Hairline, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .offset(y = if (selected) (-5).dp * bounce else 0.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) Orange else OrangeSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else Orange, modifier = Modifier.size(30.dp))
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) Orange else Color.Transparent)
                .then(if (!selected) Modifier.border(2.dp, Hairline, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = if (selected) Color.White else Ink)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.7f) else Muted
            )
        }
    }
}

@Composable
private fun FeatureList(title: String, items: List<Pair<ImageVector, String>>) {
    SoftCard(padding = 18.dp) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = Muted)
        VSpace(6.dp)
        items.forEach { (icon, text) ->
            Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(OrangeSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                }
                HSpace(12.dp)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }
    }
}

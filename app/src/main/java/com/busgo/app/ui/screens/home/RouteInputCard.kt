package com.busgo.app.ui.screens.home

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

/** "From / To" card with a round swap button, like a ticket-booking search box. */
@Composable
fun RouteInputCard(
    from: String,
    to: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        SoftCard(padding = 0.dp) {
            RouteField("From", from, onFromChange, Icons.Outlined.TripOrigin)
            HairlineDivider(Modifier.padding(start = 60.dp, end = 76.dp))
            RouteField("To", to, onToChange, Icons.Outlined.LocationOn)
        }
        Surface(
            onClick = onSwap,
            shape = CircleShape,
            color = Orange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.SwapVert, contentDescription = "Swap", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun RouteField(label: String, value: String, onChange: (String) -> Unit, icon: ImageVector) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).background(ChipSelected, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Ink)
        }
        HSpace(12.dp)
        Column(Modifier.weight(1f).padding(end = 56.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
            Box {
                if (value.isEmpty()) {
                    Text("Choose a town", style = MaterialTheme.typography.titleMedium, color = Muted.copy(alpha = 0.5f))
                }
                BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = Ink),
                cursorBrush = SolidColor(Rose),
                modifier = Modifier.fillMaxWidth()
            )
            }
        }
    }
}

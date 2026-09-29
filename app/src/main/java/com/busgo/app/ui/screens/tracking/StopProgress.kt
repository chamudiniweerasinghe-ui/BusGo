package com.busgo.app.ui.screens.tracking

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

/** Horizontal line of stops: where the bus is now and where the passenger gets on. */
@Composable
fun StopProgress(stops: List<Stop>, currentIndex: Int, userIndex: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            stops.forEachIndexed { i, _ ->
                val dot = when (i) {
                    currentIndex -> Modifier.size(12.dp).background(Rose, CircleShape)
                    userIndex -> Modifier.size(12.dp).background(Color.White, CircleShape).border(2.dp, Coral, CircleShape)
                    else -> Modifier.size(8.dp).background(if (i < currentIndex) Ink else Hairline, CircleShape)
                }
                Box(dot)
                if (i < stops.lastIndex) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (i < currentIndex) Ink else Hairline)
                    )
                }
            }
        }
        VSpace(8.dp)
        Row(Modifier.fillMaxWidth()) {
            Text(stops.first().name, style = MaterialTheme.typography.bodySmall, color = Muted)
            Spacer(Modifier.weight(1f))
            Text("Your stop: ${stops[userIndex].name}", style = MaterialTheme.typography.labelMedium, color = Coral)
            Spacer(Modifier.weight(1f))
            Text(stops.last().name, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

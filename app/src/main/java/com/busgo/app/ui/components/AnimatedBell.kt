package com.busgo.app.ui.components

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
import androidx.compose.animation.core.Animatable
import com.busgo.app.ui.components.map.*

/** Notification bell that rings every few seconds and shows a pulsing dot. */
@Composable
fun AnimatedBell(onClick: () -> Unit, modifier: Modifier = Modifier, hasNew: Boolean = true) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(hasNew) {
        while (hasNew) {
            delay(2800)
            for (angle in listOf(18f, -16f, 12f, -8f, 4f, 0f)) {
                rotation.animateTo(angle, tween(70))
            }
        }
    }
    Box(modifier) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.12f),
            contentColor = Color.White,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Alerts",
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            rotationZ = rotation.value
                            transformOrigin = TransformOrigin(0.5f, 0.1f)
                        }
                )
            }
        }
        if (hasNew) {
            LiveDot(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp), color = Orange)
        }
    }
}

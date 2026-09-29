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

enum class PillStyle { Primary, Dark, Light, Outline, Danger }

/** Full-width rounded pill button — orange by default, like the reference. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    style: PillStyle = PillStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    height: Dp = 56.dp
) {
    val container = when (style) {
        PillStyle.Primary -> Orange
        PillStyle.Dark -> Navy
        PillStyle.Light -> Color.White
        PillStyle.Outline -> Color.White
        PillStyle.Danger -> Danger
    }
    val content = when (style) {
        PillStyle.Primary, PillStyle.Dark, PillStyle.Danger -> Color.White
        PillStyle.Outline -> OrangeDeep
        else -> Ink
    }
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = CircleShape,
        color = if (enabled) container else container.copy(alpha = 0.3f),
        contentColor = content,
        border = if (style == PillStyle.Outline) BorderStroke(1.5.dp, Orange) else null,
        modifier = modifier.height(height)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = content, strokeWidth = 2.dp)
            } else {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    HSpace(8.dp)
                }
                Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}

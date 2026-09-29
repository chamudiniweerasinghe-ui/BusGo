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

/** Lavender → pink slider with a white thumb and a coloured dot (reference "movement level"). */
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    labels: List<String>,
    modifier: Modifier = Modifier,
    brush: Brush = BusGoGradients.SoftTrack,
    dotColor: Color = Rose
) {
    val onChange by rememberUpdatedState(onValueChange)
    val v = value.coerceIn(0f, 1f)
    Column(modifier.fillMaxWidth()) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onChange((it.x / size.width).coerceIn(0f, 1f)) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        onChange((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                }
        ) {
            val thumb = 22.dp
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Hairline.copy(alpha = 0.7f))
            )
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(v.coerceAtLeast(0.04f))
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (maxWidth - thumb) * v)
                    .size(thumb)
                    .shadow(3.dp, CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(8.dp).background(dotColor, CircleShape))
            }
        }
        VSpace(8.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

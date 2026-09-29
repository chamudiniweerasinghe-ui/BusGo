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

/**
 * Draws a QR-style code for the ticket.
 * NOTE: this is a visual placeholder only. For real scanning, generate the
 * matrix with ZXing (com.google.zxing:core) and draw it the same way.
 */
@Composable
fun QrCodeView(data: String, modifier: Modifier = Modifier, color: Color = Ink) {
    val cells = 25
    val matrix = remember(data) { buildQrMatrix(data, cells) }
    Canvas(modifier) {
        val cell = size.minDimension / cells
        for (y in 0 until cells) {
            for (x in 0 until cells) {
                if (matrix[y][x]) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x * cell, y * cell),
                        size = Size(cell, cell),
                        cornerRadius = CornerRadius(cell * 0.25f)
                    )
                }
            }
        }
    }
}

private fun buildQrMatrix(data: String, n: Int): Array<BooleanArray> {
    val rnd = kotlin.random.Random(data.hashCode())
    val m = Array(n) { BooleanArray(n) { rnd.nextFloat() < 0.47f } }
    fun finder(ox: Int, oy: Int) {
        for (y in -1..7) for (x in -1..7) {
            val xx = ox + x
            val yy = oy + y
            if (xx !in 0 until n || yy !in 0 until n) continue
            val edge = x in 0..6 && y in 0..6 && (x == 0 || x == 6 || y == 0 || y == 6)
            val core = x in 2..4 && y in 2..4
            m[yy][xx] = edge || core
        }
    }
    finder(0, 0)
    finder(n - 7, 0)
    finder(0, n - 7)
    return m
}

package com.busgo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.busgo.app.ui.theme.Ink
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Draws a real, standard-compliant, scannable QR code for the ticket.
 */
@Composable
fun QrCodeView(data: String, modifier: Modifier = Modifier, color: Color = Ink) {
    val matrix = remember(data) { generateQrMatrix(data) }

    if (matrix != null && matrix.isNotEmpty()) {
        val matrixSize = matrix.size
        Canvas(modifier) {
            val cell = size.minDimension / matrixSize
            for (y in 0 until matrixSize) {
                for (x in 0 until matrixSize) {
                    if (matrix[y][x]) {
                        drawRect(
                            color = color,
                            topLeft = Offset(x * cell, y * cell),
                            size = Size(cell, cell)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(data: String): Array<BooleanArray>? {
    if (data.isBlank()) return null
    return try {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 0, 0, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val matrix = Array(height) { BooleanArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                matrix[y][x] = bitMatrix.get(x, y)
            }
        }
        matrix
    } catch (e: Exception) {
        null
    }
}

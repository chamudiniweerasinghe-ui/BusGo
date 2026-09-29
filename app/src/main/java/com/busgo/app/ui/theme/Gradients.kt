package com.busgo.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object BusGoGradients {
    val WarmTop = Brush.verticalGradient(listOf(Cream, Color(0xFFFBEEE4)))
    val BlushTop = Brush.verticalGradient(listOf(Cream, Color(0xFFFBE6E3)))
    val AlertTop = Brush.verticalGradient(listOf(Color(0xFFFBE7E3), Cream))
    val Header = Brush.verticalGradient(listOf(NavyDeep, Navy))
    val SoftTrack = Brush.horizontalGradient(listOf(Lavender, Peach, Orange))
    val DangerTrack = Brush.horizontalGradient(listOf(Color(0xFFF9D9A8), Orange, Danger))
    val Avatar = Brush.linearGradient(listOf(Orange, OrangeDeep))
}

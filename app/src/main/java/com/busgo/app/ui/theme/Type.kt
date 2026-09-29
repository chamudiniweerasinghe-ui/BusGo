package com.busgo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bold, rounded-looking sans headlines like the reference.
val DisplayFont = FontFamily.SansSerif
val SansBody = FontFamily.SansSerif

val BusGoTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontSize = 64.sp, lineHeight = 70.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontSize = 46.sp, lineHeight = 50.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontSize = 19.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = SansBody, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = SansBody, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = SansBody, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = SansBody, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = SansBody, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = SansBody, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
    labelMedium = TextStyle(fontFamily = SansBody, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontFamily = SansBody, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
)

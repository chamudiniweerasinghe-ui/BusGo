package com.busgo.app.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Double.toLkr(): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    return "LKR " + nf.format(this)
}

fun LocalDate.pretty(): String =
    format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH))

fun LocalDate.chipLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    else -> format(DateTimeFormatter.ofPattern("EEE d", Locale.ENGLISH))
}

fun formatCountdown(totalSeconds: Int): String =
    "%02d:%02d".format(Locale.US, totalSeconds / 60, totalSeconds % 60)

package com.busgo.app.ui.screens.auth.register

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
import com.busgo.app.ui.components.map.*

enum class AccountType { PASSENGER, BUS }

/** All fields for sign-up (both passenger and bus), kept in one place so steps stay simple. */
class RegisterFormState {
    var accountType by mutableStateOf<AccountType?>(null)

    // account (both roles)
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // passenger only
    var contactName by mutableStateOf("")
    var contactPhone by mutableStateOf("")
    var relationship by mutableStateOf("Parent")
    var travelTime by mutableFloatStateOf(0.3f)
    var busTypes by mutableStateOf(setOf("Normal"))
    var seatPreference by mutableStateOf("Window")
    var notifyAbout by mutableStateOf(setOf("Bus arriving", "Delays"))

    // bus only
    var busNumber by mutableStateOf("")
    var routeNo by mutableStateOf("")
    var busFrom by mutableStateOf("")
    var busTo by mutableStateOf("")
    var busCategory by mutableStateOf(BusCategory.NORMAL)
    var totalSeats by mutableIntStateOf(40)
    var firstDeparture by mutableStateOf("06:00")

    val accountValid: Boolean
        get() = fullName.isNotBlank() && phone.length >= 9 && email.contains("@") && password.length >= 6

    val contactValid: Boolean
        get() = contactName.isNotBlank() && contactPhone.length >= 9

    val busValid: Boolean
        get() = busNumber.trim().length >= 4 &&
            busFrom.isNotBlank() && busTo.isNotBlank() &&
            !busFrom.trim().equals(busTo.trim(), ignoreCase = true)

    /** The bus the owner just registered, shaped like the rest of the app's data. */
    fun toBus(): Bus = Bus(
        id = "my-bus",
        number = busNumber.trim().uppercase(),
        operator = fullName.ifBlank { "My bus" },
        routeNo = routeNo.trim(),
        category = busCategory,
        from = busFrom.trim(),
        to = busTo.trim(),
        departure = firstDeparture,
        arrival = "--",
        duration = "--",
        seatsLeft = totalSeats,
        totalSeats = totalSeats,
        ratePerKm = when (busCategory) {
            BusCategory.NORMAL -> 2.6
            BusCategory.LUXURY -> 4.2
            BusCategory.PRIVATE -> 3.4
        },
        routeKm = 0.0,
        etaMinutes = 0,
        nextStop = busFrom.trim(),
        speedKmh = 0
    )
}

fun Set<String>.toggle(item: String): Set<String> = if (item in this) this - item else this + item

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

@Composable
fun AccountStep(form: RegisterFormState) {
    val isBus = form.accountType == AccountType.BUS
    Column {
        ScreenHeadline(
            if (isBus) "Who's running this bus?" else "Let's set up your account",
            if (isBus) "The owner's or driver's details. We use them to verify the bus and reach you if something goes wrong."
            else "We'll use these details for your e-tickets and payment receipts."
        )
        VSpace(32.dp)
        SoftTextField(form.fullName, { form.fullName = it }, if (isBus) "Owner or driver name" else "Full name",
            placeholder = if (isBus) "Sunil Jayasinghe" else "Kasun Perera", leadingIcon = Icons.Outlined.Person)
        VSpace(16.dp)
        SoftTextField(form.phone, { form.phone = it }, "Mobile number",
            placeholder = "+94 77 123 4567", leadingIcon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone)
        VSpace(16.dp)
        SoftTextField(form.email, { form.email = it }, "Email",
            placeholder = "you@example.com", leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email)
        VSpace(16.dp)
        SoftTextField(form.password, { form.password = it }, "Password",
            placeholder = "At least 6 characters", leadingIcon = Icons.Outlined.Lock,
            isPassword = true)
    }
}

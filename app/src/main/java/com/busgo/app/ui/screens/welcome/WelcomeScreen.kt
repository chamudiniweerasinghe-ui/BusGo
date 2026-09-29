package com.busgo.app.ui.screens.welcome

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

import com.busgo.app.ui.components.illustration.BusScene

/** Navy welcome screen modelled on the reference "SCHOOL BUS" card, with a live bus scene. */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BusGoGradients.Header)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GridLogo(size = 30.dp, animated = true)
            VSpace(20.dp)
            Text("BUS GO", style = MaterialTheme.typography.displayMedium, color = Color.White, textAlign = TextAlign.Center)
            VSpace(12.dp)
            Text(
                "Track long-distance buses live,\nbook your seat and travel safely",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            VSpace(32.dp)
            PillButton("GET STARTED", onGetStarted)
            VSpace(14.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                Text(
                    " Log in",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Orange,
                    modifier = Modifier.clickable(onClick = onLogin).padding(6.dp)
                )
            }
        }
        BusScene(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            approach = 1f,
            showSky = false,
            headlights = false
        )
    }
}

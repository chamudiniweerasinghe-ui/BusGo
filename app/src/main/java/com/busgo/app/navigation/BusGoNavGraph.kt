package com.busgo.app.navigation

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

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.busgo.app.ui.screens.admin.AdminDashboardScreen
import com.busgo.app.ui.screens.alerts.AlertsScreen
import com.busgo.app.ui.screens.auth.LoginScreen
import com.busgo.app.ui.screens.auth.register.RegisterFlow
import com.busgo.app.ui.screens.booking.SeatCountScreen
import com.busgo.app.ui.screens.booking.SeatSelectionScreen
import com.busgo.app.ui.screens.booking.SegmentScreen
import com.busgo.app.ui.screens.driver.DriverScreen
import com.busgo.app.ui.screens.emergency.ReportIncidentScreen
import com.busgo.app.ui.screens.home.HomeScreen
import com.busgo.app.ui.screens.payment.PaymentScreen
import com.busgo.app.ui.screens.profile.ProfileScreen
import com.busgo.app.ui.screens.search.SearchResultsScreen
import com.busgo.app.ui.screens.ticket.TicketScreen
import com.busgo.app.ui.screens.tickets.MyTicketsScreen
import com.busgo.app.ui.screens.tracking.LiveTrackingScreen
import com.busgo.app.ui.screens.splash.SplashScreen
import com.busgo.app.ui.screens.welcome.WelcomeScreen

@Composable
fun BusGoNavGraph() {
    val nav = rememberNavController()
    var draft by remember { mutableStateOf(BookingDraft()) }
    val tickets = remember { mutableStateListOf<Ticket>().apply { addAll(MockData.sampleTickets) } }
    var myBus by remember { mutableStateOf<Bus?>(null) }
    var openTicketId by rememberSaveable { mutableStateOf<String?>(null) }
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val tabs = listOf(
        BottomTab(Routes.HOME, "Home", Icons.Outlined.Home),
        BottomTab(Routes.TICKETS, "Tickets", Icons.Outlined.ConfirmationNumber),
        BottomTab(Routes.ALERTS, "Alerts", Icons.Outlined.NotificationsNone),
        BottomTab(Routes.PROFILE, "Profile", Icons.Outlined.PersonOutline)
    )

    val pop: () -> Unit = { nav.popBackStack() }
    fun goTab(route: String) = nav.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    fun startFresh(route: String) = nav.navigate(route) {
        popUpTo(nav.graph.id) { inclusive = true }
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            enterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 10 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(280)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(280)) { it / 10 } }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = { startFresh(Routes.WELCOME) })
            }
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onGetStarted = { nav.navigate(Routes.REGISTER) },
                    onLogin = { nav.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = pop,
                    onLogin = { role -> startFresh(role.startRoute()) },
                    onCreateAccount = { nav.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterFlow(
                    onBack = pop,
                    onPassengerRegistered = { startFresh(Routes.HOME) },
                    onBusRegistered = { bus ->
                        myBus = bus
                        startFresh(Routes.DRIVER)
                    }
                )
            }

            // ---- passenger tabs ----
            composable(Routes.HOME) {
                HomeScreen(
                    draft = draft,
                    onDraftChange = { draft = it },
                    onSearch = { nav.navigate(Routes.RESULTS) },
                    onOpenAlerts = { goTab(Routes.ALERTS) },
                    onTrackBus = { bus ->
                        draft = draft.copy(bus = bus)
                        nav.navigate(Routes.TRACKING)
                    }
                )
            }
            composable(Routes.TICKETS) {
                MyTicketsScreen(tickets) { t ->
                    openTicketId = t.id
                    nav.navigate(Routes.TICKET)
                }
            }
            composable(Routes.ALERTS) {
                AlertsScreen(MockData.alerts) {
                    draft = draft.copy(bus = MockData.buses.first())
                    nav.navigate(Routes.TRACKING)
                }
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSwitchRole = { role -> startFresh(role.startRoute()) },
                    onLogout = { startFresh(Routes.WELCOME) }
                )
            }

            // ---- search, tracking & booking flow ----
            composable(Routes.RESULTS) {
                SearchResultsScreen(
                    draft = draft,
                    onBack = pop,
                    onTrack = { bus ->
                        draft = draft.copy(bus = bus)
                        nav.navigate(Routes.TRACKING)
                    },
                    onBook = { bus ->
                        draft = draft.copy(bus = bus, seats = emptyList())
                        nav.navigate(Routes.SEAT_COUNT)
                    }
                )
            }
            composable(Routes.TRACKING) {
                val bus = draft.bus ?: MockData.buses.first()
                LiveTrackingScreen(bus, onBack = pop, onBook = {
                    draft = draft.copy(bus = bus, seats = emptyList())
                    nav.navigate(Routes.SEAT_COUNT)
                })
            }
            composable(Routes.SEAT_COUNT) {
                SeatCountScreen(draft, onBack = pop) { n ->
                    draft = draft.copy(seatCount = n, seats = emptyList())
                    nav.navigate(Routes.SEGMENT)
                }
            }
            composable(Routes.SEGMENT) {
                SegmentScreen(draft, onBack = pop) { boarding, alighting ->
                    draft = draft.copy(boarding = boarding, alighting = alighting)
                    nav.navigate(Routes.SEATS)
                }
            }
            composable(Routes.SEATS) {
                SeatSelectionScreen(draft, onBack = pop) { seats ->
                    draft = draft.copy(seats = seats)
                    nav.navigate(Routes.PAYMENT)
                }
            }
            composable(Routes.PAYMENT) {
                PaymentScreen(draft, onBack = pop) {
                    val ticket = draft.toTicket()
                    tickets.add(0, ticket)
                    openTicketId = ticket.id
                    nav.navigate(Routes.TICKET) { popUpTo(Routes.HOME) }
                }
            }
            composable(Routes.TICKET) {
                val ticket = tickets.firstOrNull { it.id == openTicketId } ?: tickets.first()
                TicketScreen(
                    ticket = ticket,
                    onDone = { goTab(Routes.TICKETS) },
                    onTrack = {
                        draft = draft.copy(bus = ticket.bus)
                        nav.navigate(Routes.TRACKING)
                    }
                )
            }

            // ---- driver & operator ----
            composable(Routes.DRIVER) {
                DriverScreen(
                    bus = myBus ?: MockData.buses.first(),
                    onReportIncident = { nav.navigate(Routes.REPORT_INCIDENT) },
                    onLogout = { startFresh(Routes.WELCOME) }
                )
            }
            composable(Routes.REPORT_INCIDENT) {
                ReportIncidentScreen(onBack = pop, onDone = pop)
            }
            composable(Routes.ADMIN) {
                AdminDashboardScreen(
                    onOpenIncident = {
                        draft = draft.copy(bus = MockData.buses.first())
                        nav.navigate(Routes.TRACKING)
                    },
                    onLogout = { startFresh(Routes.WELCOME) }
                )
            }
        }

        if (currentRoute in tabs.map { it.route }) {
            BusGoBottomBar(
                tabs = tabs,
                currentRoute = currentRoute,
                onSelect = { goTab(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

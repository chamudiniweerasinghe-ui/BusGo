package com.busgo.app.data.mock

import com.busgo.app.data.model.AdminItem
import com.busgo.app.data.model.AdminTab
import com.busgo.app.data.model.AlertItem
import com.busgo.app.data.model.AlertType
import com.busgo.app.data.model.Bus
import com.busgo.app.data.model.BusCategory
import com.busgo.app.data.model.Passenger
import com.busgo.app.data.model.Seat
import com.busgo.app.data.model.SeatState
import com.busgo.app.data.model.Stop
import com.busgo.app.data.model.Ticket
import com.busgo.app.data.model.TicketStatus
import java.time.LocalDate

/**
 * Fake data so the UI runs without a backend.
 * Replace with calls to your Node.js REST API / Socket.IO later.
 */
object MockData {

    val routeStops = listOf(
        Stop("s1", "Kurunegala", "KUR", 0.0),
        Stop("s2", "Polgahawela", "PLG", 22.0),
        Stop("s3", "Alawwa", "ALW", 30.0),
        Stop("s4", "Warakapola", "WKP", 44.0),
        Stop("s5", "Nittambuwa", "NTB", 58.0),
        Stop("s6", "Kadawatha", "KDW", 78.0),
        Stop("s7", "Colombo Fort", "CMB", 94.0)
    )

    val buses = listOf(
        Bus("b1", "NB-2231", "Ceylon Express", "5", BusCategory.LUXURY, "Kurunegala", "Colombo Fort",
            "06:15", "08:35", "2h 20m", 14, 40, 4.2, 94.0, 12, "Alawwa", 58, listOf("A/C", "Wi-Fi", "USB")),
        Bus("b2", "ND-4410", "SLTB Kurunegala", "5", BusCategory.NORMAL, "Kurunegala", "Colombo Fort",
            "06:40", "09:20", "2h 40m", 27, 40, 2.6, 94.0, 21, "Polgahawela", 46),
        Bus("b3", "WP-8872", "Rajarata Travels", "5/1", BusCategory.PRIVATE, "Kurunegala", "Colombo Fort",
            "07:05", "09:30", "2h 25m", 6, 40, 3.4, 94.0, 34, "Kurunegala", 38, listOf("A/C")),
        Bus("b4", "NB-1190", "Ceylon Express", "5", BusCategory.LUXURY, "Kurunegala", "Colombo Fort",
            "08:00", "10:15", "2h 15m", 22, 40, 4.2, 94.0, 58, "Kurunegala", 0, listOf("A/C", "Wi-Fi")),
        Bus("b5", "NC-7765", "SLTB Kurunegala", "5", BusCategory.NORMAL, "Kurunegala", "Colombo Fort",
            "09:10", "11:50", "2h 40m", 31, 40, 2.6, 94.0, 76, "Kurunegala", 0)
    )

    fun seatsFor(bus: Bus): List<Seat> {
        val seed = bus.id.hashCode()
        return (1..40).map { n ->
            val v = (n * 31 + seed).mod(9)
            val state = when {
                v == 0 || v == 4 || v == 7 -> SeatState.BOOKED
                v == 2 && n % 3 == 0 -> SeatState.HELD
                else -> SeatState.AVAILABLE
            }
            Seat(n, state)
        }
    }

    val passenger = Passenger(
        name = "Kasun Perera",
        email = "kasun@example.com",
        phone = "+94 77 123 4567",
        emergencyName = "Nirmala Perera",
        emergencyRelation = "Parent",
        emergencyPhone = "+94 71 555 0192"
    )

    val sampleTickets = listOf(
        Ticket("BG-482915", buses[0], routeStops[0], routeStops[4], LocalDate.now().plusDays(1),
            listOf(11, 12), 500.0, TicketStatus.UPCOMING),
        Ticket("BG-117204", buses[1], routeStops[0], routeStops[6], LocalDate.now().minusDays(6),
            listOf(23), 250.0, TicketStatus.COMPLETED)
    )

    val alerts = listOf(
        AlertItem("a1", AlertType.EMERGENCY, "Incident reported on NB-2231",
            "Front tyre burst near Alawwa junction. Everyone on board is safe and a replacement bus is on the way.",
            "2 min ago"),
        AlertItem("a2", AlertType.DELAY, "ND-4410 is running 15 minutes late",
            "Heavy traffic at Polgahawela. Your new estimated pickup is 07:05.", "18 min ago"),
        AlertItem("a3", AlertType.BOOKING, "Ticket confirmed",
            "Seats 11 and 12 on NB-2231, Kurunegala to Nittambuwa.", "Yesterday"),
        AlertItem("a4", AlertType.INFO, "Route 5 timetable update",
            "An extra luxury service leaves Kurunegala at 17:30 from next Monday.", "2 days ago")
    )

    fun adminItems(tab: AdminTab): List<AdminItem> = when (tab) {
        AdminTab.BUSES -> listOf(
            AdminItem("NB-2231", "Luxury, 40 seats, Route 5", "On route"),
            AdminItem("NB-1190", "Luxury, 40 seats, Route 5", "At depot"),
            AdminItem("WP-8872", "Private, 40 seats, Route 5/1", "On route"),
            AdminItem("NB-3308", "Luxury, 44 seats, Route 6", "Maintenance", healthy = false)
        )
        AdminTab.ROUTES -> listOf(
            AdminItem("Route 5", "Kurunegala to Colombo Fort, 94 km, 7 stops", "Active"),
            AdminItem("Route 6", "Kurunegala to Kandy, 42 km, 5 stops", "Active"),
            AdminItem("Route 5/1", "Kurunegala to Colombo via Giriulla", "Draft", healthy = false)
        )
        AdminTab.SCHEDULES -> listOf(
            AdminItem("06:15 Daily", "NB-2231 on Route 5", "Running"),
            AdminItem("08:00 Daily", "NB-1190 on Route 5", "Scheduled"),
            AdminItem("17:30 Weekdays", "Unassigned", "Needs bus", healthy = false)
        )
        AdminTab.FARES -> listOf(
            AdminItem("Normal", "LKR 2.60 per km", "Live"),
            AdminItem("Luxury", "LKR 4.20 per km", "Live"),
            AdminItem("Private", "LKR 3.40 per km", "Live")
        )
        AdminTab.DRIVERS -> listOf(
            AdminItem("Sunil Jayasinghe", "Driver on NB-2231", "On duty"),
            AdminItem("Ruwan Bandara", "Conductor on NB-2231", "On duty"),
            AdminItem("Chaminda Silva", "Driver, licence expires in 12 days", "Check", healthy = false)
        )
    }
}

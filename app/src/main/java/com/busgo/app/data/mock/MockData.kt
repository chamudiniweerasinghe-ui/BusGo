package com.busgo.app.data.mock

import com.busgo.app.data.model.AdminItem
import com.busgo.app.data.model.AdminTab
import com.busgo.app.data.model.AlertItem
import com.busgo.app.data.model.Bus
import com.busgo.app.data.model.Passenger
import com.busgo.app.data.model.Seat
import com.busgo.app.data.model.SeatState
import com.busgo.app.data.model.Stop
import com.busgo.app.data.model.Ticket

/**
 * Data structures placeholder for dynamic database sync.
 */
object MockData {
    val routeStops: List<Stop> = emptyList()
    val buses: List<Bus> = emptyList()

    fun seatsFor(bus: Bus): List<Seat> {
        return (1..bus.totalSeats).map { n ->
            Seat(n, SeatState.AVAILABLE)
        }
    }

    val passenger: Passenger = Passenger(
        name = "",
        email = "",
        phone = "",
        emergencyName = "",
        emergencyRelation = "",
        emergencyPhone = ""
    )

    val sampleTickets: List<Ticket> = emptyList()
    val alerts: List<AlertItem> = emptyList()

    fun adminItems(tab: AdminTab): List<AdminItem> = emptyList()
}

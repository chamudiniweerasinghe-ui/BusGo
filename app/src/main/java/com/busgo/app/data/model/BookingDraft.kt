package com.busgo.app.data.model

import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Everything the passenger has chosen so far in the booking flow.
 * Held in the nav graph and passed between screens.
 */
data class BookingDraft(
    val from: String = "Kurunegala",
    val to: String = "Colombo Fort",
    val date: LocalDate = LocalDate.now(),
    val category: BusCategory? = null,
    val bus: Bus? = null,
    val seatCount: Int = 1,
    val boarding: Stop? = null,
    val alighting: Stop? = null,
    val seats: List<Int> = emptyList()
) {
    val distanceKm: Double
        get() = if (boarding != null && alighting != null)
            abs(alighting.kmFromStart - boarding.kmFromStart) else 0.0

    /** Segment fare, rounded up to the nearest 10 rupees. */
    val farePerSeat: Double
        get() = ceil((bus?.ratePerKm ?: 0.0) * distanceKm / 10.0) * 10.0

    val totalFare: Double get() = farePerSeat * seatCount
}

fun BookingDraft.toTicket(): Ticket = Ticket(
    id = "BG-" + (100000..999999).random(),
    bus = requireNotNull(bus),
    boarding = requireNotNull(boarding),
    alighting = requireNotNull(alighting),
    date = date,
    seats = seats,
    fare = totalFare,
    status = TicketStatus.UPCOMING
)

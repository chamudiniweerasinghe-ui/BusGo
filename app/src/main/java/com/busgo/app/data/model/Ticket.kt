package com.busgo.app.data.model

import java.time.LocalDate

enum class TicketStatus { UPCOMING, COMPLETED, CANCELLED }

data class Ticket(
    val id: String,
    val bus: Bus,
    val boarding: Stop,
    val alighting: Stop,
    val date: LocalDate,
    val seats: List<Int>,
    val fare: Double,
    val status: TicketStatus
)

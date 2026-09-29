package com.busgo.app.data.model

enum class SeatState { AVAILABLE, BOOKED, HELD }

data class Seat(
    val number: Int,
    val state: SeatState
)

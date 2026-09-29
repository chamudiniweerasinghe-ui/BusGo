package com.busgo.app.data.model

import kotlin.math.ceil

data class Bus(
    val id: String,
    val number: String,
    val operator: String,
    val routeNo: String,
    val category: BusCategory,
    val from: String,
    val to: String,
    val departure: String,
    val arrival: String,
    val duration: String,
    val seatsLeft: Int,
    val totalSeats: Int,
    val ratePerKm: Double,
    val routeKm: Double,
    val etaMinutes: Int,
    val nextStop: String,
    val speedKmh: Int,
    val amenities: List<String> = emptyList()
) {
    /** Fare for the whole route, rounded up to the nearest 10 rupees. */
    val fullFare: Double get() = ceil(ratePerKm * routeKm / 10.0) * 10.0
}

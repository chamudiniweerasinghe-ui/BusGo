package com.busgo.app.data.model

data class Passenger(
    val name: String,
    val email: String,
    val phone: String,
    val emergencyName: String,
    val emergencyRelation: String,
    val emergencyPhone: String
) {
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}

package com.busgo.app.data.model

enum class AdminTab(val label: String, val singular: String) {
    BUSES("Buses", "bus"),
    ROUTES("Routes", "route"),
    SCHEDULES("Schedules", "schedule"),
    FARES("Fares", "fare"),
    DRIVERS("Drivers", "driver")
}

data class AdminItem(
    val title: String,
    val subtitle: String,
    val status: String,
    val healthy: Boolean = true
)

package com.busgo.app.data.model

enum class AlertType { EMERGENCY, DELAY, BOOKING, INFO }

data class AlertItem(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val time: String
)

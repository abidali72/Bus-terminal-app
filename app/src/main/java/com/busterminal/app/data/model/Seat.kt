package com.busterminal.app.data.model

data class Seat(
    val id: String = "",      // Unique ID for the seat within the bus layout e.g. "L1-A1"
    val seatNumber: String = "", // Display number e.g. "A1"
    val type: String = "normal", // normal, premium, sleeper, ladies
    val status: String = "available", // available, booked, blocked
    val floor: Int = 1,       // 1 for Lower, 2 for Upper (Sleeper buses)
    val row: Int = 0,
    val column: Int = 0, // Visual grid position
    val priceMultiplier: Double = 1.0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "seatNumber" to seatNumber,
        "type" to type,
        "status" to status,
        "floor" to floor,
        "row" to row,
        "column" to column,
        "priceMultiplier" to priceMultiplier
    )
}

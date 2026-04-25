package com.busterminal.app.data.model

data class Bus(
    val id: String = "",
    val busNumber: String = "", // License Plate or Unique ID
    val companyId: String = "",
    val routeId: String = "",
    val busName: String = "",
    val busType: String = "AC", // AC, Non-AC, Sleeper, Executive
    val seatLayoutType: String = "2x2", // 2x2, 2x1, sleeper
    val totalSeats: Int = 40,
    val baseFare: Double = 0.0,
    val amenities: List<String> = emptyList(), // WiFi, Charging, etc.
    val imageUrls: List<String> = emptyList(),
    val status: String = "active", // active, maintenance, disabled
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "busNumber" to busNumber,
        "companyId" to companyId,
        "routeId" to routeId,
        "busName" to busName,
        "busType" to busType,
        "seatLayoutType" to seatLayoutType,
        "totalSeats" to totalSeats,
        "baseFare" to baseFare,
        "amenities" to amenities,
        "imageUrls" to imageUrls,
        "status" to status,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

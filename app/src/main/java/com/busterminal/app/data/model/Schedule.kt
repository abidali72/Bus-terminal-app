package com.busterminal.app.data.model

data class Schedule(
    val id: String = "",
    val companyId: String = "",
    val busId: String = "",
    val routeId: String = "",
    val driverId: String? = null,
    val driverName: String = "",
    val driverPhone: String = "",
    val departureTime: Long = 0L,
    val arrivalTime: Long = 0L, // Auto-calculated or manual
    val basePrice: Double = 0.0,
    val recurringDays: List<Int> = emptyList(), // 1=Sun, 2=Mon... Empty if one-time
    val status: String = "scheduled", // scheduled, live, completed, cancelled, delayed
    val availableSeats: Int = 0,
    val boardingPoints: List<String> = emptyList(),
    val dropOffPoints: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "companyId" to companyId,
        "busId" to busId,
        "routeId" to routeId,
        "driverId" to driverId,
        "driverName" to driverName,
        "driverPhone" to driverPhone,
        "departureTime" to departureTime,
        "arrivalTime" to arrivalTime,
        "basePrice" to basePrice,
        "recurringDays" to recurringDays,
        "status" to status,
        "availableSeats" to availableSeats,
        "boardingPoints" to boardingPoints,
        "dropOffPoints" to dropOffPoints,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

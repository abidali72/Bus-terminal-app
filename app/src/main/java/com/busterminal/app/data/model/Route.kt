package com.busterminal.app.data.model

data class Route(
    val id: String = "",
    val companyId: String = "",
    val routeCode: String = "", // Auto-generated
    val fromCity: String = "",
    val toCity: String = "",
    val stops: List<String> = emptyList(),
    val distanceKm: Double = 0.0,
    val estimatedDurationMinutes: Int = 0,
    val status: String = "active", // active, inactive
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "companyId" to companyId,
        "routeCode" to routeCode,
        "fromCity" to fromCity,
        "toCity" to toCity,
        "stops" to stops,
        "distanceKm" to distanceKm,
        "estimatedDurationMinutes" to estimatedDurationMinutes,
        "status" to status,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

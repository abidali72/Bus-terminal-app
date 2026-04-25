package com.busterminal.app.data.model

data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val licenseNumber: String = "",
    val licenseExpiryDate: Long = 0L,
    val profileImageUrl: String = "",
    val rating: Double = 0.0,
    val experienceYears: Int = 0,
    val companyId: String = "",
    val assignedBusId: String? = null, // Can be null if not assigned
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "licenseNumber" to licenseNumber,
        "licenseExpiryDate" to licenseExpiryDate,
        "profileImageUrl" to profileImageUrl,
        "rating" to rating,
        "experienceYears" to experienceYears,
        "companyId" to companyId,
        "assignedBusId" to assignedBusId,
        "createdAt" to createdAt
    )
}

package com.busterminal.app.data.model

data class Company(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val ownerName: String = "",
    val registrationNumber: String = "",
    val approvalStatus: String = "pending",
    val logoUrl: String = "",
    val licenseDocumentUrl: String = "",
    val ownerId: String = "",
    val rating: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "email" to email,
        "address" to address,
        "city" to city,
        "ownerName" to ownerName,
        "registrationNumber" to registrationNumber,
        "approvalStatus" to approvalStatus,
        "logoUrl" to logoUrl,
        "licenseDocumentUrl" to licenseDocumentUrl,
        "ownerId" to ownerId,
        "rating" to rating,
        "createdAt" to createdAt
    )
}

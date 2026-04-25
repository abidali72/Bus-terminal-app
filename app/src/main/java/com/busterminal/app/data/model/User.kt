package com.busterminal.app.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val status: String = "active",
    val profileImageUrl: String = "",
    val city: String = "",
    val gender: String = "",
    val dateOfBirth: String = "", // stored as string for simplicity or timestamp
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "role" to role,
        "status" to status,
        "profileImageUrl" to profileImageUrl,
        "city" to city,
        "gender" to gender,
        "dateOfBirth" to dateOfBirth,
        "createdAt" to createdAt
    )
}

package com.busterminal.app.data.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val targetRole: String = "all",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "message" to message,
        "createdBy" to createdBy,
        "createdByName" to createdByName,
        "targetRole" to targetRole,
        "createdAt" to createdAt
    )
}

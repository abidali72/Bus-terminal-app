package com.busterminal.app.data.model

data class ActivityLog(
    val id: String = "",
    val action: String = "",
    val performedBy: String = "",
    val performedByName: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "action" to action,
        "performedBy" to performedBy,
        "performedByName" to performedByName,
        "targetType" to targetType,
        "targetId" to targetId,
        "details" to details,
        "timestamp" to timestamp
    )
}

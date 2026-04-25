package com.busterminal.app.util

object Constants {
    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val COMPANIES_COLLECTION = "companies"
    const val BUSES_COLLECTION = "buses"
    const val DRIVERS_COLLECTION = "drivers"
    const val ANNOUNCEMENTS_COLLECTION = "announcements"
    const val ACTIVITY_LOGS_COLLECTION = "activity_logs"
    const val ROUTES_COLLECTION = "routes"
    const val SCHEDULES_COLLECTION = "schedules"
    const val FAVORITES_COLLECTION = "favorites"

    // User Roles
    const val ROLE_PASSENGER = "passenger"
    const val ROLE_COMPANY = "company"
    const val ROLE_ADMIN = "admin"

    // Approval Statuses
    const val STATUS_PENDING = "pending"
    const val STATUS_APPROVED = "approved"
    const val STATUS_SUSPENDED = "suspended"

    // Bus Statuses
    const val BUS_ACTIVE = "active"
    const val BUS_DELAYED = "delayed"
    const val BUS_CANCELLED = "cancelled"
    const val BUS_MAINTENANCE = "maintenance"
    const val BUS_DISABLED = "disabled"

    // Route Statuses
    const val ROUTE_ACTIVE = "active"
    const val ROUTE_INACTIVE = "inactive"

    // Schedule Statuses
    const val SCHEDULE_SCHEDULED = "scheduled"
    const val SCHEDULE_LIVE = "live"
    const val SCHEDULE_COMPLETED = "completed"
    const val SCHEDULE_CANCELLED = "cancelled"
    const val SCHEDULE_DELAYED = "delayed"

    // Bus Types
    const val BUS_TYPE_AC = "AC"
    const val BUS_TYPE_NON_AC = "Non-AC"
    const val BUS_TYPE_SLEEPER = "Sleeper"
    const val BUS_TYPE_LUXURY = "Luxury"

    // Admin invite code
    const val ADMIN_INVITE_CODE = "BUSTERMINAL2024"
}

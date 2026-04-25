package com.busterminal.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Passenger
    data object PassengerHome : Screen("passenger_home")
    data object BusList : Screen("bus_list/{from}/{to}") {
        fun createRoute(from: String, to: String) = "bus_list/$from/$to"
    }
    data object BusDetail : Screen("bus_detail/{busId}") {
        fun createRoute(busId: String) = "bus_detail/$busId"
    }
    data object Search : Screen("search")
    data object Companies : Screen("companies")
    data object CompanyBuses : Screen("company_buses/{companyId}") {
        fun createRoute(companyId: String) = "company_buses/$companyId"
    }
    data object Favorites : Screen("favorites")
    data object RouteDetails : Screen("route_details/{routeId}") {
        fun createRoute(routeId: String) = "route_details/$routeId"
    }
    data object PassengerProfile : Screen("passenger_profile")
    data object EditProfile : Screen("edit_profile")

    // Transport Company
    data object CompanyDashboard : Screen("company_dashboard")
    data object AddEditBus : Screen("add_edit_bus?busId={busId}") {
        fun createRoute(busId: String? = null) = "add_edit_bus?busId=${busId ?: ""}"
    }
    data object DriverManagement : Screen("driver_management")
    data object AddEditDriver : Screen("add_edit_driver?driverId={driverId}") {
        fun createRoute(driverId: String? = null) = "add_edit_driver?driverId=${driverId ?: ""}"
    }
    data object CompanyProfile : Screen("company_profile")

    // Admin
    data object AdminDashboard : Screen("admin_dashboard")
    data object CompanyManagement : Screen("company_management")
    data object AdminBusManagement : Screen("admin_bus_management")
    data object AdminDriverManagement : Screen("admin_driver_management")
    data object RouteManagement : Screen("route_management")
    data object ScheduleManagement : Screen("schedule_management")
    data object Announcements : Screen("announcements")
    data object ActivityLogs : Screen("activity_logs")
}

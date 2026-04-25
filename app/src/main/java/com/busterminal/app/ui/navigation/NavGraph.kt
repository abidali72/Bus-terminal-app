package com.busterminal.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.busterminal.app.data.model.Bus
import com.busterminal.app.ui.admin.*
import com.busterminal.app.ui.auth.*
import com.busterminal.app.ui.common.LoadingScreen
import com.busterminal.app.ui.company.*
import com.busterminal.app.ui.passenger.*
import com.busterminal.app.ui.profile.*
import com.busterminal.app.util.Constants

// ─── Bottom Nav Items ────────────────────────────────────────
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val passengerNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.PassengerHome.route),
    BottomNavItem("Search", Icons.Default.Search, Screen.Search.route),
    BottomNavItem("Companies", Icons.Default.Business, Screen.Companies.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.PassengerProfile.route)
)

val companyNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Dashboard, Screen.CompanyDashboard.route),
    BottomNavItem("Drivers", Icons.Default.People, Screen.DriverManagement.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.CompanyProfile.route)
)

val adminNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.AdminPanelSettings, Screen.AdminDashboard.route),
    BottomNavItem("Companies", Icons.Default.Business, Screen.CompanyManagement.route),
    BottomNavItem("Routes", Icons.Default.Route, Screen.RouteManagement.route),
    BottomNavItem("Announce", Icons.Default.Campaign, Screen.Announcements.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.PassengerProfile.route)
)

// ─── Main App Entry Point ────────────────────────────────────
@Composable
fun BusTerminalMainScreen() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    if (authState.isLoading) {
        LoadingScreen()
        return
    }

    if (!authState.isLoggedIn && !authState.isGuest) {
        AuthNavGraph(authViewModel = authViewModel)
    } else {
        if (authState.isGuest) {
            PassengerMainScreen(
                userId = "guest",
                userName = "Guest",
                onLogout = { authViewModel.logout() }
            )
        } else {
            val user = authState.currentUser!!
            when (user.role) {
                Constants.ROLE_PASSENGER -> PassengerMainScreen(
                    userId = user.id,
                    userName = user.name,
                    onLogout = { authViewModel.logout() }
                )
                Constants.ROLE_COMPANY -> CompanyMainScreen(
                    userId = user.id,
                    userName = user.name,
                    onLogout = { authViewModel.logout() }
                )
                Constants.ROLE_ADMIN -> AdminMainScreen(
                    userId = user.id,
                    userName = user.name,
                    onLogout = { authViewModel.logout() }
                )
                else -> AuthNavGraph(authViewModel = authViewModel)
            }
        }
    }
}

// ─── Auth Navigation Graph ────────────────────────────────────
@Composable
fun AuthNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authState.isLoggedIn) {
        // Navigation handled by parent BusTerminalMainScreen
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onLogin = { email, pass -> authViewModel.login(email, pass) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onSkip = { authViewModel.skipLogin() },
                onClearError = { authViewModel.clearError() }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onRegister = { name, email, phone, pass, role, compName, compAddr, city, gender, dob, ownerName, regNum ->
                    authViewModel.register(
                        name = name,
                        email = email,
                        phone = phone,
                        password = pass,
                        role = role,
                        companyName = compName,
                        companyAddress = compAddr,
                        city = city,
                        gender = gender,
                        dateOfBirth = dob,
                        ownerName = ownerName,
                        registrationNumber = regNum
                    )
                },
                onNavigateToLogin = { navController.popBackStack() },
                onClearError = { authViewModel.clearError() }
            )
        }
    }
}

// ─── Passenger Main Screen with Bottom Nav ────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerMainScreen(userId: String, userName: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val passengerViewModel: PassengerViewModel = hiltViewModel()
    val uiState by passengerViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in passengerNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    passengerNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PassengerHome.route,
            modifier = Modifier.padding(padding)
        ) {
            // ─── Home Screen (Routes) ────────────────────
            composable(Screen.PassengerHome.route) {
                PassengerHomeScreen(
                    uiState = uiState,
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onRouteClick = { route ->
                        passengerViewModel.selectRoute(route)
                        navController.navigate(Screen.RouteDetails.createRoute(route.id))
                    },
                    onCompaniesClick = { navController.navigate(Screen.Companies.route) },
                    onRefresh = { passengerViewModel.refresh() },
                    onFilterByType = { passengerViewModel.filterByBusType(it) }
                )
            }

            // ─── Search Screen ───────────────────────────
            composable(Screen.Search.route) {
                SearchScreen(
                    uiState = uiState,
                    onSearch = { from, to -> passengerViewModel.searchRoutes(from, to) },
                    onRouteClick = { route ->
                        passengerViewModel.selectRoute(route)
                        navController.navigate(Screen.RouteDetails.createRoute(route.id))
                    },
                    onClearSearch = { passengerViewModel.clearSearch() }
                )
            }

            // ─── Route Details Screen ────────────────────
            composable(
                route = Screen.RouteDetails.route,
                arguments = listOf(navArgument("routeId") { type = NavType.StringType })
            ) {
                RouteDetailsScreen(
                    uiState = uiState,
                    onBack = {
                        passengerViewModel.clearRouteDetails()
                        navController.popBackStack()
                    },
                    onSortBy = { passengerViewModel.sortBy(it) },
                    onBusDetailClick = { scheduleWithBus ->
                        scheduleWithBus.bus?.let { bus ->
                            navController.navigate(Screen.BusDetail.createRoute(bus.id))
                        }
                    }
                )
            }

            // ─── Bus Detail Screen ───────────────────────
            composable(
                route = Screen.BusDetail.route,
                arguments = listOf(navArgument("busId") { type = NavType.StringType })
            ) { backStackEntry ->
                val busId = backStackEntry.arguments?.getString("busId") ?: ""
                // Find the bus from the route details
                val bus = uiState.routeBuses[busId]
                bus?.let {
                    BusDetailScreen(
                        bus = it,
                        onBack = { navController.popBackStack() },
                        isFavorite = uiState.favorites.contains(bus.id),
                        onToggleFavorite = { passengerViewModel.toggleFavorite(bus.id) }
                    )
                }
            }

            // ─── Companies Screen ────────────────────────
            composable(Screen.Companies.route) {
                CompanyListScreen(
                    companies = uiState.companies,
                    onCompanyClick = { /* Could navigate to company detail */ }
                )
            }

            // ─── Profile Screen ──────────────────────────
            composable(Screen.PassengerProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                val companyVM: CompanyViewModel = hiltViewModel()
                ProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.EditProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                val companyVM: CompanyViewModel = hiltViewModel()
                EditProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Company Main Screen with Bottom Nav ──────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyMainScreen(userId: String, userName: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val companyViewModel: CompanyViewModel = hiltViewModel()
    val uiState by companyViewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        companyViewModel.loadCompanyData(userId)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in companyNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    companyNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CompanyDashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.CompanyDashboard.route) {
                CompanyDashboardScreen(
                    company = uiState.company,
                    buses = uiState.buses,
                    drivers = uiState.drivers,
                    isLoading = uiState.isLoading,
                    onAddBus = { navController.navigate("add_bus") },
                    onEditBus = { bus ->
                        // Navigate to edit screen (simplified)
                        navController.navigate("add_bus")
                    },
                    onDeleteBus = { busId -> companyViewModel.deleteBus(busId) },
                    onStatusChange = { busId, status -> companyViewModel.updateBusStatus(busId, status) },
                    onManageDrivers = { navController.navigate(Screen.DriverManagement.route) }
                )
            }

            composable(Screen.DriverManagement.route) {
                DriverManagementScreen(
                    drivers = uiState.drivers,
                    companyId = uiState.company?.id ?: "",
                    onAddDriver = { driver -> companyViewModel.addDriver(driver) },
                    onUpdateDriver = { driver -> companyViewModel.updateDriver(driver) },
                    onDeleteDriver = { id -> companyViewModel.deleteDriver(id) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("add_bus") {
                AddEditBusScreen(
                    companyId = uiState.company?.id ?: "",
                    drivers = uiState.drivers,
                    isLoading = uiState.isLoading,
                    onSave = { bus -> companyViewModel.addBus(bus) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CompanyProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                ProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyViewModel,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.EditProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                EditProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Admin Main Screen with Bottom Nav ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(userId: String, userName: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val adminViewModel: AdminViewModel = hiltViewModel()
    val uiState by adminViewModel.uiState.collectAsState()
    val routeScheduleViewModel: AdminRouteScheduleViewModel = hiltViewModel()
    val routeScheduleUiState by routeScheduleViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in adminNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    adminNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AdminDashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    companies = uiState.companies,
                    buses = uiState.allBuses,
                    drivers = uiState.allDrivers,
                    pendingCompanies = uiState.pendingCompanies,
                    onCompanyManagement = { navController.navigate(Screen.CompanyManagement.route) },
                    onBusManagement = { navController.navigate(Screen.AdminBusManagement.route) },
                    onDriverManagement = { /* Driver management screen */ },
                    onAnnouncements = { navController.navigate(Screen.Announcements.route) },
                    onRouteManagement = { navController.navigate(Screen.RouteManagement.route) },
                    onScheduleManagement = { navController.navigate(Screen.ScheduleManagement.route) }
                )
            }

            composable(Screen.CompanyManagement.route) {
                CompanyManagementScreen(
                    companies = uiState.companies,
                    onApprove = { companyId -> adminViewModel.approveCompany(companyId) },
                    onSuspend = { companyId -> adminViewModel.suspendCompany(companyId) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Announcements.route) {
                AnnouncementScreen(
                    announcements = uiState.announcements,
                    adminName = userName,
                    adminId = userId,
                    onPost = { title, message, name, id ->
                        adminViewModel.postAnnouncement(title, message, name, id)
                    },
                    onDelete = { id -> adminViewModel.deleteAnnouncement(id) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Route Management ───────────────────────────────
            composable(Screen.RouteManagement.route) {
                RouteManagementScreen(
                    uiState = routeScheduleUiState,
                    onAddRoute = { routeScheduleViewModel.addRoute(it) },
                    onUpdateRoute = { routeScheduleViewModel.updateRoute(it) },
                    onSoftDeleteRoute = { routeScheduleViewModel.softDeleteRoute(it) },
                    onSearchChange = { routeScheduleViewModel.updateRouteSearch(it) },
                    onStatusFilterChange = { routeScheduleViewModel.updateRouteStatusFilter(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Bus Management (Admin) ────────────────────────
            composable(Screen.AdminBusManagement.route) {
                AdminBusManagementScreen(
                    uiState = routeScheduleUiState,
                    onAddBus = { routeScheduleViewModel.addBus(it) },
                    onUpdateBus = { routeScheduleViewModel.updateBus(it) },
                    onSoftDeleteBus = { routeScheduleViewModel.softDeleteBus(it) },
                    onSearchChange = { routeScheduleViewModel.updateBusSearch(it) },
                    onStatusFilterChange = { routeScheduleViewModel.updateBusStatusFilter(it) },
                    onTypeFilterChange = { routeScheduleViewModel.updateBusTypeFilter(it) },
                    getCompanyName = { routeScheduleViewModel.getCompanyName(it) },
                    getRouteName = { routeScheduleViewModel.getRouteName(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Schedule Management ───────────────────────────
            composable(Screen.ScheduleManagement.route) {
                ScheduleManagementScreen(
                    uiState = routeScheduleUiState,
                    onAddSchedule = { routeScheduleViewModel.addSchedule(it) },
                    onUpdateSchedule = { routeScheduleViewModel.updateSchedule(it) },
                    onCancelSchedule = { routeScheduleViewModel.cancelSchedule(it) },
                    onRouteFilterChange = { routeScheduleViewModel.updateScheduleRouteFilter(it) },
                    onCompanyFilterChange = { routeScheduleViewModel.updateScheduleCompanyFilter(it) },
                    onStatusFilterChange = { routeScheduleViewModel.updateScheduleStatusFilter(it) },
                    getBusNumber = { routeScheduleViewModel.getBusNumber(it) },
                    getRouteName = { routeScheduleViewModel.getRouteName(it) },
                    getCompanyName = { routeScheduleViewModel.getCompanyName(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PassengerProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                val companyVM: CompanyViewModel = hiltViewModel()
                ProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = onLogout
                )
            }

            composable(Screen.EditProfile.route) {
                val authVM: AuthViewModel = hiltViewModel()
                val companyVM: CompanyViewModel = hiltViewModel()
                EditProfileScreen(
                    authViewModel = authVM,
                    companyViewModel = companyVM,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

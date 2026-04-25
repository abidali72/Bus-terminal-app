package com.busterminal.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busterminal.app.data.model.*
import com.busterminal.app.domain.repository.*
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UI State ────────────────────────────────────────────────
data class AdminRouteScheduleUiState(
    val routes: List<Route> = emptyList(),
    val buses: List<Bus> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val companies: List<Company> = emptyList(),
    // Filtered views
    val filteredRoutes: List<Route> = emptyList(),
    val filteredBuses: List<Bus> = emptyList(),
    val filteredSchedules: List<Schedule> = emptyList(),
    // Filter state
    val routeSearchQuery: String = "",
    val busSearchQuery: String = "",
    val routeStatusFilter: String = "all", // all, active, inactive
    val busStatusFilter: String = "all",
    val busTypeFilter: String = "all",
    val scheduleRouteFilter: String = "all",
    val scheduleCompanyFilter: String = "all",
    val scheduleStatusFilter: String = "all",
    // Loading & messages
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionSuccess: String? = null
)

// ─── ViewModel ───────────────────────────────────────────────
@HiltViewModel
class AdminRouteScheduleViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository,
    private val scheduleRepository: ScheduleRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminRouteScheduleUiState())
    val uiState: StateFlow<AdminRouteScheduleUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    // ─── Data Loading ─────────────────────────────────────────
    private fun loadAllData() {
        viewModelScope.launch {
            routeRepository.getAllRoutes().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(routes = result.data, isLoading = false).applyRouteFilters()
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            busRepository.getAllBuses().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(buses = result.data, isLoading = false).applyBusFilters()
                    }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            scheduleRepository.getAllSchedules().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update {
                        it.copy(schedules = result.data, isLoading = false).applyScheduleFilters()
                    }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            companyRepository.getAllCompanies().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update { it.copy(companies = result.data) }
                    else -> {}
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ROUTE CRUD
    // ═══════════════════════════════════════════════════════════

    fun addRoute(route: Route) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Duplicate check
            when (val dupCheck = routeRepository.checkDuplicateRoute(route.fromCity, route.toCity)) {
                is Resource.Success -> {
                    if (dupCheck.data) {
                        _uiState.update { it.copy(error = "Route from ${route.fromCity} to ${route.toCity} already exists", isLoading = false) }
                        return@launch
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = dupCheck.message, isLoading = false) }
                    return@launch
                }
                else -> {}
            }
            // Generate route code
            val routeCode = "RT-${route.fromCity.take(3).uppercase()}-${route.toCity.take(3).uppercase()}-${System.currentTimeMillis() % 10000}"
            val newRoute = route.copy(routeCode = routeCode)
            when (routeRepository.addRoute(newRoute)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Route added successfully", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to add route", isLoading = false) }
                else -> {}
            }
        }
    }

    fun updateRoute(route: Route) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updated = route.copy(updatedAt = System.currentTimeMillis())
            when (routeRepository.updateRoute(updated)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(actionSuccess = "Route updated", isLoading = false) }
                    // If route deactivated, cancel future schedules
                    if (updated.status == Constants.ROUTE_INACTIVE) {
                        cancelSchedulesForRoute(updated.id)
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to update route", isLoading = false) }
                else -> {}
            }
        }
    }

    fun softDeleteRoute(routeId: String) {
        viewModelScope.launch {
            // Guard: check if buses are assigned to this route
            val assignedBuses = _uiState.value.buses.filter { it.routeId == routeId && it.status != Constants.BUS_DISABLED }
            if (assignedBuses.isNotEmpty()) {
                _uiState.update { it.copy(error = "Cannot delete route: ${assignedBuses.size} active bus(es) assigned. Reassign or disable them first.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            when (routeRepository.softDeleteRoute(routeId)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Route deactivated", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to deactivate route", isLoading = false) }
                else -> {}
            }
        }
    }

    private fun cancelSchedulesForRoute(routeId: String) {
        viewModelScope.launch {
            val futureSchedules = _uiState.value.schedules.filter {
                it.routeId == routeId &&
                it.status == Constants.SCHEDULE_SCHEDULED &&
                it.departureTime > System.currentTimeMillis()
            }
            futureSchedules.forEach { schedule ->
                scheduleRepository.cancelSchedule(schedule.id)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // BUS CRUD
    // ═══════════════════════════════════════════════════════════

    fun addBus(bus: Bus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Duplicate bus number check
            when (val dupCheck = busRepository.checkDuplicateBusNumber(bus.busNumber)) {
                is Resource.Success -> {
                    if (dupCheck.data) {
                        _uiState.update { it.copy(error = "Bus number '${bus.busNumber}' already exists", isLoading = false) }
                        return@launch
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = dupCheck.message, isLoading = false) }
                    return@launch
                }
                else -> {}
            }
            when (busRepository.addBus(bus)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Bus added successfully", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to add bus", isLoading = false) }
                else -> {}
            }
        }
    }

    fun updateBus(bus: Bus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updated = bus.copy(updatedAt = System.currentTimeMillis())
            when (busRepository.updateBus(updated)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(actionSuccess = "Bus updated", isLoading = false) }
                    // If seat count changed, update related schedules
                    val oldBus = _uiState.value.buses.find { it.id == bus.id }
                    if (oldBus != null && oldBus.totalSeats != bus.totalSeats) {
                        updateScheduleSeatsForBus(bus.id, bus.totalSeats)
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to update bus", isLoading = false) }
                else -> {}
            }
        }
    }

    fun softDeleteBus(busId: String) {
        viewModelScope.launch {
            // Guard: check for active schedules
            val activeSchedules = _uiState.value.schedules.filter {
                it.busId == busId && it.status in listOf(Constants.SCHEDULE_SCHEDULED, Constants.SCHEDULE_LIVE)
            }
            if (activeSchedules.isNotEmpty()) {
                _uiState.update { it.copy(error = "Cannot delete bus: ${activeSchedules.size} active schedule(s) exist. Cancel them first.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            when (busRepository.softDeleteBus(busId)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Bus disabled", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to disable bus", isLoading = false) }
                else -> {}
            }
        }
    }

    private fun updateScheduleSeatsForBus(busId: String, newTotalSeats: Int) {
        viewModelScope.launch {
            val schedulesToUpdate = _uiState.value.schedules.filter {
                it.busId == busId && it.status == Constants.SCHEDULE_SCHEDULED
            }
            schedulesToUpdate.forEach { schedule ->
                val updated = schedule.copy(
                    availableSeats = newTotalSeats,
                    updatedAt = System.currentTimeMillis()
                )
                scheduleRepository.updateSchedule(updated)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SCHEDULE CRUD
    // ═══════════════════════════════════════════════════════════

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Validate arrival > departure
            if (schedule.arrivalTime <= schedule.departureTime) {
                _uiState.update { it.copy(error = "Arrival time must be after departure time", isLoading = false) }
                return@launch
            }
            // Duplicate check
            when (val dupCheck = scheduleRepository.checkDuplicateSchedule(schedule.busId, schedule.departureTime)) {
                is Resource.Success -> {
                    if (dupCheck.data) {
                        _uiState.update { it.copy(error = "A schedule already exists for this bus at this departure time", isLoading = false) }
                        return@launch
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = dupCheck.message, isLoading = false) }
                    return@launch
                }
                else -> {}
            }
            // Auto-set available seats from bus
            val bus = _uiState.value.buses.find { it.id == schedule.busId }
            val newSchedule = schedule.copy(
                availableSeats = bus?.totalSeats ?: schedule.availableSeats,
                routeId = bus?.routeId ?: schedule.routeId,
                companyId = bus?.companyId ?: schedule.companyId
            )
            when (scheduleRepository.addSchedule(newSchedule)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Schedule created", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to create schedule", isLoading = false) }
                else -> {}
            }
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (schedule.arrivalTime <= schedule.departureTime) {
                _uiState.update { it.copy(error = "Arrival time must be after departure time", isLoading = false) }
                return@launch
            }
            val updated = schedule.copy(updatedAt = System.currentTimeMillis())
            when (scheduleRepository.updateSchedule(updated)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Schedule updated", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to update schedule", isLoading = false) }
                else -> {}
            }
        }
    }

    fun cancelSchedule(scheduleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (scheduleRepository.cancelSchedule(scheduleId)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Schedule cancelled", isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to cancel schedule", isLoading = false) }
                else -> {}
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FILTERS & SEARCH
    // ═══════════════════════════════════════════════════════════

    fun updateRouteSearch(query: String) {
        _uiState.update { it.copy(routeSearchQuery = query).applyRouteFilters() }
    }

    fun updateRouteStatusFilter(status: String) {
        _uiState.update { it.copy(routeStatusFilter = status).applyRouteFilters() }
    }

    fun updateBusSearch(query: String) {
        _uiState.update { it.copy(busSearchQuery = query).applyBusFilters() }
    }

    fun updateBusStatusFilter(status: String) {
        _uiState.update { it.copy(busStatusFilter = status).applyBusFilters() }
    }

    fun updateBusTypeFilter(type: String) {
        _uiState.update { it.copy(busTypeFilter = type).applyBusFilters() }
    }

    fun updateScheduleRouteFilter(routeId: String) {
        _uiState.update { it.copy(scheduleRouteFilter = routeId).applyScheduleFilters() }
    }

    fun updateScheduleCompanyFilter(companyId: String) {
        _uiState.update { it.copy(scheduleCompanyFilter = companyId).applyScheduleFilters() }
    }

    fun updateScheduleStatusFilter(status: String) {
        _uiState.update { it.copy(scheduleStatusFilter = status).applyScheduleFilters() }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, actionSuccess = null) }
    }

    // ─── Helper: get human-readable names ─────────────────────
    fun getCompanyName(companyId: String): String {
        return _uiState.value.companies.find { it.id == companyId }?.name ?: "Unknown"
    }

    fun getRouteName(routeId: String): String {
        val route = _uiState.value.routes.find { it.id == routeId }
        return if (route != null) "${route.fromCity} → ${route.toCity}" else "Unknown"
    }

    fun getBusNumber(busId: String): String {
        return _uiState.value.buses.find { it.id == busId }?.busNumber ?: "Unknown"
    }
}

// ─── Filter Extension Functions ──────────────────────────────
private fun AdminRouteScheduleUiState.applyRouteFilters(): AdminRouteScheduleUiState {
    var filtered = routes
    if (routeStatusFilter != "all") {
        filtered = filtered.filter { it.status == routeStatusFilter }
    }
    if (routeSearchQuery.isNotBlank()) {
        val q = routeSearchQuery.lowercase()
        filtered = filtered.filter {
            it.fromCity.lowercase().contains(q) ||
            it.toCity.lowercase().contains(q) ||
            it.routeCode.lowercase().contains(q)
        }
    }
    return copy(filteredRoutes = filtered)
}

private fun AdminRouteScheduleUiState.applyBusFilters(): AdminRouteScheduleUiState {
    var filtered = buses
    if (busStatusFilter != "all") {
        filtered = filtered.filter { it.status == busStatusFilter }
    }
    if (busTypeFilter != "all") {
        filtered = filtered.filter { it.busType == busTypeFilter }
    }
    if (busSearchQuery.isNotBlank()) {
        val q = busSearchQuery.lowercase()
        filtered = filtered.filter {
            it.busNumber.lowercase().contains(q) ||
            it.busName.lowercase().contains(q)
        }
    }
    return copy(filteredBuses = filtered)
}

private fun AdminRouteScheduleUiState.applyScheduleFilters(): AdminRouteScheduleUiState {
    var filtered = schedules
    if (scheduleRouteFilter != "all") {
        filtered = filtered.filter { it.routeId == scheduleRouteFilter }
    }
    if (scheduleCompanyFilter != "all") {
        filtered = filtered.filter { it.companyId == scheduleCompanyFilter }
    }
    if (scheduleStatusFilter != "all") {
        filtered = filtered.filter { it.status == scheduleStatusFilter }
    }
    return copy(filteredSchedules = filtered)
}

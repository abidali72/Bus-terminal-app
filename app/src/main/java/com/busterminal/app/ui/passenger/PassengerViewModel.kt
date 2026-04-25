package com.busterminal.app.ui.passenger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busterminal.app.data.model.*
import com.busterminal.app.domain.repository.*
import com.busterminal.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Data Classes ────────────────────────────────────────────

enum class SortOption { DEPARTURE, PRICE, DURATION, RATING }

data class ScheduleWithBus(
    val schedule: Schedule,
    val bus: Bus? = null,
    val driver: Driver? = null
)

data class CompanyBusGroup(
    val company: Company,
    val schedules: List<ScheduleWithBus>,
    val minFare: Double,
    val earliestDeparture: Long,
    val totalAvailableSeats: Int
)

data class PassengerUiState(
    // Route listing
    val routes: List<Route> = emptyList(),
    val filteredRoutes: List<Route> = emptyList(),
    val companies: List<Company> = emptyList(),
    val companiesMap: Map<String, Company> = emptyMap(),

    // Route Details
    val selectedRoute: Route? = null,
    val routeSchedules: List<Schedule> = emptyList(),
    val routeBuses: Map<String, Bus> = emptyMap(),
    val groupedByCompany: List<CompanyBusGroup> = emptyList(),

    // Sort & Filter
    val sortOption: SortOption = SortOption.DEPARTURE,
    val searchFrom: String = "",
    val searchTo: String = "",
    val selectedBusType: String? = null,

    // States
    val isLoading: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val error: String? = null,
    val detailsError: String? = null,
    val favorites: Set<String> = emptySet()
)

// ─── ViewModel ───────────────────────────────────────────────

@HiltViewModel
class PassengerViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val scheduleRepository: ScheduleRepository,
    private val busRepository: BusRepository,
    private val companyRepository: CompanyRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PassengerUiState())
    val uiState: StateFlow<PassengerUiState> = _uiState.asStateFlow()

    private var routeDetailsJob: Job? = null

    init {
        loadRoutes()
        loadCompanies()
    }

    // ─── Route Loading ───────────────────────────────────────

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository.getActiveRoutes().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                routes = result.data,
                                filteredRoutes = applyFilters(result.data, it.searchFrom, it.searchTo, it.selectedBusType),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    private fun loadCompanies() {
        viewModelScope.launch {
            companyRepository.getApprovedCompanies().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val map = result.data.associateBy { it.id }
                        _uiState.update {
                            it.copy(companies = result.data, companiesMap = map)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    // ─── Search & Filter ─────────────────────────────────────

    fun searchRoutes(from: String, to: String) {
        _uiState.update {
            it.copy(
                searchFrom = from,
                searchTo = to,
                filteredRoutes = applyFilters(it.routes, from, to, it.selectedBusType)
            )
        }
    }

    fun filterByBusType(type: String?) {
        _uiState.update {
            it.copy(
                selectedBusType = type,
                filteredRoutes = applyFilters(it.routes, it.searchFrom, it.searchTo, type)
            )
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchFrom = "",
                searchTo = "",
                selectedBusType = null,
                filteredRoutes = it.routes
            )
        }
    }

    private fun applyFilters(
        routes: List<Route>,
        from: String,
        to: String,
        busType: String?
    ): List<Route> {
        var filtered = routes

        if (from.isNotBlank()) {
            filtered = filtered.filter {
                it.fromCity.contains(from, ignoreCase = true)
            }
        }
        if (to.isNotBlank()) {
            filtered = filtered.filter {
                it.toCity.contains(to, ignoreCase = true)
            }
        }
        // Bus type filtering will be applied at the schedule level in route details
        return filtered
    }

    // ─── Route Selection & Details ───────────────────────────

    fun selectRoute(route: Route) {
        _uiState.update {
            it.copy(
                selectedRoute = route,
                isLoadingDetails = true,
                detailsError = null,
                groupedByCompany = emptyList()
            )
        }
        loadRouteDetails(route.id)
    }

    private fun loadRouteDetails(routeId: String) {
        routeDetailsJob?.cancel()
        routeDetailsJob = viewModelScope.launch {
            scheduleRepository.getSchedulesByRouteRealtime(routeId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingDetails = true) }
                    }
                    is Resource.Success -> {
                        val schedules = result.data
                        // Fetch all unique bus IDs
                        val busIds = schedules.map { it.busId }.distinct()
                        val busMap = mutableMapOf<String, Bus>()
                        for (busId in busIds) {
                            if (busId.isNotEmpty()) {
                                val busResult = busRepository.getBusById(busId)
                                if (busResult is Resource.Success) {
                                    busMap[busId] = busResult.data
                                }
                            }
                        }

                        // Group by company
                        val grouped = buildCompanyGroups(schedules, busMap, _uiState.value.companiesMap)
                        val sorted = sortGroups(grouped, _uiState.value.sortOption)

                        _uiState.update {
                            it.copy(
                                isLoadingDetails = false,
                                routeSchedules = schedules,
                                routeBuses = busMap,
                                groupedByCompany = sorted,
                                detailsError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoadingDetails = false, detailsError = result.message)
                        }
                    }
                }
            }
        }
    }

    private fun buildCompanyGroups(
        schedules: List<Schedule>,
        busMap: Map<String, Bus>,
        companiesMap: Map<String, Company>
    ): List<CompanyBusGroup> {
        val byCompany = schedules.groupBy { it.companyId }

        return byCompany.mapNotNull { (companyId, companySchedules) ->
            val company = companiesMap[companyId] ?: Company(id = companyId, name = "Unknown Company")

            val schedulesWithBus = companySchedules.map { schedule ->
                ScheduleWithBus(
                    schedule = schedule,
                    bus = busMap[schedule.busId]
                )
            }

            val minFare = companySchedules.minOfOrNull { it.basePrice } ?: 0.0
            val earliest = companySchedules.minOfOrNull { it.departureTime } ?: 0L
            val totalSeats = companySchedules.sumOf { it.availableSeats }

            CompanyBusGroup(
                company = company,
                schedules = schedulesWithBus,
                minFare = minFare,
                earliestDeparture = earliest,
                totalAvailableSeats = totalSeats
            )
        }
    }

    // ─── Sorting ─────────────────────────────────────────────

    fun sortBy(option: SortOption) {
        _uiState.update {
            it.copy(
                sortOption = option,
                groupedByCompany = sortGroups(it.groupedByCompany, option)
            )
        }
    }

    private fun sortGroups(groups: List<CompanyBusGroup>, option: SortOption): List<CompanyBusGroup> {
        return when (option) {
            SortOption.DEPARTURE -> groups.sortedBy { it.earliestDeparture }
            SortOption.PRICE -> groups.sortedBy { it.minFare }
            SortOption.DURATION -> groups.sortedBy { it.earliestDeparture }
            SortOption.RATING -> groups.sortedByDescending { it.company.rating }
        }.map { group ->
            group.copy(
                schedules = when (option) {
                    SortOption.DEPARTURE -> group.schedules.sortedBy { it.schedule.departureTime }
                    SortOption.PRICE -> group.schedules.sortedBy { it.schedule.basePrice }
                    SortOption.DURATION -> group.schedules.sortedBy {
                        it.schedule.arrivalTime - it.schedule.departureTime
                    }
                    SortOption.RATING -> group.schedules
                }
            )
        }
    }

    // ─── Favorites ───────────────────────────────────────────

    fun toggleFavorite(routeId: String) {
        _uiState.update {
            val newFavorites = it.favorites.toMutableSet()
            if (newFavorites.contains(routeId)) newFavorites.remove(routeId)
            else newFavorites.add(routeId)
            it.copy(favorites = newFavorites)
        }
    }

    // ─── Refresh ─────────────────────────────────────────────

    fun refresh() {
        loadRoutes()
        loadCompanies()
    }

    fun clearRouteDetails() {
        routeDetailsJob?.cancel()
        _uiState.update {
            it.copy(
                selectedRoute = null,
                routeSchedules = emptyList(),
                routeBuses = emptyMap(),
                groupedByCompany = emptyList(),
                isLoadingDetails = false,
                detailsError = null
            )
        }
    }
}

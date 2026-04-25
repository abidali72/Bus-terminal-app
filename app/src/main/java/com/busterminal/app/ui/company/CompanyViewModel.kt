package com.busterminal.app.ui.company

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busterminal.app.data.model.Bus
import com.busterminal.app.data.model.Company
import com.busterminal.app.data.model.Driver
import com.busterminal.app.domain.repository.BusRepository
import com.busterminal.app.domain.repository.CompanyRepository
import com.busterminal.app.domain.repository.DriverRepository
import com.busterminal.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompanyUiState(
    val company: Company? = null,
    val buses: List<Bus> = emptyList(),
    val drivers: List<Driver> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionSuccess: String? = null
)

@HiltViewModel
class CompanyViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val busRepository: BusRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState())
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    fun loadCompanyData(userId: String) {
        viewModelScope.launch {
            companyRepository.getCompanyByOwnerId(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val company = result.data
                        _uiState.update { it.copy(company = company, isLoading = false) }
                        company?.let {
                            loadBuses(it.id)
                            loadDrivers(it.id)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadBuses(companyId: String) {
        viewModelScope.launch {
            busRepository.getBusesByCompany(companyId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(buses = result.data) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadDrivers(companyId: String) {
        viewModelScope.launch {
            driverRepository.getDriversByCompany(companyId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(drivers = result.data) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun addBus(bus: Bus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = busRepository.addBus(bus)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = "Bus added successfully") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun updateBus(bus: Bus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = busRepository.updateBus(bus)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = "Bus updated") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun updateBusStatus(busId: String, status: String) {
        viewModelScope.launch {
            busRepository.updateBusStatus(busId, status)
        }
    }

    fun deleteBus(busId: String) {
        viewModelScope.launch {
            busRepository.deleteBus(busId)
        }
    }

    fun addDriver(driver: Driver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = driverRepository.addDriver(driver)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, actionSuccess = "Driver added") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun updateDriver(driver: Driver) {
        viewModelScope.launch {
            driverRepository.updateDriver(driver)
        }
    }

    fun updateCompanyProfile(company: Company) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = companyRepository.updateCompanyProfile(company)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            company = company,
                            actionSuccess = "Company profile updated"
                        ) 
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun deleteDriver(driverId: String) {
        viewModelScope.launch {
            driverRepository.deleteDriver(driverId)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, actionSuccess = null) }
    }
}

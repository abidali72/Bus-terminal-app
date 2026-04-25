package com.busterminal.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busterminal.app.data.model.*
import com.busterminal.app.domain.repository.*
import com.busterminal.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val companies: List<Company> = emptyList(),
    val pendingCompanies: List<Company> = emptyList(),
    val allBuses: List<Bus> = emptyList(),
    val allDrivers: List<Driver> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionSuccess: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val companyRepository: CompanyRepository,
    private val busRepository: BusRepository,
    private val driverRepository: DriverRepository,
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            companyRepository.getAllCompanies().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                companies = result.data,
                                pendingCompanies = result.data.filter { c -> c.approvalStatus == "pending" }
                            )
                        }
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            busRepository.getAllBuses().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update { it.copy(allBuses = result.data, isLoading = false) }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            driverRepository.getAllDrivers().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update { it.copy(allDrivers = result.data) }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            announcementRepository.getAnnouncements().collect { result ->
                when (result) {
                    is Resource.Success -> _uiState.update { it.copy(announcements = result.data) }
                    else -> {}
                }
            }
        }
    }

    fun approveCompany(companyId: String) {
        viewModelScope.launch {
            when (companyRepository.approveCompany(companyId)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Company approved") }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to approve") }
                else -> {}
            }
        }
    }

    fun suspendCompany(companyId: String) {
        viewModelScope.launch {
            when (companyRepository.suspendCompany(companyId)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Company suspended") }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to suspend") }
                else -> {}
            }
        }
    }

    fun deleteBus(busId: String) {
        viewModelScope.launch { busRepository.deleteBus(busId) }
    }

    fun updateBusStatus(busId: String, status: String) {
        viewModelScope.launch { busRepository.updateBusStatus(busId, status) }
    }

    fun deleteDriver(driverId: String) {
        viewModelScope.launch { driverRepository.deleteDriver(driverId) }
    }

    fun postAnnouncement(title: String, message: String, adminName: String, adminId: String) {
        viewModelScope.launch {
            val announcement = Announcement(
                title = title,
                message = message,
                createdBy = adminId,
                createdByName = adminName
            )
            when (announcementRepository.postAnnouncement(announcement)) {
                is Resource.Success -> _uiState.update { it.copy(actionSuccess = "Announcement posted") }
                is Resource.Error -> _uiState.update { it.copy(error = "Failed to post") }
                else -> {}
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch { announcementRepository.deleteAnnouncement(id) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, actionSuccess = null) }
    }
}

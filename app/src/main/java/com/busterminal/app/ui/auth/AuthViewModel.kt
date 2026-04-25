package com.busterminal.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.busterminal.app.data.model.Company
import com.busterminal.app.data.model.User
import com.busterminal.app.domain.repository.AuthRepository
import com.busterminal.app.domain.repository.CompanyRepository
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val registrationSuccess: Boolean = false,
    val isGuest: Boolean = false,
    val actionSuccess: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (authRepository.isLoggedIn) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                when (val result = authRepository.getCurrentUser()) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentUser = result.data,
                                isLoggedIn = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.loginWithEmail(email, password)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = result.data,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun skipLogin() {
        _uiState.update {
            it.copy(
                isGuest = true,
                isLoggedIn = false,
                currentUser = null, // Guest has no user object
                error = null
            )
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        companyName: String = "",
        companyAddress: String = "",
        city: String = "",
        gender: String = "",
        dateOfBirth: String = "",
        ownerName: String = "",
        registrationNumber: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.registerWithEmail(
                name, email, phone, password, role, city, gender, dateOfBirth
            )) {
                is Resource.Success -> {
                    val user = result.data
                    // If registering as company, create company record
                    if (role == Constants.ROLE_COMPANY) {
                        val company = Company(
                            name = companyName.ifEmpty { name },
                            phone = phone,
                            email = email,
                            address = companyAddress,
                            city = city,
                            ownerName = ownerName,
                            registrationNumber = registrationNumber,
                            approvalStatus = Constants.STATUS_PENDING,
                            ownerId = user.id
                        )
                        companyRepository.createCompany(company)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoggedIn = true,
                            registrationSuccess = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.updateUserProfile(user)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            actionSuccess = "Profile updated successfully"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState() // Resets isGuest to false (default)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

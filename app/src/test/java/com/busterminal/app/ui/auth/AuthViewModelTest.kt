package com.busterminal.app.ui.auth

import com.busterminal.app.data.model.Company
import com.busterminal.app.data.model.User
import com.busterminal.app.domain.repository.AuthRepository
import com.busterminal.app.domain.repository.CompanyRepository
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeCompanyRepository: FakeCompanyRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeCompanyRepository = FakeCompanyRepository()
        viewModel = AuthViewModel(fakeAuthRepository, fakeCompanyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerAdmin_withInvalidInviteCode_failsWithSecurityError() = runTest {
        viewModel.register(
            name = "Admin User",
            email = "admin@example.com",
            phone = "1234567890",
            password = "password123",
            role = Constants.ROLE_ADMIN,
            adminCode = "WRONG_CODE"
        )

        val state = viewModel.uiState.value
        assertEquals("Invalid admin invite code", state.error)
        assertFalse(state.isLoggedIn)
        assertFalse(fakeAuthRepository.registerCalled)
    }

    @Test
    fun registerAdmin_withValidInviteCode_succeeds() = runTest {
        viewModel.register(
            name = "Admin User",
            email = "admin@example.com",
            phone = "1234567890",
            password = "password123",
            role = Constants.ROLE_ADMIN,
            adminCode = Constants.ADMIN_INVITE_CODE
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.error)
        assertTrue(state.isLoggedIn)
        assertTrue(fakeAuthRepository.registerCalled)
    }

    @Test
    fun registerPassenger_succeedsWithoutAdminCode() = runTest {
        viewModel.register(
            name = "Passenger User",
            email = "passenger@example.com",
            phone = "1234567890",
            password = "password123",
            role = Constants.ROLE_PASSENGER
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.error)
        assertTrue(state.isLoggedIn)
        assertTrue(fakeAuthRepository.registerCalled)
    }

    private class FakeAuthRepository : AuthRepository {
        var registerCalled = false
        override val currentUserId: String? = null
        override val isLoggedIn: Boolean = false

        override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
            return Resource.Error("Not implemented")
        }

        override suspend fun registerWithEmail(
            name: String,
            email: String,
            phone: String,
            password: String,
            role: String,
            city: String,
            gender: String,
            dateOfBirth: String
        ): Resource<User> {
            registerCalled = true
            val user = User(
                id = "user123",
                name = name,
                email = email,
                phone = phone,
                role = role
            )
            return Resource.Success(user)
        }

        override suspend fun updateUserProfile(user: User): Resource<Unit> {
            return Resource.Success(Unit)
        }

        override suspend fun getCurrentUser(): Resource<User> {
            return Resource.Error("No user")
        }

        override suspend fun logout() {}

        override fun observeAuthState(): Flow<Boolean> = flowOf(false)
    }

    private class FakeCompanyRepository : CompanyRepository {
        override fun getAllCompanies(): Flow<Resource<List<Company>>> = flowOf(Resource.Success(emptyList()))
        override fun getApprovedCompanies(): Flow<Resource<List<Company>>> = flowOf(Resource.Success(emptyList()))
        override fun getPendingCompanies(): Flow<Resource<List<Company>>> = flowOf(Resource.Success(emptyList()))
        override fun getCompanyByOwnerId(ownerId: String): Flow<Resource<Company?>> = flowOf(Resource.Success(null))
        override suspend fun getCompanyById(companyId: String): Resource<Company> = Resource.Error("Not found")
        override suspend fun createCompany(company: Company): Resource<Company> = Resource.Success(company)
        override suspend fun updateCompany(company: Company): Resource<Company> = Resource.Success(company)
        override suspend fun updateCompanyProfile(company: Company): Resource<Unit> = Resource.Success(Unit)
        override suspend fun approveCompany(companyId: String): Resource<Unit> = Resource.Success(Unit)
        override suspend fun suspendCompany(companyId: String): Resource<Unit> = Resource.Success(Unit)
        override suspend fun deleteCompany(companyId: String): Resource<Unit> = Resource.Success(Unit)
    }
}

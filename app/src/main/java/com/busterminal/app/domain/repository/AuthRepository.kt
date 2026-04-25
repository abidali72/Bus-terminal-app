package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.User
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isLoggedIn: Boolean
    suspend fun loginWithEmail(email: String, password: String): Resource<User>
    suspend fun registerWithEmail(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        city: String = "",
        gender: String = "",
        dateOfBirth: String = ""
    ): Resource<User>

    suspend fun updateUserProfile(user: User): Resource<Unit>
    suspend fun getCurrentUser(): Resource<User>
    suspend fun logout()
    fun observeAuthState(): Flow<Boolean>
}

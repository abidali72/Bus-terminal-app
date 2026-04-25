package com.busterminal.app.data.repository

import com.busterminal.app.data.model.User
import com.busterminal.app.domain.repository.AuthRepository
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Login failed")
            getCurrentUser()
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed", e)
        }
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
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed")

            val user = User(
                id = uid,
                name = name,
                email = email,
                phone = phone,
                role = role,
                city = city,
                gender = gender,
                dateOfBirth = dateOfBirth,
                status = "active"
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .set(user.toMap())
                .await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed", e)
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.id)
                .update(user.toMap())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Not logged in")
            val doc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = doc.toObject(User::class.java)
                ?: return Resource.Error("User not found")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user", e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

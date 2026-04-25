package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Route
import com.busterminal.app.domain.repository.RouteRepository
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RouteRepository {

    private val collection = firestore.collection(Constants.ROUTES_COLLECTION)

    override fun getAllRoutes(): Flow<Resource<List<Route>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed"))
                return@addSnapshotListener
            }
            try {
                val routes = snapshot?.toObjects(Route::class.java) ?: emptyList()
                trySend(Resource.Success(routes))
            } catch (e: Exception) {
                trySend(Resource.Error("Data error: ${e.message}"))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getActiveRoutes(): Flow<Resource<List<Route>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val routes = snapshot?.toObjects(Route::class.java) ?: emptyList()
                    trySend(Resource.Success(routes))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getRoutesByCompany(companyId: String): Flow<Resource<List<Route>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("companyId", companyId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val routes = snapshot?.toObjects(Route::class.java) ?: emptyList()
                    trySend(Resource.Success(routes))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getRouteById(routeId: String): Resource<Route> {
        return try {
            val doc = collection.document(routeId).get().await()
            val route = doc.toObject(Route::class.java) ?: return Resource.Error("Route not found")
            Resource.Success(route)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun addRoute(route: Route): Resource<Route> {
        return try {
            val docRef = collection.document()
            val newRoute = route.copy(id = docRef.id)
            docRef.set(newRoute.toMap()).await()
            Resource.Success(newRoute)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateRoute(route: Route): Resource<Route> {
        return try {
            collection.document(route.id).set(route.toMap()).await()
            Resource.Success(route)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteRoute(routeId: String): Resource<Unit> {
        return try {
            collection.document(routeId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun searchRoute(from: String, to: String): Resource<List<Route>> {
        return try {
            val snapshot = collection
                .whereEqualTo("fromCity", from)
                .whereEqualTo("toCity", to)
                .get()
                .await()
            val routes = snapshot.toObjects(Route::class.java)
            Resource.Success(routes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun softDeleteRoute(routeId: String): Resource<Unit> {
        return try {
            collection.document(routeId)
                .update(mapOf("status" to "inactive", "updatedAt" to System.currentTimeMillis()))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to deactivate route", e)
        }
    }

    override suspend fun checkDuplicateRoute(from: String, to: String): Resource<Boolean> {
        return try {
            val snapshot = collection
                .whereEqualTo("fromCity", from)
                .whereEqualTo("toCity", to)
                .get()
                .await()
            Resource.Success(snapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check duplicate", e)
        }
    }
}

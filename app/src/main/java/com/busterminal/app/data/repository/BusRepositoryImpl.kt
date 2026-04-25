package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Bus
import com.busterminal.app.domain.repository.BusRepository
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
class BusRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BusRepository {

    private val collection = firestore.collection(Constants.BUSES_COLLECTION)

    override fun getAllBuses(): Flow<Resource<List<Bus>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load buses"))
                    return@addSnapshotListener
                }
                try {
                    val buses = snapshot?.toObjects(Bus::class.java) ?: emptyList()
                    trySend(Resource.Success(buses))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getBusesByCompany(companyId: String): Flow<Resource<List<Bus>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("companyId", companyId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val buses = snapshot?.toObjects(Bus::class.java) ?: emptyList()
                    trySend(Resource.Success(buses))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getBusById(busId: String): Resource<Bus> {
        return try {
            val doc = collection.document(busId).get().await()
            val bus = doc.toObject(Bus::class.java)
                ?: return Resource.Error("Bus not found")
            Resource.Success(bus)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun addBus(bus: Bus): Resource<Bus> {
        return try {
            val docRef = collection.document()
            val newBus = bus.copy(id = docRef.id)
            docRef.set(newBus.toMap()).await()
            Resource.Success(newBus)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateBus(bus: Bus): Resource<Bus> {
        return try {
            val updated = bus.copy(updatedAt = System.currentTimeMillis())
            collection.document(bus.id).set(updated.toMap()).await()
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateBusStatus(busId: String, status: String): Resource<Unit> {
        return try {
            collection.document(busId)
                .update(mapOf("status" to status, "updatedAt" to System.currentTimeMillis()))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteBus(busId: String): Resource<Unit> {
        return try {
            collection.document(busId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override fun getActiveBuses(): Flow<Resource<List<Bus>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("status", Constants.BUS_ACTIVE)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val buses = snapshot?.toObjects(Bus::class.java) ?: emptyList()
                    trySend(Resource.Success(buses))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun softDeleteBus(busId: String): Resource<Unit> {
        return try {
            collection.document(busId)
                .update(mapOf("status" to Constants.BUS_DISABLED, "updatedAt" to System.currentTimeMillis()))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to disable bus", e)
        }
    }

    override suspend fun checkDuplicateBusNumber(busNumber: String): Resource<Boolean> {
        return try {
            val snapshot = collection
                .whereEqualTo("busNumber", busNumber)
                .get()
                .await()
            Resource.Success(snapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check duplicate", e)
        }
    }

    override fun getBusesByRoute(routeId: String): Flow<Resource<List<Bus>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("routeId", routeId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val buses = snapshot?.toObjects(Bus::class.java) ?: emptyList()
                    trySend(Resource.Success(buses))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }
}

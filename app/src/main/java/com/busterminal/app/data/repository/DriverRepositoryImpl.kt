package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Driver
import com.busterminal.app.domain.repository.DriverRepository
import com.busterminal.app.util.Constants
import com.busterminal.app.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DriverRepository {

    private val collection = firestore.collection(Constants.DRIVERS_COLLECTION)

    override fun getAllDrivers(): Flow<Resource<List<Driver>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed"))
                return@addSnapshotListener
            }
            try {
                val drivers = snapshot?.toObjects(Driver::class.java) ?: emptyList()
                trySend(Resource.Success(drivers))
            } catch (e: Exception) {
                trySend(Resource.Error("Data error: ${e.message}"))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getDriversByCompany(companyId: String): Flow<Resource<List<Driver>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val drivers = snapshot?.toObjects(Driver::class.java) ?: emptyList()
                    trySend(Resource.Success(drivers))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getDriverById(driverId: String): Resource<Driver> {
        return try {
            val doc = collection.document(driverId).get().await()
            val driver = doc.toObject(Driver::class.java) ?: return Resource.Error("Not found")
            Resource.Success(driver)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun addDriver(driver: Driver): Resource<Driver> {
        return try {
            val docRef = collection.document()
            val newDriver = driver.copy(id = docRef.id)
            docRef.set(newDriver.toMap()).await()
            Resource.Success(newDriver)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateDriver(driver: Driver): Resource<Driver> {
        return try {
            collection.document(driver.id).set(driver.toMap()).await()
            Resource.Success(driver)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteDriver(driverId: String): Resource<Unit> {
        return try {
            collection.document(driverId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun assignDriverToBus(driverId: String, busId: String): Resource<Unit> {
        return try {
            collection.document(driverId).update("assignedBusId", busId).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }
}

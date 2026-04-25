package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Schedule
import com.busterminal.app.domain.repository.ScheduleRepository
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
class ScheduleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ScheduleRepository {

    private val collection = firestore.collection(Constants.SCHEDULES_COLLECTION)

    override fun getAllSchedules(): Flow<Resource<List<Schedule>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed"))
                return@addSnapshotListener
            }
            try {
                val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                trySend(Resource.Success(schedules))
            } catch (e: Exception) {
                trySend(Resource.Error("Data error: ${e.message}"))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getSchedulesByCompany(companyId: String): Flow<Resource<List<Schedule>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("companyId", companyId)
            .orderBy("departureTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                    trySend(Resource.Success(schedules))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getSchedulesByRoute(routeId: String): Flow<Resource<List<Schedule>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("routeId", routeId)
            .orderBy("departureTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                    trySend(Resource.Success(schedules))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getSchedulesByRouteRealtime(routeId: String): Flow<Resource<List<Schedule>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("routeId", routeId)
            .whereEqualTo("status", "scheduled")
            .orderBy("departureTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                    trySend(Resource.Success(schedules))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getScheduleById(scheduleId: String): Resource<Schedule> {
        return try {
            val doc = collection.document(scheduleId).get().await()
            val schedule = doc.toObject(Schedule::class.java) ?: return Resource.Error("Schedule not found")
            Resource.Success(schedule)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun addSchedule(schedule: Schedule): Resource<Schedule> {
        return try {
            val docRef = collection.document()
            val newSchedule = schedule.copy(id = docRef.id)
            docRef.set(newSchedule.toMap()).await()
            Resource.Success(newSchedule)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateSchedule(schedule: Schedule): Resource<Schedule> {
        return try {
            collection.document(schedule.id).set(schedule.toMap()).await()
            Resource.Success(schedule)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteSchedule(scheduleId: String): Resource<Unit> {
        return try {
            collection.document(scheduleId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun cancelSchedule(scheduleId: String): Resource<Unit> {
        return try {
            collection.document(scheduleId)
                .update(mapOf("status" to "cancelled", "updatedAt" to System.currentTimeMillis()))
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel schedule", e)
        }
    }

    override suspend fun checkDuplicateSchedule(busId: String, departureTime: Long): Resource<Boolean> {
        return try {
            val snapshot = collection
                .whereEqualTo("busId", busId)
                .whereEqualTo("departureTime", departureTime)
                .get()
                .await()
            Resource.Success(snapshot.documents.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check duplicate", e)
        }
    }

    override fun getSchedulesByBus(busId: String): Flow<Resource<List<Schedule>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("busId", busId)
            .orderBy("departureTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                    trySend(Resource.Success(schedules))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    // This is a simplified search. In production, this would involve complex queries or cloud functions.
    override suspend fun searchSchedules(fromCity: String, toCity: String, dateInMillis: Long): Resource<List<Schedule>> {
        // Placeholder: Real implementation requires querying Routes then Schedules, or Denormalization.
         return Resource.Error("Search implementation requires UseCase logic")
    }
}

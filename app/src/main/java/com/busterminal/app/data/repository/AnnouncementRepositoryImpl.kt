package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Announcement
import com.busterminal.app.domain.repository.AnnouncementRepository
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
class AnnouncementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnnouncementRepository {

    private val collection = firestore.collection(Constants.ANNOUNCEMENTS_COLLECTION)

    override fun getAnnouncements(): Flow<Resource<List<Announcement>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val list = snapshot?.toObjects(Announcement::class.java) ?: emptyList()
                    trySend(Resource.Success(list))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun postAnnouncement(announcement: Announcement): Resource<Announcement> {
        return try {
            val docRef = collection.document()
            val newAnnouncement = announcement.copy(id = docRef.id)
            docRef.set(newAnnouncement.toMap()).await()
            Resource.Success(newAnnouncement)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteAnnouncement(announcementId: String): Resource<Unit> {
        return try {
            collection.document(announcementId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }
}

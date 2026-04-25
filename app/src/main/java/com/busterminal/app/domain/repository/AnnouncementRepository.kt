package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Announcement
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    fun getAnnouncements(): Flow<Resource<List<Announcement>>>
    suspend fun postAnnouncement(announcement: Announcement): Resource<Announcement>
    suspend fun deleteAnnouncement(announcementId: String): Resource<Unit>
}

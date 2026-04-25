package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Schedule
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getAllSchedules(): Flow<Resource<List<Schedule>>>
    fun getSchedulesByCompany(companyId: String): Flow<Resource<List<Schedule>>>
    fun getSchedulesByRoute(routeId: String): Flow<Resource<List<Schedule>>>
    fun getSchedulesByRouteRealtime(routeId: String): Flow<Resource<List<Schedule>>>
    suspend fun getScheduleById(scheduleId: String): Resource<Schedule>
    suspend fun addSchedule(schedule: Schedule): Resource<Schedule>
    suspend fun updateSchedule(schedule: Schedule): Resource<Schedule>
    suspend fun deleteSchedule(scheduleId: String): Resource<Unit>
    suspend fun cancelSchedule(scheduleId: String): Resource<Unit>
    suspend fun checkDuplicateSchedule(busId: String, departureTime: Long): Resource<Boolean>
    fun getSchedulesByBus(busId: String): Flow<Resource<List<Schedule>>>
    
    // Key search method for passengers
    suspend fun searchSchedules(fromCity: String, toCity: String, dateInMillis: Long): Resource<List<Schedule>>
}

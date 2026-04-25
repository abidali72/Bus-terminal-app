package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Bus
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface BusRepository {
    fun getAllBuses(): Flow<Resource<List<Bus>>>
    fun getBusesByCompany(companyId: String): Flow<Resource<List<Bus>>>
    suspend fun getBusById(busId: String): Resource<Bus>
    suspend fun addBus(bus: Bus): Resource<Bus>
    suspend fun updateBus(bus: Bus): Resource<Bus>
    suspend fun updateBusStatus(busId: String, status: String): Resource<Unit>
    suspend fun deleteBus(busId: String): Resource<Unit>
    suspend fun softDeleteBus(busId: String): Resource<Unit>
    suspend fun checkDuplicateBusNumber(busNumber: String): Resource<Boolean>
    fun getActiveBuses(): Flow<Resource<List<Bus>>>
    fun getBusesByRoute(routeId: String): Flow<Resource<List<Bus>>>
}

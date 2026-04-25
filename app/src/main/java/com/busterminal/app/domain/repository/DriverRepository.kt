package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Driver
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getAllDrivers(): Flow<Resource<List<Driver>>>
    fun getDriversByCompany(companyId: String): Flow<Resource<List<Driver>>>
    suspend fun getDriverById(driverId: String): Resource<Driver>
    suspend fun addDriver(driver: Driver): Resource<Driver>
    suspend fun updateDriver(driver: Driver): Resource<Driver>
    suspend fun deleteDriver(driverId: String): Resource<Unit>
    suspend fun assignDriverToBus(driverId: String, busId: String): Resource<Unit>
}

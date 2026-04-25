package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Route
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun getAllRoutes(): Flow<Resource<List<Route>>>
    fun getActiveRoutes(): Flow<Resource<List<Route>>>
    fun getRoutesByCompany(companyId: String): Flow<Resource<List<Route>>>
    suspend fun getRouteById(routeId: String): Resource<Route>
    suspend fun addRoute(route: Route): Resource<Route>
    suspend fun updateRoute(route: Route): Resource<Route>
    suspend fun deleteRoute(routeId: String): Resource<Unit>
    suspend fun softDeleteRoute(routeId: String): Resource<Unit>
    suspend fun searchRoute(from: String, to: String): Resource<List<Route>>
    suspend fun checkDuplicateRoute(from: String, to: String): Resource<Boolean>
}

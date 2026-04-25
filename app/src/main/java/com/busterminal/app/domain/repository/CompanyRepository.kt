package com.busterminal.app.domain.repository

import com.busterminal.app.data.model.Company
import com.busterminal.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun getAllCompanies(): Flow<Resource<List<Company>>>
    fun getApprovedCompanies(): Flow<Resource<List<Company>>>
    fun getPendingCompanies(): Flow<Resource<List<Company>>>
    fun getCompanyByOwnerId(ownerId: String): Flow<Resource<Company?>>
    suspend fun getCompanyById(companyId: String): Resource<Company>
    suspend fun createCompany(company: Company): Resource<Company>
    suspend fun updateCompany(company: Company): Resource<Company>
    suspend fun updateCompanyProfile(company: Company): Resource<Unit>
    suspend fun approveCompany(companyId: String): Resource<Unit>
    suspend fun suspendCompany(companyId: String): Resource<Unit>
    suspend fun deleteCompany(companyId: String): Resource<Unit>
}

package com.busterminal.app.data.repository

import com.busterminal.app.data.model.Company
import com.busterminal.app.domain.repository.CompanyRepository
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
class CompanyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CompanyRepository {

    private val collection = firestore.collection(Constants.COMPANIES_COLLECTION)

    override fun getAllCompanies(): Flow<Resource<List<Company>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load companies"))
                    return@addSnapshotListener
                }
                try {
                    val companies = snapshot?.toObjects(Company::class.java) ?: emptyList()
                    trySend(Resource.Success(companies))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getApprovedCompanies(): Flow<Resource<List<Company>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("approvalStatus", Constants.STATUS_APPROVED)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load companies"))
                    return@addSnapshotListener
                }
                try {
                    val companies = snapshot?.toObjects(Company::class.java) ?: emptyList()
                    trySend(Resource.Success(companies))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getPendingCompanies(): Flow<Resource<List<Company>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("approvalStatus", Constants.STATUS_PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val companies = snapshot?.toObjects(Company::class.java) ?: emptyList()
                    trySend(Resource.Success(companies))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getCompanyByOwnerId(ownerId: String): Flow<Resource<Company?>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = collection
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed"))
                    return@addSnapshotListener
                }
                try {
                    val company = snapshot?.toObjects(Company::class.java)?.firstOrNull()
                    trySend(Resource.Success(company))
                } catch (e: Exception) {
                    trySend(Resource.Error("Data error: ${e.message}"))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getCompanyById(companyId: String): Resource<Company> {
        return try {
            val doc = collection.document(companyId).get().await()
            val company = doc.toObject(Company::class.java)
                ?: return Resource.Error("Company not found")
            Resource.Success(company)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun createCompany(company: Company): Resource<Company> {
        return try {
            val docRef = collection.document()
            val newCompany = company.copy(id = docRef.id)
            docRef.set(newCompany.toMap()).await()
            Resource.Success(newCompany)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateCompany(company: Company): Resource<Company> {
        return try {
            collection.document(company.id).set(company.toMap()).await()
            Resource.Success(company)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun updateCompanyProfile(company: Company): Resource<Unit> {
        return try {
            collection.document(company.id)
                .update(company.toMap())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update company profile", e)
        }
    }

    override suspend fun approveCompany(companyId: String): Resource<Unit> {
        return try {
            collection.document(companyId)
                .update("approvalStatus", Constants.STATUS_APPROVED)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun suspendCompany(companyId: String): Resource<Unit> {
        return try {
            collection.document(companyId)
                .update("approvalStatus", Constants.STATUS_SUSPENDED)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }

    override suspend fun deleteCompany(companyId: String): Resource<Unit> {
        return try {
            collection.document(companyId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed", e)
        }
    }
}

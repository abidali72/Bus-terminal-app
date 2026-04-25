package com.busterminal.app.di

import com.busterminal.app.data.repository.*
import com.busterminal.app.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, firestore)

    @Provides
    @Singleton
    fun provideCompanyRepository(
        firestore: FirebaseFirestore
    ): CompanyRepository = CompanyRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideBusRepository(
        firestore: FirebaseFirestore
    ): BusRepository = BusRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideDriverRepository(
        firestore: FirebaseFirestore
    ): DriverRepository = DriverRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideAnnouncementRepository(
        firestore: FirebaseFirestore
    ): AnnouncementRepository = AnnouncementRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideRouteRepository(
        firestore: FirebaseFirestore
    ): RouteRepository = RouteRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideScheduleRepository(
        firestore: FirebaseFirestore
    ): ScheduleRepository = ScheduleRepositoryImpl(firestore)
}

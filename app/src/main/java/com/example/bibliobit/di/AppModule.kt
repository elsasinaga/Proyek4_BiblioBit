package com.example.bibliobit.di

import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.data.repository.AuthRepositoryImpl
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
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, firestore: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(auth, firestore) // Berikan kedua parameter: auth dan firestore
    }
}
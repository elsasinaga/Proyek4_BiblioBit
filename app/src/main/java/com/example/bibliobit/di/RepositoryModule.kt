package com.example.bibliobit.di

import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // Catatan: Repository lain seperti BookRepository, UserLibraryRepository, dll.
    // tidak perlu di-bind di sini karena mereka bukan interface dan sudah memiliki
    // anotasi @Inject constructor, sehingga Hilt sudah tahu cara membuatnya.
}
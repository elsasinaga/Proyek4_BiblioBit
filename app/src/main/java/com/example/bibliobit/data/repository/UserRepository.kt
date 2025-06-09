package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.LocalUser // Gunakan model ini
import com.example.bibliobit.data.remote.RemoteDataSource
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getProfile(): LocalUser {
        return remoteDataSource.getProfile()
    }
    suspend fun updateProfile(name: String, username: String): LocalUser {
        return remoteDataSource.updateProfile(name, username)
    }
    suspend fun updateProfileImage(imagePath: String?): LocalUser {
        return remoteDataSource.updateProfileImage(imagePath)
    }
}
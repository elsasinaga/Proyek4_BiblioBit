package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, username: String, name: String): Result<User>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun syncLocalUser(user: User) // Tambahkan untuk sinkronisasi LocalUser
}
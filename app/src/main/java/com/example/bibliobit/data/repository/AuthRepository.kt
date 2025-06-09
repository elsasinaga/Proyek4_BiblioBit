package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun resetPassword(email: String): Result<Unit>
    fun getCurrentUser(): User?
    fun logout()
}
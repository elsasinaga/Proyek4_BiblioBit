package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserLibraryRepository @Inject constructor(
    private val userLibraryDao: UserLibraryDao
) {
    suspend fun insert(userLibrary: UserLibrary) {
        userLibraryDao.insert(userLibrary)
    }

    suspend fun update(userLibrary: UserLibrary) {
        userLibraryDao.update(userLibrary)
    }

    fun getUserLibrary(userId: String): Flow<List<UserLibrary>> {
        return userLibraryDao.getUserLibrary(userId)
    }

    fun getUserLibraryByStatus(userId: String, status: BookStatus): Flow<List<UserLibrary>> {
        return userLibraryDao.getUserLibraryByStatus(userId, status)
    }

    fun searchUserLibrary(userId: String, query: String): Flow<List<UserLibrary>> {
        return userLibraryDao.searchUserLibrary(userId, query)
    }

    suspend fun getUserLibraryByBookId(userId: String, bookId: Long): UserLibrary? {
        return userLibraryDao.getUserLibraryByBookId(userId, bookId)
    }
}
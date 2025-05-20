package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

class UserLibraryRepository @Inject constructor(
    private val userLibraryDao: UserLibraryDao,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun insert(userLibrary: UserLibrary) {
        userLibraryDao.insert(userLibrary.copy(isSynced = false))
        syncUnsyncedUserLibrary()
    }

    suspend fun update(userLibrary: UserLibrary) {
        userLibraryDao.update(userLibrary.copy(isSynced = false))
        syncUnsyncedUserLibrary()
    }

    fun getUserLibrary(userId: String): Flow<List<UserLibrary>> =
        userLibraryDao.getUserLibrary(userId)

    fun getUserLibraryByStatus(userId: String, status: BookStatus): Flow<List<UserLibrary>> =
        userLibraryDao.getUserLibraryByStatus(userId, status)

    fun searchUserLibrary(userId: String, query: String): Flow<List<UserLibrary>> =
        userLibraryDao.searchUserLibrary(userId, query)

    suspend fun getUserLibraryByBookId(userId: String, bookId: Long): UserLibrary? =
        userLibraryDao.getUserLibraryByBookId(userId, bookId)

    suspend fun getUserLibraryById(id: Long): UserLibrary? =
        userLibraryDao.getUserLibraryById(id)

    suspend fun deleteUserLibrary(userId: String, bookId: Long) {
        userLibraryDao.deleteUserLibrary(userId, bookId)
        try {
            val userLibrary = userLibraryDao.getUserLibraryByBookId(userId, bookId)
            if (userLibrary != null) {
                remoteDataSource.deleteUserLibrary(userLibrary.id)
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUserLibraryStatus(userId: String, bookId: Long, status: BookStatus) {
        val userLibrary = getUserLibraryByBookId(userId, bookId)
        if (userLibrary != null) {
            val updatedUserLibrary = userLibrary.copy(status = status, isSynced = false)
            update(updatedUserLibrary)
        }
    }

    suspend fun syncUnsyncedUserLibrary() {
        try {
            val unsyncedUserLibrary = userLibraryDao.getUnsyncedUserLibrary()
            if (unsyncedUserLibrary.isNotEmpty()) {
                val syncedUserLibrary = remoteDataSource.syncUserLibrary(unsyncedUserLibrary)
                syncedUserLibrary.forEach { userLibrary ->
                    userLibraryDao.insert(userLibrary.copy(isSynced = true))
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun syncUserLibraryFromServer() {
        try {
            val serverUserLibrary = remoteDataSource.getUserLibrary()
            serverUserLibrary.forEach { userLibrary ->
                userLibraryDao.insert(userLibrary.copy(isSynced = true))
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
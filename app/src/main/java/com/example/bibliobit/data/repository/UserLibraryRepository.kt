package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.model.UserLibraryResponse
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.data.remote.UserLibraryRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toDate(): Date? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.parse(this)
    } catch (e: Exception) {
        Log.e("DateUtil", "Gagal parsing date: $this, ${e.message}")
        null
    }
}

class UserLibraryRepository @Inject constructor(
    private val userLibraryDao: UserLibraryDao,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun insert(userLibrary: UserLibrary) {
        try {
            val request = UserLibraryRequest(
                bookId = userLibrary.bookId,
                status = userLibrary.status.name,
                lastPageRead = userLibrary.lastPageRead,
                rating = userLibrary.rating
            )
            val response = remoteDataSource.updateOrCreateUserLibrary(request)
            val syncedEntry = UserLibrary(
                id = response.id,
                userId = response.userId,
                bookId = response.bookId,
                status = BookStatus.valueOf(response.status),
                lastPageRead = response.lastPageRead,
                updatedAt = response.updatedAt?.toDate() ?: Date(),
                rating = response.rating,
                createdAt = response.createdAt?.toDate(),
                isSynced = true,
                book = response.book
            )
            userLibraryDao.insert(syncedEntry)
            Log.d("UserLibraryRepository", "Berhasil menambahkan ke server: ${response.id}")
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Unknown error"
            Log.e("UserLibraryRepository", "HTTP Error: ${e.code()} - $errorBody")
            when (e.code()) {
                401 -> throw Exception("Unauthorized: Please log in again")
                422 -> throw Exception("Invalid data: Rating only allowed for FINISH status")
                else -> {
                    userLibraryDao.insert(userLibrary.copy(isSynced = false, updatedAt = Date()))
                    Log.e("UserLibraryRepository", "Gagal ke server, menyimpan lokal: ${e.message}")
                    syncUnsyncedUserLibrary()
                }
            }
        } catch (e: Exception) {
            Log.e("UserLibraryRepository", "Error: ${e.message}")
            userLibraryDao.insert(userLibrary.copy(isSynced = false, updatedAt = Date()))
            syncUnsyncedUserLibrary()
            throw e
        }
    }

    suspend fun update(userLibrary: UserLibrary) {
        try {
            userLibraryDao.update(userLibrary.copy(isSynced = true, updatedAt = Date()))
        } catch (e: HttpException) {
            userLibraryDao.update(userLibrary.copy(isSynced = false, updatedAt = Date()))
            syncUnsyncedUserLibrary()
        }
    }

    fun getUserLibrary(userId: String): Flow<List<UserLibrary>> =
        userLibraryDao.getUserLibrary(userId)

    fun getUserLibraryByStatus(userId: String, status: BookStatus): Flow<List<UserLibrary>> =
        userLibraryDao.getUserLibraryByStatus(userId, status.name)

    fun searchUserLibrary(userId: String, query: String): Flow<List<UserLibrary>> =
        userLibraryDao.searchUserLibrary(userId, query)

    suspend fun getUserLibraryByBookId(userId: String, bookId: Long): UserLibrary? =
        userLibraryDao.getUserLibraryByBookId(userId, bookId)

    suspend fun getUserLibraryById(id: Long): UserLibrary? =
        userLibraryDao.getUserLibraryById(id)

    suspend fun deleteUserLibrary(userId: String, bookId: Long) {
        try {
            val userLibrary = userLibraryDao.getUserLibraryByBookId(userId, bookId)
            if (userLibrary != null) {
                remoteDataSource.deleteUserLibrary(userLibrary.id)
                userLibraryDao.deleteUserLibrary(userId, bookId)
                Log.d("UserLibraryRepository", "Berhasil menghapus: bookId=$bookId")
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            Log.e("UserLibraryRepository", "Gagal menghapus: ${e.message}")
            throw e
        }
    }

    suspend fun updateUserLibraryStatus(userId: String, bookId: Long, status: BookStatus) {
        val userLibrary = getUserLibraryByBookId(userId, bookId)
        if (userLibrary != null) {
            val updatedUserLibrary = userLibrary.copy(status = status, isSynced = false, updatedAt = Date())
            update(updatedUserLibrary)
        }
    }

    suspend fun syncUnsyncedUserLibrary() {
        try {
            val unsyncedUserLibrary = userLibraryDao.getUnsyncedUserLibrary()
            if (unsyncedUserLibrary.isNotEmpty()) {
                unsyncedUserLibrary.forEach { entry ->
                    val request = UserLibraryRequest(
                        bookId = entry.bookId,
                        status = entry.status.name,
                        lastPageRead = entry.lastPageRead,
                        rating = entry.rating
                    )
                    val response = remoteDataSource.updateOrCreateUserLibrary(request)
                    val syncedEntry = UserLibrary(
                        id = response.id,
                        userId = response.userId,
                        bookId = response.bookId,
                        status = BookStatus.valueOf(response.status),
                        lastPageRead = response.lastPageRead,
                        updatedAt = response.updatedAt?.toDate() ?: Date(),
                        rating = response.rating,
                        createdAt = response.createdAt?.toDate(),
                        isSynced = true,
                        book = response.book
                    )
                    userLibraryDao.insert(syncedEntry)
                }
                Log.d("UserLibraryRepository", "Sinkronisasi berhasil: ${unsyncedUserLibrary.size} entri")
            }
        } catch (e: HttpException) {
            Log.e("UserLibraryRepository", "Gagal sinkronisasi: ${e.message}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            }
        } catch (e: Exception) {
            Log.e("UserLibraryRepository", "Error sinkronisasi: ${e.message}")
        }
    }

    suspend fun syncUserLibraryFromServer() {
        try {
            val serverUserLibrary = remoteDataSource.getUserLibrary()
            serverUserLibrary.forEach { response ->
                val entry = UserLibrary(
                    id = response.id,
                    userId = response.userId,
                    bookId = response.bookId,
                    status = BookStatus.valueOf(response.status),
                    lastPageRead = response.lastPageRead,
                    updatedAt = response.updatedAt?.toDate() ?: Date(),
                    rating = response.rating,
                    createdAt = response.createdAt?.toDate(),
                    isSynced = true,
                    book = response.book
                )
                userLibraryDao.insert(entry)
            }
            Log.d("UserLibraryRepository", "Sinkronisasi dari server berhasil")
        } catch (e: HttpException) {
            Log.e("UserLibraryRepository", "Gagal sinkronisasi dari server: ${e.message}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            }
        } catch (e: Exception) {
            Log.e("UserLibraryRepository", "Error: ${e.message}")
        }
    }
}
package com.example.bibliobit.data.repository

import com.example.bibliobit.data.mapper.toDomain
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.data.remote.request.UpdateUserLibraryRequest
import com.example.bibliobit.data.remote.request.UserLibraryRequest
import javax.inject.Inject

class UserLibraryRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun getUserLibrary(status: String? = null, query: String? = null): List<UserLibrary> {
        val response = remoteDataSource.getUserLibrary(status, query)
        return response.toDomain()
    }

    suspend fun upsertUserLibrary(entry: UserLibrary): UserLibrary {
        val response = if (entry.id == null) {
            val request = UserLibraryRequest(
                bookId = entry.bookId,
                status = entry.status.name
            )
            remoteDataSource.createUserLibrary(request)
        } else {
            val request = UpdateUserLibraryRequest(
                status = entry.status.name,
                lastPageRead = entry.lastPageRead,
                rating = entry.rating
            )
            remoteDataSource.updateUserLibrary(entry.id, request)
        }
        return response.toDomain()
    }

    /**
     * ## FUNGSI BARU ##
     * Menghapus entri library berdasarkan ID-nya.
     */
    suspend fun deleteUserLibrary(libraryId: Long) {
        remoteDataSource.deleteUserLibrary(libraryId)
    }

    suspend fun getUserLibraryById(id: Long): UserLibrary {
        val response = remoteDataSource.getUserLibraryById(id)
        return response.toDomain()
    }
}
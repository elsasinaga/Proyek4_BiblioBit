package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

class ReadingProgressRepository @Inject constructor(
    private val readingProgressDao: ReadingProgressDao,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun insert(readingProgress: ReadingProgress) {
        readingProgressDao.insert(readingProgress.copy(isSynced = false))
        syncUnsyncedReadingProgress()
    }

    fun getReadingProgressByUserLibraryId(userLibraryId: Long): Flow<List<ReadingProgress>> =
        readingProgressDao.getReadingProgressByUserLibraryId(userLibraryId)

    suspend fun getFirstReadingProgress(userLibraryId: Long): ReadingProgress? =
        readingProgressDao.getFirstReadingProgress(userLibraryId)

    fun getReadingProgressByUserId(userId: String): Flow<List<ReadingProgress>> =
        readingProgressDao.getReadingProgressByUserId(userId)

    suspend fun deleteReadingProgressByUserLibraryId(userLibraryId: Long) {
        readingProgressDao.deleteReadingProgressByUserLibraryId(userLibraryId)
        // Sinkronkan penghapusan ke server jika diperlukan
    }

    suspend fun syncUnsyncedReadingProgress() {
        try {
            val unsyncedProgress = readingProgressDao.getUnsyncedReadingProgress()
            if (unsyncedProgress.isNotEmpty()) {
                val syncedProgress = remoteDataSource.syncReadingProgress(unsyncedProgress)
                syncedProgress.forEach { progress ->
                    readingProgressDao.insert(progress.copy(isSynced = true))
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

    suspend fun syncReadingProgressFromServer() {
        try {
            val serverProgress = remoteDataSource.getReadingProgress()
            serverProgress.forEach { progress ->
                readingProgressDao.insert(progress.copy(isSynced = true))
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
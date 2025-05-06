package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.ReadingProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingProgressRepository @Inject constructor(
    private val readingProgressDao: ReadingProgressDao
) {
    suspend fun insert(readingProgress: ReadingProgress) {
        readingProgressDao.insert(readingProgress)
    }

    fun getReadingProgressByUserLibraryId(userLibraryId: Long): Flow<List<ReadingProgress>> {
        return readingProgressDao.getReadingProgressByUserLibraryId(userLibraryId)
    }

    suspend fun getFirstReadingProgress(userLibraryId: Long): ReadingProgress? {
        return readingProgressDao.getFirstReadingProgress(userLibraryId)
    }

    fun getReadingProgressByUserId(userId: String): Flow<List<ReadingProgress>> {
        return readingProgressDao.getReadingProgressByUserId(userId)
    }

    suspend fun deleteReadingProgressByUserLibraryId(userLibraryId: Long) {
        readingProgressDao.deleteReadingProgressByUserLibraryId(userLibraryId)
    }
}
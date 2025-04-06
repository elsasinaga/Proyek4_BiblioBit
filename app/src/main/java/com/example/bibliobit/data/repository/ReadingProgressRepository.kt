package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.ReadingProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingProgressRepository @Inject constructor(
    private val readingProgressDao: ReadingProgressDao
) {
    suspend fun insert(readingProgress: ReadingProgress) {
        println("Inserting ReadingProgress: $readingProgress")
        readingProgressDao.insert(readingProgress)
        println("Inserted ReadingProgress successfully")
    }

    fun getReadingProgressByUserLibraryId(userLibraryId: Long): Flow<List<ReadingProgress>> {
        println("Fetching ReadingProgress for userLibraryId: $userLibraryId")
        return readingProgressDao.getReadingProgressByUserLibraryId(userLibraryId)
    }

    suspend fun getFirstReadingProgress(userLibraryId: Long): ReadingProgress? {
        println("Fetching First ReadingProgress for userLibraryId: $userLibraryId")
        val firstProgress = readingProgressDao.getFirstReadingProgress(userLibraryId)
        println("First ReadingProgress: $firstProgress")
        return firstProgress
    }
}
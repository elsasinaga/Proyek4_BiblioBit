package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bibliobit.data.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(readingProgress: ReadingProgress)

    @Query("SELECT * FROM reading_progress WHERE user_library_id = :userLibraryId ORDER BY recorded_at ASC")
    fun getReadingProgressByUserLibraryId(userLibraryId: Long): Flow<List<ReadingProgress>>

    @Query("SELECT * FROM reading_progress WHERE user_library_id = :userLibraryId ORDER BY recorded_at ASC LIMIT 1")
    suspend fun getFirstReadingProgress(userLibraryId: Long): ReadingProgress?
}
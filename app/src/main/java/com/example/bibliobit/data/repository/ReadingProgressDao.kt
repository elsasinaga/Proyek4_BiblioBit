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

    // Query baru untuk mengambil semua ReadingProgress berdasarkan userId
    @Query("""
        SELECT rp.* FROM reading_progress rp
        INNER JOIN user_library ul ON rp.user_library_id = ul.id
        WHERE ul.userId = :userId
        ORDER BY rp.recorded_at ASC
    """)
    fun getReadingProgressByUserId(userId: String): Flow<List<ReadingProgress>>
}
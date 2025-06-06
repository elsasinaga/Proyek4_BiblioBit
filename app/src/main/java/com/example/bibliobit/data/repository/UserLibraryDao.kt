package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLibraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLibrary: UserLibrary)

    @Update
    suspend fun update(userLibrary: UserLibrary)

    @Query("SELECT * FROM user_library WHERE userId = :userId")
    fun getUserLibrary(userId: String): Flow<List<UserLibrary>>

    @Query("SELECT * FROM user_library WHERE userId = :userId AND status = :status")
    fun getUserLibraryByStatus(userId: String, status: String): Flow<List<UserLibrary>>

    @Query("SELECT * FROM user_library WHERE userId = :userId AND (SELECT title FROM books WHERE books.id = user_library.bookId) LIKE '%' || :query || '%' OR (SELECT author FROM books WHERE books.id = user_library.bookId) LIKE '%' || :query || '%'")
    fun searchUserLibrary(userId: String, query: String): Flow<List<UserLibrary>>

    @Query("SELECT * FROM user_library WHERE userId = :userId AND bookId = :bookId LIMIT 1")
    suspend fun getUserLibraryByBookId(userId: String, bookId: Long): UserLibrary?

    @Query("SELECT * FROM user_library WHERE id = :id LIMIT 1")
    suspend fun getUserLibraryById(id: Long): UserLibrary?

    @Query("DELETE FROM user_library WHERE userId = :userId AND bookId = :bookId")
    suspend fun deleteUserLibrary(userId: String, bookId: Long)

    @Query("SELECT * FROM user_library WHERE isSynced = 0")
    suspend fun getUnsyncedUserLibrary(): List<UserLibrary>
}
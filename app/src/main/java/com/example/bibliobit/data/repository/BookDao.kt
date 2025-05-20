package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.bibliobit.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isSynced = 0")
    suspend fun getUnsyncedBooks(): List<Book>
}
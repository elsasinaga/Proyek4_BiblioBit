package com.example.bibliobit.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bibliobit.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: Book)

    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<Book?>
}
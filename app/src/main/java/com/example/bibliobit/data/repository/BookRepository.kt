package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bookDao: BookDao
) {
    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }

    fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks()
    }

    fun getBookById(bookId: Long): Flow<Book?> {
        return bookDao.getBookById(bookId)
    }

    fun searchBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query)
    }
}
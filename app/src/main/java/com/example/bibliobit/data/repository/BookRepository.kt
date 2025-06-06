package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun insert(book: Book): Long {
        return try {
            println("Inserting book to server: $book")
            val responseBook = remoteDataSource.createBook(book.copy(isSynced = false))
            println("Server response for book insert: $responseBook")
            val localId = if (responseBook.id > 0) {
                bookDao.insertBook(responseBook.copy(isSynced = true))
                println("Inserted book to local DB (synced): $responseBook")
                responseBook.id.toLong()
            } else {
                bookDao.insertBook(book.copy(isSynced = false))
                println("Inserted book to local DB (unsynced): $book")
                book.id.toLong()
            }
            localId
        } catch (e: HttpException) {
            println("HTTP error inserting book: ${e.code()} - ${e.message}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                val localId = bookDao.insertBook(book.copy(isSynced = false))
                println("Inserted book locally (unsynced) due to HTTP error: $book")
                syncUnsyncedBooks()
                localId
            }
        } catch (e: Exception) {
            println("General error inserting book: ${e.message}")
            val localId = bookDao.insertBook(book.copy(isSynced = false))
            println("Inserted book locally (unsynced) due to error: $book")
            throw e
        }
    }

    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    fun getBookById(bookId: Long): Flow<Book?> = bookDao.getBookById(bookId)

    fun searchBooks(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    suspend fun syncUnsyncedBooks() {
        try {
            val unsyncedBooks = bookDao.getUnsyncedBooks()
            println("Found ${unsyncedBooks.size} unsynced books: $unsyncedBooks")
            if (unsyncedBooks.isNotEmpty()) {
                val syncedBooks = remoteDataSource.syncBooks(unsyncedBooks)
                println("Synced ${syncedBooks.size} books with server")
                syncedBooks.forEach { book ->
                    bookDao.insertBook(book.copy(isSynced = true))
                    println("Updated local DB with synced book: $book")
                }
            }
        } catch (e: HttpException) {
            println("HTTP error syncing unsynced books: ${e.code()} - ${e.message}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            println("General error syncing unsynced books: ${e.message}")
            throw e
        }
    }

    suspend fun syncBooksFromServer() {
        try {
            println("Fetching books from server...")
            val serverBooks = remoteDataSource.getBooks()
            println("Fetched ${serverBooks.size} books from server: $serverBooks")
            serverBooks.forEach { book ->
                bookDao.insertBook(book.copy(isSynced = true))
                println("Inserted book to local DB: $book")
            }
        } catch (e: HttpException) {
            println("HTTP error fetching books: ${e.code()} - ${e.message}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                throw e
            }
        } catch (e: Exception) {
            println("General error fetching books: ${e.message}")
            throw e
        }
    }
}
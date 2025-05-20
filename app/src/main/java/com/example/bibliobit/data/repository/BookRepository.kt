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
    suspend fun insertBook(book: Book): Long {
        return try {
            // Simpan ke server terlebih dahulu
            val responseBook = remoteDataSource.createBook(book.copy(isSynced = false))
            // Jika berhasil, simpan ke lokal dengan ID dari server (jika ada)
            val localId = if (responseBook.id > 0) {
                bookDao.insertBook(responseBook.copy(isSynced = true))
                responseBook.id.toLong()
            } else {
                bookDao.insertBook(book.copy(isSynced = false))
            }
            localId
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please log in again")
            } else {
                // Jika gagal ke server, simpan lokal saja dan tandai unsynced
                val localId = bookDao.insertBook(book.copy(isSynced = false))
                syncUnsyncedBooks() // Coba sinkronisasi nanti
                localId
            }
        } catch (e: Exception) {
            // Jika error lain, simpan lokal dan lempar exception
            val localId = bookDao.insertBook(book.copy(isSynced = false))
            throw e
        }
    }

    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    fun getBookById(bookId: Long): Flow<Book?> = bookDao.getBookById(bookId)

    fun searchBooks(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    suspend fun syncUnsyncedBooks() {
        try {
            val unsyncedBooks = bookDao.getUnsyncedBooks()
            if (unsyncedBooks.isNotEmpty()) {
                val syncedBooks = remoteDataSource.syncBooks(unsyncedBooks)
                syncedBooks.forEach { book ->
                    bookDao.insertBook(book.copy(isSynced = true))
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

    suspend fun syncBooksFromServer() {
        try {
            val serverBooks = remoteDataSource.getBooks()
            serverBooks.forEach { book ->
                bookDao.insertBook(book.copy(isSynced = true))
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
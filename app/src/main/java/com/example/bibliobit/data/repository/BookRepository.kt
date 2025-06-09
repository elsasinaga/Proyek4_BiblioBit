package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.remote.RemoteDataSource
import javax.inject.Inject

/**
 * BookRepository final untuk arsitektur online-only.
 * - Menambahkan fungsi `createBook` untuk menyimpan buku baru ke server.
 * - Menambahkan fungsi `searchBooks` untuk melakukan pencarian.
 */
class BookRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    /**
     * Mengambil semua buku dari server.
     */
    suspend fun getAllBooks(): List<Book> {
        return remoteDataSource.getBooks()
    }

    /**
     * Mengambil detail satu buku berdasarkan ID dari server.
     */
    suspend fun getBookById(bookId: Long): Book {
        return remoteDataSource.getBook(bookId)
    }

    /**
     * **FUNGSI BARU UNTUK MENGATASI ERROR `createBook`**
     * Mengirim data buku baru ke server.
     */
    suspend fun createBook(book: Book): Book {
        return remoteDataSource.createBook(book)
    }

    suspend fun searchBooks(query: String): List<Book> {
        // Ambil semua buku dari server
        val allBooks = remoteDataSource.getBooks()
        // Filter di sisi klien berdasarkan judul atau penulis
        return allBooks.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
    }
}
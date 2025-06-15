package com.example.bibliobit.data.repository

import com.example.bibliobit.data.model.GoogleBook
import com.example.bibliobit.data.remote.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookSearchRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    suspend fun searchBooks(query: String): Result<List<GoogleBook>> {
        return withContext(Dispatchers.IO) {
            try {
                val books = remoteDataSource.searchGoogleBooks(query)
                Result.success(books)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun findBookByIsbn(isbn: String): Result<GoogleBook> {
        return withContext(Dispatchers.IO) {
            try {
                val book = remoteDataSource.findGoogleBookByIsbn(isbn)
                Result.success(book)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

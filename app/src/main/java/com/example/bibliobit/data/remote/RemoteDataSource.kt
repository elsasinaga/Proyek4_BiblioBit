package com.example.bibliobit.data.remote

import com.example.bibliobit.data.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth
) {
    private suspend fun <T> handleResponse(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Response body is null")
        } else {
            throw HttpException(response)
        }
    }

    // Book
    suspend fun getBooks(): List<Book> = handleResponse(apiService.getBooks())
    suspend fun createBook(book: Book): Book = handleResponse(apiService.createBook(book))
    suspend fun updateBook(id: Long, book: Book): Book = handleResponse(apiService.updateBook(id, book))
    suspend fun deleteBook(id: Long) = handleResponse(apiService.deleteBook(id))
    suspend fun syncBooks(books: List<Book>): List<Book> = handleResponse(apiService.syncBooks(books))
    // RemoteDataSource
    suspend fun getBookById(id: Long): Book = handleResponse(apiService.getBookById(id))


    // UserLibrary
    suspend fun getUserLibrary(status: String? = null, query: String? = null): List<UserLibrary> =
        handleResponse(apiService.getUserLibrary(status, query))
    suspend fun createUserLibrary(userLibrary: UserLibrary): UserLibrary =
        handleResponse(apiService.createUserLibrary(userLibrary))
    suspend fun updateUserLibrary(id: Long, userLibrary: UserLibrary): UserLibrary =
        handleResponse(apiService.updateUserLibrary(id, userLibrary))
    suspend fun deleteUserLibrary(id: Long) = handleResponse(apiService.deleteUserLibrary(id))
    suspend fun syncUserLibrary(userLibraries: List<UserLibrary>): List<UserLibrary> =
        handleResponse(apiService.syncUserLibrary(userLibraries))

    // LocalUser
    suspend fun getLocalUsers(): List<LocalUser> = handleResponse(apiService.getLocalUsers())
    suspend fun createLocalUser(localUser: LocalUser): LocalUser =
        handleResponse(apiService.createLocalUser(localUser))
    suspend fun updateLocalUser(uid: String, localUser: LocalUser): LocalUser =
        handleResponse(apiService.updateLocalUser(uid, localUser))
    suspend fun syncLocalUsers(localUsers: List<LocalUser>): List<LocalUser> =
        handleResponse(apiService.syncLocalUsers(localUsers))

    // ReadingProgress
    suspend fun getReadingProgress(userLibraryId: Long? = null): List<ReadingProgress> =
        handleResponse(apiService.getReadingProgress(userLibraryId))
    suspend fun createReadingProgress(readingProgress: ReadingProgress): ReadingProgress =
        handleResponse(apiService.createReadingProgress(readingProgress))
    suspend fun syncReadingProgress(readingProgressList: List<ReadingProgress>): List<ReadingProgress> =
        handleResponse(apiService.syncReadingProgress(readingProgressList))

    suspend fun getReadingBooks(userId: String): List<UserLibrary> =
        handleResponse(apiService.getReadingBooks(userId))

    // Note
    suspend fun getNotes(userLibraryId: Long? = null): List<Note> =
        handleResponse(apiService.getNotes(userLibraryId))
    suspend fun createNote(note: Note): Note = handleResponse(apiService.createNote(note))
    suspend fun updateNote(id: Long, note: Note): Note = handleResponse(apiService.updateNote(id, note))
    suspend fun deleteNote(id: Long) = handleResponse(apiService.deleteNote(id))
    suspend fun syncNotes(notes: List<Note>): List<Note> =
        handleResponse(apiService.syncNotes(notes))
}
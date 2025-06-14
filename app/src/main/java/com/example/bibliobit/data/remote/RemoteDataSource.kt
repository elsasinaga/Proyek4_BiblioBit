package com.example.bibliobit.data.remote

import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.remote.request.UpdateUserLibraryRequest
import com.example.bibliobit.data.remote.request.UserLibraryRequest
import com.example.bibliobit.data.remote.response.UserLibraryResponse
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    firebaseAuth: FirebaseAuth
) {
    private suspend fun <T> handleResponse(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: (Unit as T)
        } else {
            throw HttpException(response)
        }
    }

    // Book
    suspend fun getBooks(): List<Book> = handleResponse(apiService.getBooks())
    suspend fun getBook(id: Long): Book = handleResponse(apiService.getBookById(id))
    suspend fun createBook(book: Book): Book = handleResponse(apiService.createBook(book))

    // UserLibrary
    suspend fun getUserLibrary(status: String?, query: String?): List<UserLibraryResponse> =
        handleResponse(apiService.getUserLibrary(status, query))

    suspend fun createUserLibrary(request: UserLibraryRequest): UserLibraryResponse =
        handleResponse(apiService.createUserLibrary(request))

    suspend fun updateUserLibrary(id: Long, request: UpdateUserLibraryRequest): UserLibraryResponse =
        handleResponse(apiService.updateUserLibrary(id, request))

    suspend fun deleteUserLibrary(id: Long) = handleResponse(apiService.deleteUserLibrary(id))

    suspend fun getUserLibraryById(id: Long): UserLibraryResponse =
        handleResponse(apiService.getUserLibraryById(id))

    // --- Reading Progress ---
    suspend fun getReadingProgress(userLibraryId: Long): List<ReadingProgress> =
        handleResponse(apiService.getReadingProgress(userLibraryId))

    suspend fun createReadingProgress(readingProgress: ReadingProgress): ReadingProgress =
        handleResponse(apiService.createReadingProgress(readingProgress))

    // --- Profile ---
    suspend fun getProfile(): LocalUser = handleResponse(apiService.getProfile())

    suspend fun updateProfile(name: String, username: String): LocalUser {
        val requestBody = mapOf("name" to name, "username" to username)
        return handleResponse(apiService.updateProfile(requestBody))
    }

    suspend fun updateProfileImage(imagePath: String?): LocalUser {
        val requestBody = mapOf("profile_image" to imagePath)
        return handleResponse(apiService.updateProfileImage(requestBody))
    }

    suspend fun getAllReadingProgress(): List<ReadingProgress> =
        handleResponse(apiService.getAllReadingProgress())

    suspend fun getNotes(userLibraryId: Long): List<Note> {
        return apiService.getNotes(userLibraryId)
    }

    suspend fun createNote(userLibraryId: Long, content: RequestBody, image: MultipartBody.Part?): Note {
        return apiService.createNote(userLibraryId, content, image)
    }

    suspend fun updateNote(noteId: Long, content: RequestBody, image: MultipartBody.Part?): Note {
        return apiService.updateNote(noteId, content, image)
    }

    suspend fun deleteNote(noteId: Long) {
        handleResponse(apiService.deleteNote(noteId))
    }
}
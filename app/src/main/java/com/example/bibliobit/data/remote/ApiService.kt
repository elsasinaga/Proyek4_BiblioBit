package com.example.bibliobit.data.remote

import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.remote.request.UpdateUserLibraryRequest
import com.example.bibliobit.data.remote.request.UserLibraryRequest
import com.example.bibliobit.data.remote.response.UserLibraryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Books ---
    @GET("api/books")
    suspend fun getBooks(): Response<List<Book>>

    @GET("api/books/{id}")
    suspend fun getBookById(@Path("id") id: Long): Response<Book>

    @POST("api/books")
    suspend fun createBook(@Body book: Book): Response<Book>

    // --- User Library ---
    @GET("api/user-library")
    suspend fun getUserLibrary(
        @Query("status") status: String? = null,
        @Query("query") query: String? = null
    ): Response<List<UserLibraryResponse>>

    @DELETE("api/user-library/{id}")
    suspend fun deleteUserLibrary(@Path("id") id: Long): Response<Unit>

    @POST("api/user-library")
    suspend fun createUserLibrary(@Body request: UserLibraryRequest): Response<UserLibraryResponse>

    @PUT("api/user-library/{id}")
    suspend fun updateUserLibrary(
        @Path("id") id: Long,
        @Body request: UpdateUserLibraryRequest
    ): Response<UserLibraryResponse>

    @GET("api/user-library/{id}")
    suspend fun getUserLibraryById(@Path("id") id: Long): Response<UserLibraryResponse>

    // --- Reading Progress ---
    @GET("api/reading-progress")
    suspend fun getReadingProgress(
        @Query("user_library_id") userLibraryId: Long
    ): Response<List<ReadingProgress>>

    @POST("api/reading-progress")
    suspend fun createReadingProgress(@Body readingProgress: ReadingProgress): Response<ReadingProgress>

    // --- Profile ---
    @GET("api/profile")
    suspend fun getProfile(): Response<LocalUser>

    @PUT("api/profile")
    suspend fun updateProfile(@Body profileUpdateRequest: Map<String, String>): Response<LocalUser>

    @PUT("api/profile")
    suspend fun updateProfileImage(@Body profileImageRequest: Map<String, String?>): Response<LocalUser>

    @GET("api/reading-progress")
    suspend fun getAllReadingProgress(): Response<List<ReadingProgress>>

    // --- Notes ---
    @GET("api/user-library/{userLibraryId}/notes")
    suspend fun getNotes(@Path("userLibraryId") userLibraryId: Long): List<Note>

    @Multipart
    @POST("api/user-library/{userLibraryId}/notes")
    suspend fun createNote(
        @Path("userLibraryId") userLibraryId: Long,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Note

    @Multipart
    @POST("api/notes/{noteId}")
    suspend fun updateNote(
        @Path("noteId") noteId: Long,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Note

    @DELETE("api/notes/{noteId}")
    suspend fun deleteNote(@Path("noteId") noteId: Long): Response<Unit>
}
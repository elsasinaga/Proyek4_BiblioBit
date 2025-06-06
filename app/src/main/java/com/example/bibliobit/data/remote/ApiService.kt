package com.example.bibliobit.data.remote

import com.example.bibliobit.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ------------------ Book ------------------ //
    @GET("books")
    suspend fun getBooks(): Response<List<Book>>

    @POST("books")
    suspend fun createBook(@Body book: Book): Response<Book>

    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: Long, @Body book: Book): Response<Book>

    @PUT("books/{id}/status")
    suspend fun updateBookStatus(
        @Path("id") bookId: Long,
        @Body status: BookStatus // Ganti StatusUpdateRequest dengan BookStatus
    ): Response<String>

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Long): Response<Unit>

    @POST("sync/books") // Tidak ada 'api/' sebelumnya, jadi tetap
    suspend fun syncBooks(@Body books: List<Book>): Response<List<Book>>

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: Long): Response<Book>

    // ------------------ User Library ------------------ //
    @GET("user-library") // Tidak ada 'api/' sebelumnya, jadi tetap
    suspend fun getUserLibrary(
        @Query("status") status: String? = null,
        @Query("query") query: String? = null
    ): Response<List<UserLibraryResponse>>

    @GET("user-library/{id}") // Dihilangkan 'api/'
    suspend fun getUserLibraryById(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<UserLibraryResponse>

    @GET("user-library/reading") // Dihilangkan 'api/'
    suspend fun getReadingBooks(
        @Query("userId") userId: String? = null
    ): Response<List<UserLibraryResponse>>

    @POST("user-library") // Dihilangkan 'api/'
    suspend fun updateOrCreateUserLibrary(@Body request: UserLibraryRequest): Response<UserLibraryResponse>

    @POST("user-library") // Dihilangkan 'api/'
    suspend fun createUserLibrary(@Body userLibrary: UserLibraryResponse): Response<UserLibraryResponse>

    @PUT("user-library/{id}") // Dihilangkan 'api/'
    suspend fun updateUserLibrary(
        @Path("id") id: Long,
        @Body userLibrary: UserLibraryResponse
    ): Response<UserLibraryResponse>

    @DELETE("user-library/{id}") // Dihilangkan 'api/'
    suspend fun deleteUserLibrary(@Path("id") id: Long): Response<Unit>

    @POST("sync/user-library") // Dihilangkan 'api/'
    suspend fun syncUserLibrary(@Body userLibraries: List<UserLibraryResponse>): Response<List<UserLibraryResponse>>

    // ------------------ Local User ------------------ //
    @GET("local-users") // Dihilangkan 'api/'
    suspend fun getLocalUsers(): Response<List<LocalUser>>

    @POST("local-users") // Dihilangkan 'api/'
    suspend fun createLocalUser(@Body localUser: LocalUser): Response<LocalUser>

    @PUT("local-users/{uid}") // Dihilangkan 'api/'
    suspend fun updateLocalUser(
        @Path("uid") uid: String,
        @Body localUser: LocalUser
    ): Response<LocalUser>

    @POST("sync/local-users") // Dihilangkan 'api/'
    suspend fun syncLocalUsers(@Body localUsers: List<LocalUser>): Response<List<LocalUser>>

    // ------------------ Reading Progress ------------------ //
    @GET("reading-progress") // Dihilangkan 'api/'
    suspend fun getReadingProgress(
        @Query("user_library_id") userLibraryId: Long? = null
    ): Response<List<ReadingProgress>>

    @POST("reading-progress") // Dihilangkan 'api/'
    suspend fun createReadingProgress(@Body readingProgress: ReadingProgress): Response<ReadingProgress>

    @POST("sync/reading-progress") // Dihilangkan 'api/'
    suspend fun syncReadingProgress(@Body readingProgressList: List<ReadingProgress>): Response<List<ReadingProgress>>

    // ------------------ Note ------------------ //
    @GET("notes") // Dihilangkan 'api/'
    suspend fun getNotes(@Query("user_library_id") userLibraryId: Long? = null): Response<List<Note>>

    @POST("notes") // Dihilangkan 'api/'
    suspend fun createNote(@Body note: Note): Response<Note>

    @PUT("notes/{id}") // Dihilangkan 'api/'
    suspend fun updateNote(@Path("id") id: Long, @Body note: Note): Response<Note>

    @DELETE("notes/{id}") // Dihilangkan 'api/'
    suspend fun deleteNote(@Path("id") id: Long): Response<Unit>

    @POST("sync/notes") // Dihilangkan 'api/'
    suspend fun syncNotes(@Body notes: List<Note>): Response<List<Note>>
}

// Data class untuk request
data class UserLibraryRequest(
    val bookId: Long,
    val status: String,
    val lastPageRead: Int? = null,
    val rating: Float? = null
)

// Data class untuk response
data class UserLibraryResponse(
    val id: Long,
    val userId: String,
    val bookId: Long,
    val status: String,
    val lastPageRead: Int?,
    val rating: Float?,
    val createdAt: String?,
    val updatedAt: String?,
    val book: Book? = null // Pastikan nullable jika server tidak selalu mengembalikan book
)
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

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: Long): Response<Unit>

    @POST("sync/books")
    suspend fun syncBooks(@Body books: List<Book>): Response<List<Book>>

    // ApiService
    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: Long): Response<Book>

    // ------------------ User Library ------------------ //
    @GET("user-library")
    suspend fun getUserLibrary(
        @Query("status") status: String? = null,
        @Query("query") query: String? = null
    ): Response<List<UserLibrary>>

    @GET("user-library/reading")
    suspend fun getReadingBooks(
        @Query("userId") userId: String? = null
    ): Response<List<UserLibrary>>

    @POST("user-library")
    suspend fun createUserLibrary(@Body userLibrary: UserLibrary): Response<UserLibrary>

    @PUT("user-library/{id}")
    suspend fun updateUserLibrary(@Path("id") id: Long, @Body userLibrary: UserLibrary): Response<UserLibrary>

    @DELETE("user-library/{id}")
    suspend fun deleteUserLibrary(@Path("id") id: Long): Response<Unit>

    @POST("sync/user-library")
    suspend fun syncUserLibrary(@Body userLibraries: List<UserLibrary>): Response<List<UserLibrary>>

    // ------------------ Local User ------------------ //
    @GET("local-users")
    suspend fun getLocalUsers(): Response<List<LocalUser>>

    @POST("local-users")
    suspend fun createLocalUser(@Body localUser: LocalUser): Response<LocalUser>

    @PUT("local-users/{uid}")
    suspend fun updateLocalUser(@Path("uid") uid: String, @Body localUser: LocalUser): Response<LocalUser>

    @POST("sync/local-users")
    suspend fun syncLocalUsers(@Body localUsers: List<LocalUser>): Response<List<LocalUser>>

    // ------------------ Reading Progress ------------------ //
    @GET("reading-progress")
    suspend fun getReadingProgress(
        @Query("user_library_id") userLibraryId: Long? = null
    ): Response<List<ReadingProgress>>

    @POST("reading-progress")
    suspend fun createReadingProgress(@Body readingProgress: ReadingProgress): Response<ReadingProgress>

    @POST("sync/reading-progress")
    suspend fun syncReadingProgress(@Body readingProgressList: List<ReadingProgress>): Response<List<ReadingProgress>>

    // ------------------ Note ------------------ //
    @GET("notes")
    suspend fun getNotes(@Query("user_library_id") userLibraryId: Long? = null): Response<List<Note>>

    @POST("notes")
    suspend fun createNote(@Body note: Note): Response<Note>

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: Long, @Body note: Note): Response<Note>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Long): Response<Unit>

    @POST("sync/notes")
    suspend fun syncNotes(@Body notes: List<Note>): Response<List<Note>>
}

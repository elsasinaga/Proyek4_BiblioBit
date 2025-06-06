package com.example.bibliobit.di // Pastikan package Anda benar

import android.content.Context
import androidx.room.Room
import com.example.bibliobit.data.remote.ApiService
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.data.repository.*
import com.example.bibliobit.utils.BookStatusAdapter
import com.example.bibliobit.utils.PreferencesManager
import com.example.bibliobit.utils.ReadingStreak
import com.example.bibliobit.network.AuthInterceptor // Pastikan path import untuk AuthInterceptor benar
import com.example.bibliobit.data.model.BookStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // <-- PASTIKAN IMPORT INI ADA
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Pastikan BASE_URL ini sudah benar (kemungkinan diakhiri dengan /api/)
    private const val BASE_URL = "https://1f7b-2001-448a-3052-851e-7892-9a34-f73a-a557.ngrok-free.app/api/"

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, "bibliobit_database")
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
                // Jika ada migrasi lain, tambahkan di sini
            )
            .fallbackToDestructiveMigration() // Hati-hati dengan ini di production
            .build()
        println("Database version: ${database.openHelper.readableDatabase.version}")
        return database
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao = appDatabase.userDao()

    @Provides
    @Singleton
    fun provideBookDao(appDatabase: AppDatabase): BookDao = appDatabase.bookDao()

    @Provides
    @Singleton
    fun provideBookRepository(
        bookDao: BookDao,
        remoteDataSource: RemoteDataSource
    ): BookRepository = BookRepository(bookDao, remoteDataSource)

    @Provides
    @Singleton
    fun provideUserLibraryDao(appDatabase: AppDatabase): UserLibraryDao = appDatabase.userLibraryDao()

    @Provides
    @Singleton
    fun provideUserLibraryRepository(
        userLibraryDao: UserLibraryDao,
        remoteDataSource: RemoteDataSource
    ): UserLibraryRepository = UserLibraryRepository(userLibraryDao, remoteDataSource)

    @Provides
    @Singleton
    fun provideReadingProgressDao(appDatabase: AppDatabase): ReadingProgressDao = appDatabase.readingProgressDao()

    @Provides
    @Singleton
    fun provideReadingProgressRepository(
        readingProgressDao: ReadingProgressDao,
        remoteDataSource: RemoteDataSource
    ): ReadingProgressRepository = ReadingProgressRepository(readingProgressDao, remoteDataSource)

    @Provides
    @Singleton
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        remoteDataSource: RemoteDataSource
    ): NoteRepository = NoteRepository(noteDao, remoteDataSource)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao,
        remoteDataSource: RemoteDataSource
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore, userDao, remoteDataSource)

    @Provides
    @Singleton
    fun provideReadingStreakManager(
        @ApplicationContext context: Context,
        readingProgressDao: ReadingProgressDao,
        userLibraryDao: UserLibraryDao
    ): ReadingStreak = ReadingStreak(context, readingProgressDao, userLibraryDao)

    // --- PENAMBAHAN DAN MODIFIKASI UNTUK NETWORK ---
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        // Set level ke BODY untuk detail maksimal.
        // Ganti ke Level.BASIC atau Level.HEADERS untuk log yang lebih ringkas di production.
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth): AuthInterceptor {
        // Fungsi ini tetap sama seperti yang Anda miliki
        return AuthInterceptor(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor // Tambahkan parameter ini
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor) // Tambahkan logging interceptor (biasanya pertama)
            .addInterceptor(authInterceptor)       // Kemudian auth interceptor Anda
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)   // Opsional: tambahkan write timeout
            .build()
    }
    // --- AKHIR PENAMBAHAN DAN MODIFIKASI UNTUK NETWORK ---

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .setLenient() // Anda sudah punya ini
                .registerTypeAdapter(BookStatus::class.java, BookStatusAdapter()) // Dan ini
                .create()
        ))
        .client(client) // Ini akan menggunakan OkHttpClient yang sudah terkonfigurasi
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideRemoteDataSource(
        apiService: ApiService,
        firebaseAuth: FirebaseAuth
    ): RemoteDataSource = RemoteDataSource(apiService, firebaseAuth)
}
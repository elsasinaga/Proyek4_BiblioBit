package com.example.bibliobit.di // Pastikan package Anda benar

import android.content.Context
import com.example.bibliobit.data.remote.ApiService
import com.example.bibliobit.data.remote.RemoteDataSource
import com.example.bibliobit.utils.BookStatusAdapter
import com.example.bibliobit.utils.PreferencesManager
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

    private const val BASE_URL = "https://0caf-2001-448a-3045-80fd-b05f-bda-d62a-b3e4.ngrok-free.app/"

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
    ): RemoteDataSource = RemoteDataSource(apiService,  firebaseAuth)
}
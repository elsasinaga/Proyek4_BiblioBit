package com.example.bibliobit.di

import android.content.Context
import androidx.room.Room
import com.example.bibliobit.data.repository.AppDatabase
import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.data.repository.AuthRepositoryImpl
import com.example.bibliobit.data.repository.BookDao
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.NoteDao
import com.example.bibliobit.data.repository.NoteRepository
import com.example.bibliobit.data.repository.ReadingProgressDao
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserDao
import com.example.bibliobit.data.repository.UserLibraryDao
import com.example.bibliobit.data.repository.UserLibraryRepository
import com.example.bibliobit.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bibliobit_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideBookDao(appDatabase: AppDatabase): BookDao {
        return appDatabase.bookDao()
    }

    @Provides
    @Singleton
    fun provideBookRepository(bookDao: BookDao): BookRepository {
        return BookRepository(bookDao)
    }

    @Provides
    @Singleton
    fun provideUserLibraryDao(appDatabase: AppDatabase): UserLibraryDao {
        return appDatabase.userLibraryDao()
    }

    @Provides
    @Singleton
    fun provideUserLibraryRepository(userLibraryDao: UserLibraryDao): UserLibraryRepository {
        return UserLibraryRepository(userLibraryDao)
    }

    @Provides
    @Singleton
    fun provideReadingProgressDao(appDatabase: AppDatabase): ReadingProgressDao {
        return appDatabase.readingProgressDao()
    }

    @Provides
    @Singleton
    fun provideReadingProgressRepository(readingProgressDao: ReadingProgressDao): ReadingProgressRepository {
        return ReadingProgressRepository(readingProgressDao)
    }

    @Provides
    @Singleton
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao {
        return appDatabase.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepository(noteDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }
}
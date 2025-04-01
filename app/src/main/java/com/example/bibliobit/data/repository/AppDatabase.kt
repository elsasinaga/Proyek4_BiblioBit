package com.example.bibliobit.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.Book
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(entities = [LocalUser::class, Book::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao


    companion object {
        // Migrasi dari versi 1 ke versi 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Buat tabel books
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `books` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `author` TEXT NOT NULL,
                        `genre` TEXT,
                        `year` INTEGER,
                        `description` TEXT,
                        `isbn` TEXT,
                        `coverPhotoPath` TEXT
                    )
                """.trimIndent())
            }
        }
    }
}
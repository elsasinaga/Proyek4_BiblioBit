package com.example.bibliobit.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.LocalUser

@Database(entities = [LocalUser::class, Book::class], version = 3, exportSchema = false)
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

        // Migrasi dari versi 2 ke versi 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tambahkan kolom pages (INTEGER, nullable)
                database.execSQL("ALTER TABLE books ADD COLUMN pages INTEGER")
                // Tambahkan kolom publisher (TEXT, nullable)
                database.execSQL("ALTER TABLE books ADD COLUMN publisher TEXT")
            }
        }
    }
}
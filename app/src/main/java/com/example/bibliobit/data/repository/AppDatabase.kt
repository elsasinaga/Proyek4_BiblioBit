package com.example.bibliobit.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.utils.DateConverter
import com.example.bibliobit.utils.BookStatusConverter

@Database(entities = [LocalUser::class, Book::class, UserLibrary::class], version = 4, exportSchema = false)
@TypeConverters(DateConverter::class, BookStatusConverter::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun userLibraryDao(): UserLibraryDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_library` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `bookId` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `lastPageRead` INTEGER,
                        `updatedAt` INTEGER NOT NULL,
                        `rating` REAL,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`uid`) ON DELETE CASCADE,
                        FOREIGN KEY(`bookId`) REFERENCES `books`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_library_userId` ON `user_library` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_library_bookId` ON `user_library` (`bookId`)")
            }
        }
    }
}
package com.example.bibliobit.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.LocalUser
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.utils.BookStatusConverter
import com.example.bibliobit.utils.DateConverter

@Database(
    entities = [LocalUser::class, Book::class, UserLibrary::class, ReadingProgress::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(DateConverter::class, BookStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun userLibraryDao(): UserLibraryDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE books ADD COLUMN pages INTEGER")
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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reading_progress` (
                        `user_library_id` INTEGER NOT NULL,
                        `page_read` INTEGER NOT NULL,
                        `recorded_at` INTEGER NOT NULL,
                        PRIMARY KEY (`user_library_id`, `page_read`, `recorded_at`),
                        FOREIGN KEY (`user_library_id`) REFERENCES `user_library`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }
    }
}

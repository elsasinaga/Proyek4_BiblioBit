package com.example.bibliobit.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserLibrary::class,
            parentColumns = ["id"],
            childColumns = ["user_library_id"], // Gunakan nama kolom, bukan nama properti
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["user_library_id", "page_read", "recorded_at"] // Gunakan nama kolom, bukan nama properti
)
data class ReadingProgress(
    @ColumnInfo(name = "user_library_id")
    val userLibraryId: Long,

    @ColumnInfo(name = "page_read")
    val pageRead: Int,

    @ColumnInfo(name = "recorded_at")
    val recordedAt: Date
)
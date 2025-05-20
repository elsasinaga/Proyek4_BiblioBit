package com.example.bibliobit.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserLibrary::class,
            parentColumns = ["id"],
            childColumns = ["user_library_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["user_library_id", "page_read", "recorded_at"]
)
data class ReadingProgress(
    @ColumnInfo(name = "user_library_id")
    @SerializedName("user_library_id") val userLibraryId: Long,

    @ColumnInfo(name = "page_read")
    @SerializedName("page_read") val pageRead: Int,

    @ColumnInfo(name = "recorded_at")
    @SerializedName("recorded_at") val recordedAt: Date,

    @SerializedName("is_synced") val isSynced: Boolean = false
)
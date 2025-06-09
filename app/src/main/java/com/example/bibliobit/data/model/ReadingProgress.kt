package com.example.bibliobit.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ReadingProgress(
    // ## PASTIKAN FIELD INI ADA ##
    @SerializedName("user_library_id") val userLibraryId: Long,

    @SerializedName("page_read") val pageRead: Int,
    @SerializedName("recorded_at") val recordedAt: Date? = null
)
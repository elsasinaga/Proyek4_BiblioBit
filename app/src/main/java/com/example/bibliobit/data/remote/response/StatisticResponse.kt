package com.example.bibliobit.data.remote.response

import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.ReadingProgress
import com.google.gson.annotations.SerializedName

data class StatisticResponse(
    @SerializedName("totalPagesRead") val totalPagesRead: Int,
    @SerializedName("pagesReadData") val pagesReadData: Map<String, Int>,
    @SerializedName("totalBooksFinished") val totalBooksFinished: Int,
    @SerializedName("booksFinishedData") val booksFinishedData: Map<String, Int>,
    @SerializedName("readingHistory") val readingHistory: List<ReadingProgress>,
    @SerializedName("finishedBooks") val finishedBooks: List<Book>
)

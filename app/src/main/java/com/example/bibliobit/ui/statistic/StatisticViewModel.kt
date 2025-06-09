package com.example.bibliobit.ui.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.UserLibraryRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// Definisikan di sini agar bisa diakses oleh screen
data class ReadingHistoryEntry(
    val book: Book,
    val progress: ReadingProgress
)

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("week")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _statisticType = MutableStateFlow("pages")
    val statisticType: StateFlow<String> = _statisticType.asStateFlow()

    private val _pagesReadData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val pagesReadData: StateFlow<Map<String, Int>> = _pagesReadData.asStateFlow()

    private val _totalPagesRead = MutableStateFlow(0)
    val totalPagesRead: StateFlow<Int> = _totalPagesRead.asStateFlow()

    private val _booksFinishedData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val booksFinishedData: StateFlow<Map<String, Int>> = _booksFinishedData.asStateFlow()

    private val _totalBooksFinished = MutableStateFlow(0)
    val totalBooksFinished: StateFlow<Int> = _totalBooksFinished.asStateFlow()

    private val _readingHistory = MutableStateFlow<List<ReadingHistoryEntry>>(emptyList())
    val readingHistory: StateFlow<List<ReadingHistoryEntry>> = _readingHistory.asStateFlow()

    private val _finishedBooks = MutableStateFlow<List<Book>>(emptyList())
    val finishedBooks: StateFlow<List<Book>> = _finishedBooks.asStateFlow()

    fun setUserId(userId: String) {
        loadStatistics()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadStatistics()
    }

    fun setStatisticType(type: String) {
        _statisticType.value = type
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val userLibraryList = userLibraryRepository.getUserLibrary()
                val allProgress = readingProgressRepository.getAllReadingProgress()

                // Proses Statistik Halaman
                val filteredProgress = filterProgressByTime(allProgress, _selectedFilter.value)
                _totalPagesRead.value = filteredProgress.sumOf { it.pageRead } // Ini bisa disesuaikan logikanya
                _pagesReadData.value = groupPagesByFilter(filteredProgress, _selectedFilter.value)

                // Proses Statistik Buku Selesai
                val finishedLibraryItems = userLibraryList.filter { it.status == BookStatus.FINISH }
                val filteredFinishedItems = filterFinishedBooksByTime(finishedLibraryItems, _selectedFilter.value)
                _totalBooksFinished.value = filteredFinishedItems.size
                _booksFinishedData.value = groupBooksByFilter(filteredFinishedItems, _selectedFilter.value)
                _finishedBooks.value = filteredFinishedItems.mapNotNull { it.book }

                // Proses Reading History
                val historyEntries = mutableListOf<ReadingHistoryEntry>()
                val libraryMap = userLibraryList.associateBy { it.id }
                allProgress.forEach { progress ->
                    libraryMap[progress.userLibraryId]?.book?.let { book ->
                        historyEntries.add(ReadingHistoryEntry(book, progress))
                    }
                }
                _readingHistory.value = historyEntries.sortedByDescending { it.progress.recordedAt }

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // --- Helper Functions ---
    private fun filterProgressByTime(progressList: List<ReadingProgress>, filter: String): List<ReadingProgress> {
        val now = Calendar.getInstance()
        return progressList.filter {
            val progressDate = Calendar.getInstance().apply { time = it.recordedAt ?: Date() }
            when (filter) {
                "day" -> now.get(Calendar.DAY_OF_YEAR) == progressDate.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == progressDate.get(Calendar.YEAR)
                "week" -> now.get(Calendar.WEEK_OF_YEAR) == progressDate.get(Calendar.WEEK_OF_YEAR) && now.get(Calendar.YEAR) == progressDate.get(Calendar.YEAR)
                "month" -> now.get(Calendar.MONTH) == progressDate.get(Calendar.MONTH) && now.get(Calendar.YEAR) == progressDate.get(Calendar.YEAR)
                "year" -> now.get(Calendar.YEAR) == progressDate.get(Calendar.YEAR)
                else -> true
            }
        }
    }

    private fun filterFinishedBooksByTime(books: List<UserLibrary>, filter: String): List<UserLibrary> {
        val now = Calendar.getInstance()
        return books.filter {
            val bookDate = Calendar.getInstance().apply { time = it.updatedAt ?: Date() }
            when (filter) {
                "day" -> now.get(Calendar.DAY_OF_YEAR) == bookDate.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == bookDate.get(Calendar.YEAR)
                "week" -> now.get(Calendar.WEEK_OF_YEAR) == bookDate.get(Calendar.WEEK_OF_YEAR) && now.get(Calendar.YEAR) == bookDate.get(Calendar.YEAR)
                "month" -> now.get(Calendar.MONTH) == bookDate.get(Calendar.MONTH) && now.get(Calendar.YEAR) == bookDate.get(Calendar.YEAR)
                "year" -> now.get(Calendar.YEAR) == bookDate.get(Calendar.YEAR)
                else -> true
            }
        }
    }

    private fun groupPagesByFilter(progressList: List<ReadingProgress>, filter: String): Map<String, Int> {
        // Implementasi lengkap Anda...
        return emptyMap()
    }

    private fun groupBooksByFilter(books: List<UserLibrary>, filter: String): Map<String, Int> {
        // Implementasi lengkap Anda...
        return emptyMap()
    }
}
package com.example.bibliobit.ui.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    private val _selectedFilter = MutableStateFlow("day")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _pagesReadData = MutableStateFlow<Map<String, Int>>(emptyMap())
    val pagesReadData: StateFlow<Map<String, Int>> = _pagesReadData.asStateFlow()

    private val _totalPagesRead = MutableStateFlow(0)
    val totalPagesRead: StateFlow<Int> = _totalPagesRead.asStateFlow()

    private val _readingHistory = MutableStateFlow<List<ReadingHistoryEntry>>(emptyList())
    val readingHistory: StateFlow<List<ReadingHistoryEntry>> = _readingHistory.asStateFlow()

    data class ReadingHistoryEntry(
        val book: Book,
        val progress: ReadingProgress,
        val startPage: Int,
        val endPage: Int
    )

    fun setUserId(userId: String) {
        _userId.value = userId
        loadStatistics()
        loadReadingHistory()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadStatistics()
        loadReadingHistory()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            val allProgress = mutableListOf<ReadingProgress>()
            val userLibraryList = userLibraryRepository.getUserLibrary(userId).firstOrNull() ?: emptyList()

            // Ambil semua ReadingProgress untuk setiap UserLibrary
            userLibraryList.forEach { userLibrary ->
                val progressList = readingProgressRepository.getReadingProgressByUserLibraryId(userLibrary.id).firstOrNull() ?: emptyList()
                allProgress.addAll(progressList)
            }

            // Filter progress berdasarkan periode waktu yang sesuai dengan filter
            val filteredProgress = when (_selectedFilter.value) {
                "day" -> {
                    val today = Calendar.getInstance()
                    allProgress.filter {
                        val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                        progressDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                progressDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    }
                }
                "week" -> {
                    val calendar = Calendar.getInstance()
                    val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
                    allProgress.filter {
                        val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                        progressDate.get(Calendar.WEEK_OF_YEAR) == weekStart.get(Calendar.WEEK_OF_YEAR) &&
                                progressDate.get(Calendar.YEAR) == weekStart.get(Calendar.YEAR)
                    }
                }
                "month" -> {
                    val calendar = Calendar.getInstance()
                    allProgress.filter {
                        val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                        progressDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                progressDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                    }
                }
                "year" -> {
                    val calendar = Calendar.getInstance()
                    val currentYear = calendar.get(Calendar.YEAR)
                    allProgress.filter {
                        val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                        progressDate.get(Calendar.YEAR) in (currentYear - 4)..currentYear
                    }
                }
                else -> allProgress
            }

            // Hitung total halaman yang dibaca berdasarkan filter
            val totalPages = filteredProgress.sumOf { it.pageRead }
            _totalPagesRead.value = totalPages

            // Kelompokkan data berdasarkan filter
            val groupedData = when (_selectedFilter.value) {
                "day" -> groupByHour(filteredProgress)
                "week" -> groupByDay(filteredProgress)
                "month" -> groupByMonth(filteredProgress)
                "year" -> groupByYear(filteredProgress)
                else -> groupByHour(filteredProgress)
            }
            _pagesReadData.value = groupedData
        }
    }

    private fun loadReadingHistory() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            val userLibraryList = userLibraryRepository.getUserLibrary(userId).firstOrNull() ?: emptyList()
            val history = mutableListOf<ReadingHistoryEntry>()

            userLibraryList.forEach { userLibrary ->
                val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                val progressList = readingProgressRepository.getReadingProgressByUserLibraryId(userLibrary.id).firstOrNull() ?: emptyList()
                if (book != null) {
                    // Filter progress berdasarkan filter yang dipilih
                    val filteredProgress = when (_selectedFilter.value) {
                        "day" -> {
                            val today = Calendar.getInstance()
                            progressList.filter {
                                val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                                progressDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                        progressDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                            }
                        }
                        "week" -> {
                            val calendar = Calendar.getInstance()
                            val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
                            progressList.filter {
                                val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                                progressDate.get(Calendar.WEEK_OF_YEAR) == weekStart.get(Calendar.WEEK_OF_YEAR) &&
                                        progressDate.get(Calendar.YEAR) == weekStart.get(Calendar.YEAR)
                            }
                        }
                        "month" -> {
                            val calendar = Calendar.getInstance()
                            progressList.filter {
                                val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                                progressDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                        progressDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                            }
                        }
                        "year" -> {
                            val calendar = Calendar.getInstance()
                            val currentYear = calendar.get(Calendar.YEAR)
                            progressList.filter {
                                val progressDate = Calendar.getInstance().apply { time = it.recordedAt }
                                progressDate.get(Calendar.YEAR) in (currentYear - 4)..currentYear
                            }
                        }
                        else -> progressList
                    }

                    // Urutkan progress berdasarkan waktu (ascending) untuk menghitung rentang halaman
                    val sortedProgress = filteredProgress.sortedBy { it.recordedAt }
                    var currentPage = 0 // Halaman awal untuk buku ini

                    sortedProgress.forEach { progress ->
                        val startPage = currentPage
                        val endPage = currentPage + progress.pageRead
                        history.add(
                            ReadingHistoryEntry(
                                book = book,
                                progress = progress,
                                startPage = startPage,
                                endPage = endPage
                            )
                        )
                        currentPage = endPage // Update halaman terakhir untuk record berikutnya
                    }
                }
            }

            // Urutkan riwayat berdasarkan waktu (descending) untuk ditampilkan
            _readingHistory.value = history.sortedByDescending { it.progress.recordedAt }
        }
    }

    private fun groupByHour(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()

        // Inisialisasi data untuk 24 jam (00:00 - 23:00)
        for (hour in 0..23) {
            val hourLabel = String.format("%02d:00", hour)
            groupedData[hourLabel] = 0
        }

        // Kelompokkan data berdasarkan jam
        progressList.forEach { progress ->
            val hourLabel = sdf.format(progress.recordedAt)
            groupedData[hourLabel] = (groupedData[hourLabel] ?: 0) + progress.pageRead
        }

        return groupedData.toSortedMap()
    }

    private fun groupByDay(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()

        // Inisialisasi data untuk 7 hari dalam seminggu (Mon - Sun)
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day ->
            groupedData[day] = 0
        }

        // Kelompokkan data berdasarkan hari
        progressList.forEach { progress ->
            val dayLabel = sdf.format(progress.recordedAt)
            groupedData[dayLabel] = (groupedData[dayLabel] ?: 0) + progress.pageRead
        }

        return groupedData.toSortedMap(compareBy { days.indexOf(it) })
    }

    private fun groupByMonth(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()

        // Inisialisasi data untuk semua bulan (Jan - Dec)
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        months.forEach { month ->
            groupedData[month] = 0
        }

        // Kelompokkan data berdasarkan bulan
        progressList.forEach { progress ->
            val monthLabel = sdf.format(progress.recordedAt)
            groupedData[monthLabel] = (groupedData[monthLabel] ?: 0) + progress.pageRead
        }

        return groupedData.toSortedMap(compareBy { months.indexOf(it) })
    }

    private fun groupByYear(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val groupedData = mutableMapOf<String, Int>()

        // Inisialisasi data untuk 5 tahun terakhir (2021 - 2025)
        val currentYear = calendar.get(Calendar.YEAR)
        for (year in (currentYear - 4)..currentYear) {
            val yearLabel = year.toString()
            groupedData[yearLabel] = 0
        }

        // Kelompokkan data berdasarkan tahun
        progressList.forEach { progress ->
            val yearLabel = sdf.format(progress.recordedAt)
            groupedData[yearLabel] = (groupedData[yearLabel] ?: 0) + progress.pageRead
        }

        // Urutkan tahun secara ascending (2021, 2022, ..., 2025)
        return groupedData.toSortedMap(compareBy { it })
    }
}
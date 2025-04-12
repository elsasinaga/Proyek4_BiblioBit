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
import com.example.bibliobit.data.model.BookStatus

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    private val _selectedFilter = MutableStateFlow("day")
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
        loadFinishedBooks()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadStatistics()
        loadReadingHistory()
        loadFinishedBooks()
    }

    fun setStatisticType(type: String) {
        _statisticType.value = type
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            userLibraryRepository.getUserLibrary(userId).collect { userLibraryList ->
                // Load statistik halaman yang dibaca
                val allProgress = mutableListOf<ReadingProgress>()
                userLibraryList.forEach { userLibrary ->
                    val progressList = readingProgressRepository.getReadingProgressByUserLibraryId(userLibrary.id).firstOrNull() ?: emptyList()
                    allProgress.addAll(progressList)
                }

                // Filter progress berdasarkan periode waktu
                val filteredProgress = filterProgressByTime(allProgress)
                _totalPagesRead.value = filteredProgress.sumOf { it.pageRead }
                _pagesReadData.value = groupPagesByFilter(filteredProgress)

                // Load statistik buku yang selesai
                val finishedBooks = userLibraryList.filter { it.status == BookStatus.FINISH }
                val filteredFinishedBooks = filterFinishedBooksByTime(finishedBooks)
                _totalBooksFinished.value = filteredFinishedBooks.size
                _booksFinishedData.value = groupBooksByFilter(filteredFinishedBooks)
            }
        }
    }

    private fun loadFinishedBooks() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            userLibraryRepository.getUserLibrary(userId).collect { userLibraryList ->
                val finishedBooks = mutableListOf<Book>()
                // Filter UserLibrary berdasarkan status FINISH dan periode waktu
                val filteredUserLibraries = filterFinishedBooksByTime(userLibraryList.filter { it.status == BookStatus.FINISH })
                filteredUserLibraries.forEach { userLibrary ->
                    val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                    if (book != null) {
                        finishedBooks.add(book)
                    }
                }
                _finishedBooks.value = finishedBooks
            }
        }
    }

    private fun filterProgressByTime(progressList: List<ReadingProgress>): List<ReadingProgress> {
        return when (_selectedFilter.value) {
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
    }

    private fun filterFinishedBooksByTime(books: List<UserLibrary>): List<UserLibrary> {
        return when (_selectedFilter.value) {
            "day" -> {
                val today = Calendar.getInstance()
                books.filter {
                    val bookDate = Calendar.getInstance().apply { time = it.updatedAt }
                    bookDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            bookDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                }
            }
            "week" -> {
                val calendar = Calendar.getInstance()
                val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
                books.filter {
                    val bookDate = Calendar.getInstance().apply { time = it.updatedAt }
                    bookDate.get(Calendar.WEEK_OF_YEAR) == weekStart.get(Calendar.WEEK_OF_YEAR) &&
                            bookDate.get(Calendar.YEAR) == weekStart.get(Calendar.YEAR)
                }
            }
            "month" -> {
                val calendar = Calendar.getInstance()
                books.filter {
                    val bookDate = Calendar.getInstance().apply { time = it.updatedAt }
                    bookDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                            bookDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                }
            }
            "year" -> {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                books.filter {
                    val bookDate = Calendar.getInstance().apply { time = it.updatedAt }
                    bookDate.get(Calendar.YEAR) in (currentYear - 4)..currentYear
                }
            }
            else -> books
        }
    }

    private fun groupPagesByFilter(progressList: List<ReadingProgress>): Map<String, Int> {
        return when (_selectedFilter.value) {
            "day" -> groupByHour(progressList)
            "week" -> groupByDay(progressList)
            "month" -> groupByMonth(progressList)
            "year" -> groupByYear(progressList)
            else -> groupByHour(progressList)
        }
    }

    private fun groupBooksByFilter(books: List<UserLibrary>): Map<String, Int> {
        return when (_selectedFilter.value) {
            "day" -> groupBooksByHour(books)
            "week" -> groupBooksByDay(books)
            "month" -> groupBooksByMonth(books)
            "year" -> groupBooksByYear(books)
            else -> groupBooksByHour(books)
        }
    }

    private fun groupByHour(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        for (hour in 0..23) {
            val hourLabel = String.format("%02d:00", hour)
            groupedData[hourLabel] = 0
        }
        progressList.forEach { progress ->
            val hourLabel = sdf.format(progress.recordedAt)
            groupedData[hourLabel] = (groupedData[hourLabel] ?: 0) + progress.pageRead
        }
        return groupedData.toSortedMap()
    }

    private fun groupBooksByHour(books: List<UserLibrary>): Map<String, Int> {
        val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        for (hour in 0..23) {
            val hourLabel = String.format("%02d:00", hour)
            groupedData[hourLabel] = 0
        }
        books.forEach { book ->
            val hourLabel = sdf.format(book.updatedAt)
            groupedData[hourLabel] = (groupedData[hourLabel] ?: 0) + 1
        }
        return groupedData.toSortedMap()
    }

    private fun groupByDay(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day -> groupedData[day] = 0 }
        progressList.forEach { progress ->
            val dayLabel = sdf.format(progress.recordedAt)
            groupedData[dayLabel] = (groupedData[dayLabel] ?: 0) + progress.pageRead
        }
        return groupedData.toSortedMap(compareBy { days.indexOf(it) })
    }

    private fun groupBooksByDay(books: List<UserLibrary>): Map<String, Int> {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        days.forEach { day -> groupedData[day] = 0 }
        books.forEach { book ->
            val dayLabel = sdf.format(book.updatedAt)
            groupedData[dayLabel] = (groupedData[dayLabel] ?: 0) + 1
        }
        return groupedData.toSortedMap(compareBy { days.indexOf(it) })
    }

    private fun groupByMonth(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        months.forEach { month -> groupedData[month] = 0 }
        progressList.forEach { progress ->
            val monthLabel = sdf.format(progress.recordedAt)
            groupedData[monthLabel] = (groupedData[monthLabel] ?: 0) + progress.pageRead
        }
        return groupedData.toSortedMap(compareBy { months.indexOf(it) })
    }

    private fun groupBooksByMonth(books: List<UserLibrary>): Map<String, Int> {
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        val groupedData = mutableMapOf<String, Int>()
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        months.forEach { month -> groupedData[month] = 0 }
        books.forEach { book ->
            val monthLabel = sdf.format(book.updatedAt)
            groupedData[monthLabel] = (groupedData[monthLabel] ?: 0) + 1
        }
        return groupedData.toSortedMap(compareBy { months.indexOf(it) })
    }

    private fun groupByYear(progressList: List<ReadingProgress>): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val groupedData = mutableMapOf<String, Int>()
        val currentYear = calendar.get(Calendar.YEAR)
        for (year in (currentYear - 4)..currentYear) {
            val yearLabel = year.toString()
            groupedData[yearLabel] = 0
        }
        progressList.forEach { progress ->
            val yearLabel = sdf.format(progress.recordedAt)
            groupedData[yearLabel] = (groupedData[yearLabel] ?: 0) + progress.pageRead
        }
        return groupedData.toSortedMap(compareBy { it })
    }

    private fun groupBooksByYear(books: List<UserLibrary>): Map<String, Int> {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val groupedData = mutableMapOf<String, Int>()
        val currentYear = calendar.get(Calendar.YEAR)
        for (year in (currentYear - 4)..currentYear) {
            val yearLabel = year.toString()
            groupedData[yearLabel] = 0
        }
        books.forEach { book ->
            val yearLabel = sdf.format(book.updatedAt)
            groupedData[yearLabel] = (groupedData[yearLabel] ?: 0) + 1
        }
        return groupedData.toSortedMap(compareBy { it })
    }

    private fun loadReadingHistory() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            userLibraryRepository.getUserLibrary(userId).collect { userLibraryList ->
                val history = mutableListOf<ReadingHistoryEntry>()
                userLibraryList.forEach { userLibrary ->
                    val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                    val progressList = readingProgressRepository.getReadingProgressByUserLibraryId(userLibrary.id).firstOrNull() ?: emptyList()
                    if (book != null) {
                        val filteredProgress = filterProgressByTime(progressList)
                        val sortedProgress = filteredProgress.sortedBy { it.recordedAt }
                        var currentPage = 0
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
                            currentPage = endPage
                        }
                    }
                }
                _readingHistory.value = history.sortedByDescending { it.progress.recordedAt }
            }
        }
    }
}
package com.example.bibliobit.ui.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.repository.StatisticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val statisticRepository: StatisticRepository
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

    private val _readingHistory = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val readingHistory: StateFlow<List<ReadingProgress>> = _readingHistory.asStateFlow()

    private val _finishedBooks = MutableStateFlow<List<Book>>(emptyList())
    val finishedBooks: StateFlow<List<Book>> = _finishedBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadStatistics()
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadStatistics() // Panggil ulang untuk mendapatkan data dengan filter baru
    }

    fun setStatisticType(type: String) {
        _statisticType.value = type
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            statisticRepository.getStatistics(filter = _selectedFilter.value)
                .onSuccess { response ->
                    _totalPagesRead.value = response.totalPagesRead
                    _pagesReadData.value = response.pagesReadData
                    _totalBooksFinished.value = response.totalBooksFinished
                    _booksFinishedData.value = response.booksFinishedData
                    _readingHistory.value = response.readingHistory
                    _finishedBooks.value = response.finishedBooks
                }
                .onFailure { error ->
                    _errorMessage.value = "Gagal memuat statistik: ${error.message}"
                }
            _isLoading.value = false
        }
    }
}

package com.example.bibliobit.ui.readingprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ReadingProgressViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    private val _firstReadingProgress = MutableStateFlow<ReadingProgress?>(null)
    val firstReadingProgress: StateFlow<ReadingProgress?> = _firstReadingProgress.asStateFlow()

    private val _daysBetweenStartAndLast = MutableStateFlow(0L)
    val daysBetweenStartAndLast: StateFlow<Long> = _daysBetweenStartAndLast.asStateFlow()

    // State untuk riwayat progres, dibutuhkan oleh YourProgressReadingScreen
    private val _progressHistory = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val progressHistory: StateFlow<List<ReadingProgress>> = _progressHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun initialize(userLibraryId: Long) {
        if (userLibraryId == 0L) {
            _error.value = "Invalid Library ID."
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val libraryEntry = userLibraryRepository.getUserLibraryById(userLibraryId)
                _userLibrary.value = libraryEntry

                if (libraryEntry != null) {
                    val bookData = bookRepository.getBookById(libraryEntry.bookId)
                    _book.value = bookData

                    val history = readingProgressRepository.getReadingProgressByUserLibraryId(userLibraryId)
                    _progressHistory.value = history.sortedByDescending { it.recordedAt } // Urutkan dari terbaru

                    val firstProgress = history.minByOrNull { it.recordedAt?.time ?: Long.MAX_VALUE }
                    val lastProgress = history.maxByOrNull { it.recordedAt?.time ?: Long.MIN_VALUE }
                    _firstReadingProgress.value = firstProgress

                    if (firstProgress?.recordedAt != null && lastProgress?.recordedAt != null) {
                        val diff = lastProgress.recordedAt!!.time - firstProgress.recordedAt!!.time
                        val days = TimeUnit.MILLISECONDS.toDays(diff) + 1
                        _daysBetweenStartAndLast.value = days
                    } else if (firstProgress != null) {
                        _daysBetweenStartAndLast.value = 1
                    } else {
                        _daysBetweenStartAndLast.value = 0
                    }
                } else {
                    _error.value = "Library entry not found."
                }
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addReadingProgress(
        userLibraryId: Long,
        pageRead: Int,
        recordedAt: Date,
        isFinished: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                readingProgressRepository.insert(ReadingProgress(userLibraryId, pageRead, recordedAt))

                val currentEntry = userLibraryRepository.getUserLibraryById(userLibraryId)
                val newStatus = if (isFinished) BookStatus.FINISH else BookStatus.READING

                val updatedEntry = currentEntry.copy(
                    lastPageRead = pageRead,
                    status = newStatus,
                    updatedAt = recordedAt
                )
                userLibraryRepository.upsertUserLibrary(updatedEntry)
                // Panggil initialize lagi untuk refresh data di layar sebelumnya saat kembali
                initialize(userLibraryId)
            } catch (e: Exception) {
                _error.value = "Failed to save progress: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertStartReadingProgress(
        userLibraryId: Long,
        startDate: Date,
        lastReadingDate: Date,
        pageRead: Int,
        isFinished: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Buat entri "Start Reading" di halaman 0
                readingProgressRepository.insert(ReadingProgress(userLibraryId, 0, startDate))
                // Buat entri progres aktual
                addReadingProgress(userLibraryId, pageRead, lastReadingDate, isFinished)
            } catch (e: Exception) {
                _error.value = "Failed to save initial progress: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
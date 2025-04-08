package com.example.bibliobit.ui.yourfinishbook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddYourRatingViewModel @Inject constructor(
    private val readingProgressRepository: ReadingProgressRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val bookRepository: BookRepository // Tambahkan BookRepository
) : ViewModel() {

    private val _readingProgress = MutableStateFlow<List<ReadingProgress>>(emptyList())
    val readingProgress: StateFlow<List<ReadingProgress>> = _readingProgress.asStateFlow()

    private val _firstReadingProgress = MutableStateFlow<ReadingProgress?>(null)
    val firstReadingProgress: StateFlow<ReadingProgress?> = _firstReadingProgress.asStateFlow()

    private val _totalPages = MutableStateFlow<Int>(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _isFinished = MutableStateFlow<Boolean>(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun loadReadingProgress(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                val userLibrary = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                if (userLibrary != null) {
                    // Ambil data dari repository menggunakan Flow
                    val progressListFlow = readingProgressRepository.getReadingProgressByUserLibraryId(userLibrary.id)
                    val progressList = progressListFlow.firstOrNull()?.sortedBy { it.recordedAt } ?: emptyList()
                    _readingProgress.value = progressList

                    // Ambil first reading progress
                    val firstProgress = readingProgressRepository.getFirstReadingProgress(userLibrary.id)
                    _firstReadingProgress.value = firstProgress

                    // Ambil totalPages dari Book
                    val book = bookRepository.getBookById(bookId).firstOrNull()
                    _totalPages.value = book?.pages ?: 0

                    // Tentukan apakah buku selesai
                    _isFinished.value = userLibrary.status == BookStatus.FINISH

                    Log.d("AddYourRatingViewModel", "Loaded reading progress: $progressList")
                    Log.d("AddYourRatingViewModel", "Loaded first reading progress: $firstProgress")
                    Log.d("AddYourRatingViewModel", "Loaded total pages: ${_totalPages.value}")
                    Log.d("AddYourRatingViewModel", "Is finished: ${_isFinished.value}")
                }
            } catch (e: Exception) {
                Log.e("AddYourRatingViewModel", "Error loading reading progress: ${e.message}", e)
            }
        }
    }

    fun saveRating(userId: String, bookId: Long, rating: Float) {
        viewModelScope.launch {
            try {
                val userLibrary = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                if (userLibrary != null) {
                    val updatedUserLibrary = userLibrary.copy(rating = rating)
                    userLibraryRepository.update(updatedUserLibrary)
                    Log.d("AddYourRatingViewModel", "Rating saved: $rating for bookId: $bookId")
                }
            } catch (e: Exception) {
                Log.e("AddYourRatingViewModel", "Error saving rating: ${e.message}", e)
            }
        }
    }
}
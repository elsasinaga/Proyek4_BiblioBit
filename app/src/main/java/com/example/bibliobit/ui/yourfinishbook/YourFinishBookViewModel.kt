package com.example.bibliobit.ui.yourfinishbook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
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
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class YourFinishBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            try {
                val bookData = bookRepository.getBookById(bookId).firstOrNull()
                _book.value = bookData
                Log.d("YourFinishBookViewModel", "Loaded book: $bookData")
            } catch (e: Exception) {
                Log.e("YourFinishBookViewModel", "Error loading book: ${e.message}", e)
            }
        }
    }

    fun loadUserLibrary(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                val userLibraryData = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                _userLibrary.value = userLibraryData
                Log.d("YourFinishBookViewModel", "Loaded user library: $userLibraryData")
            } catch (e: Exception) {
                Log.e("YourFinishBookViewModel", "Error loading user library: ${e.message}", e)
            }
        }
    }

    fun readAgain(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                // Ambil data UserLibrary
                val userLibrary = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                if (userLibrary != null) {
                    // Perbarui UserLibrary
                    val updatedUserLibrary = userLibrary.copy(
                        status = BookStatus.READING,
                        lastPageRead = 0,
                        rating = null, // Hapus rating
                        updatedAt = Date() // Perbarui waktu
                    )
                    userLibraryRepository.update(updatedUserLibrary)
                    Log.d("YourFinishBookViewModel", "Updated UserLibrary to READING: $updatedUserLibrary")

                    // Hapus semua ReadingProgress untuk userLibraryId ini
                    readingProgressRepository.deleteReadingProgressByUserLibraryId(userLibrary.id)
                    Log.d("YourFinishBookViewModel", "Deleted ReadingProgress for userLibraryId: ${userLibrary.id}")
                } else {
                    Log.e("YourFinishBookViewModel", "UserLibrary not found for userId: $userId, bookId: $bookId")
                }
            } catch (e: Exception) {
                Log.e("YourFinishBookViewModel", "Error in readAgain: ${e.message}", e)
            }
        }
    }
}
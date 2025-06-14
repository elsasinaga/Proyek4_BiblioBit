package com.example.bibliobit.ui.yourfinishbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// State untuk UI
data class FinishBookUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val userLibrary: UserLibrary? = null
)

@HiltViewModel
class YourFinishBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            try {
                _book.value = bookRepository.getBookById(bookId)
            } catch (e: Exception) {
                // TODO: Handle error, misalnya dengan state error terpisah
            }
        }
    }

    fun loadUserLibrary(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                // Asumsi repository bisa filter berdasarkan userId, jika tidak, sesuaikan.
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                _userLibrary.value = allLibraryItems.firstOrNull { it.bookId == bookId }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun readAgain(userId: String, bookId: Long) {
        viewModelScope.launch {
            val currentLibrary = _userLibrary.value ?: return@launch
            try {
                val updatedEntry = currentLibrary.copy(
                    status = BookStatus.READING,
                    lastPageRead = 0,
                    rating = null,
                    updatedAt = Date()
                )
                // Kirim pembaruan ke server
                userLibraryRepository.upsertUserLibrary(updatedEntry)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun loadData(bookId: Long) {
        viewModelScope.launch {
            try {
                // Fungsi ini memuat kedua data, yang sebenarnya lebih efisien
                val bookData = bookRepository.getBookById(bookId)
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                val userLibraryData = allLibraryItems.firstOrNull { it.bookId == bookId }

                _book.value = bookData
                _userLibrary.value = userLibraryData

            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
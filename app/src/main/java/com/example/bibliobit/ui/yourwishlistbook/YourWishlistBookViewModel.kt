package com.example.bibliobit.ui.yourwishlistbook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YourWishlistBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            try {
                val bookData = bookRepository.getBookById(bookId).firstOrNull()
                _book.value = bookData
                Log.d("YourWishlistBookViewModel", "Loaded book: $bookData")
            } catch (e: Exception) {
                Log.e("YourWishlistBookViewModel", "Error loading book: ${e.message}", e)
            }
        }
    }

    fun startReading(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                userLibraryRepository.updateUserLibraryStatus(
                    userId = userId,
                    bookId = bookId,
                    status = BookStatus.READING
                )
                Log.d("YourWishlistBookViewModel", "Book status updated to READING for bookId: $bookId")
            } catch (e: Exception) {
                Log.e("YourWishlistBookViewModel", "Error updating book status: ${e.message}", e)
            }
        }
    }
}
package com.example.bibliobit.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                _isLoading.value = true
                try {
                    if (query.isBlank()) {
                        bookRepository.getAllBooks()
                    } else {
                        bookRepository.searchBooks(query)
                    }
                } finally {
                    _isLoading.value = false
                }
            }.collect { books ->
                _books.value = books
                _errorMessage.value = null
            }
        }
    }

    suspend fun insertBookAndGetId(book: Book): Long {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            val bookId = bookRepository.insertBook(book)
            bookRepository.syncBooksFromServer() // Pastikan data lokal selaras
            bookId
        } catch (e: Exception) {
            _errorMessage.value = "Failed to add book: ${e.message}"
            -1L // Indikator kegagalan
        } finally {
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
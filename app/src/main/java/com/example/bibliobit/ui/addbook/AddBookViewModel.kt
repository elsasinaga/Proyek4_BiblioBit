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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // State untuk search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Daftar buku yang difilter berdasarkan search query
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    init {
        // Gunakan flatMapLatest untuk beralih antara getAllBooks dan searchBooks
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    bookRepository.getAllBooks()
                } else {
                    bookRepository.searchBooks(query)
                }
            }.collect { books ->
                _books.value = books
            }
        }
    }

    // Fungsi untuk menambahkan buku ke database
    suspend fun insertBook(book: Book) {
        bookRepository.insertBook(book)
    }

    // Fungsi baru untuk menyisipkan buku dan mengembalikan ID
//    suspend fun insertBookAndGetId(book: Book): Long {
//        bookRepository.insertBook(book)
//        // Karena Room tidak langsung mengembalikan ID, kita harus mengambil buku terakhir yang disisipkan
//        // atau memodifikasi BookDao untuk mengembalikan ID
//        return bookRepository.getAllBooks().firstOrNull()?.lastOrNull()?.id ?: 0L
//    }

    suspend fun insertBookAndGetId(book: Book): Long {
        return bookRepository.insertBook(book)
    }

    // Fungsi untuk memperbarui search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
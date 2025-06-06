package com.example.bibliobit.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// AddBookViewModel.kt

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Bisa dimulai dengan true jika ingin loading langsung
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Coroutine untuk sinkronisasi awal dan mengatur isLoading
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Bersihkan error sebelumnya
            try {
                bookRepository.syncBooksFromServer()
                // Setelah sinkronisasi, Flow di bawah akan mengambil data terbaru
            } catch (e: Exception) {
                val errorMsg = "Failed to sync books: ${e.message}"
                _errorMessage.value = errorMsg
                println("Error syncing books in AddBookViewModel: $errorMsg")
                // Pertimbangkan untuk tetap menampilkan buku dari cache lokal jika sync gagal
                // atau biarkan _books diobservasi oleh flow di bawah
            } finally {
                _isLoading.value = false // Ini akan dieksekusi setelah sync selesai/gagal
            }
        }

        // Coroutine terpisah untuk mengobservasi perubahan buku berdasarkan searchQuery
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    bookRepository.getAllBooks()
                } else {
                    bookRepository.searchBooks(query)
                }
            }.catch { e -> // Tangani error spesifik dari flow buku ini
                val errorMsg = "Failed to load books: ${e.message}"
                _errorMessage.value = errorMsg // Hati-hati jika ini menimpa error dari sync
                println("Error collecting books in AddBookViewModel: $errorMsg")
                emit(emptyList()) // Emit daftar kosong jika ada error, atau sesuai kebutuhan
            }.collect { bookList ->
                _books.value = bookList
                // Jika pengumpulan buku berhasil, mungkin kita ingin membersihkan pesan error
                // yang spesifik untuk pemuatan buku (bukan error sinkronisasi).
                // Ini memerlukan logika error yang lebih hati-hati jika ada beberapa sumber error.
                // Untuk saat ini, kita bisa mengasumsikan jika buku termuat, error terkait pemuatan buku hilang.
                if (_errorMessage.value?.startsWith("Failed to load books:") == true) {
                    _errorMessage.value = null
                }
                println("Updated books in AddBookViewModel: ${bookList.size} books (query: '${_searchQuery.value}')")
            }
        }
    }

    suspend fun insertBookAndGetId(book: Book): Long {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            println("Inserting book in AddBookViewModel: $book")
            val bookId = bookRepository.insert(book)
            println("Book inserted with ID: $bookId")
            bookRepository.syncBooksFromServer() // Pastikan data lokal selaras
            println("Synced books after insert")
            bookId
        } catch (e: Exception) {
            _errorMessage.value = "Failed to add book: ${e.message}"
            println("Error inserting book in AddBookViewModel: ${e.message}")
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
package com.example.bibliobit.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.GoogleBook
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.BookSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookSearchRepository: BookSearchRepository, // Repository untuk mencari buku baru dari Google API
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

//    private val _books = MutableStateFlow<List<Book>>(emptyList())
//    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GoogleBook>>(emptyList())
    val searchResults: StateFlow<List<GoogleBook>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Saat ViewModel dibuat, langsung ambil daftar buku awal dari server.
        // fetchBooks()

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length > 2 }
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }

        performSearch("")
    }

    /**
     * Fungsi yang dipanggil dari UI setiap kali teks pencarian berubah.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _errorMessage.value = null
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // Backend cerdas Anda akan menangani apakah ini discover atau pencarian
            bookSearchRepository.searchBooks(query)
                .onSuccess { books ->
                    _searchResults.value = books
                }
                .onFailure { error ->
                    _searchResults.value = emptyList()
                    _errorMessage.value = "Gagal mencari buku: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    /**
     * Fungsi untuk menangani ISBN dari scanner.
     */
    suspend fun findAndProcessScannedIsbn(isbn: String): Long {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val googleBookResult = bookSearchRepository.findBookByIsbn(isbn)

            googleBookResult.getOrThrow().let { googleBook ->
                val book = bookRepository.findOrCreateBook(googleBook)
                book.id ?: -1L
            }
        } catch (e: Exception) {
            _errorMessage.value = "Buku dengan ISBN $isbn tidak ditemukan."
            -1L
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Fungsi ini sekarang tidak lagi menyimpan, tetapi mencari atau membuat
     * buku lalu mengembalikan ID-nya untuk navigasi.
     */
    suspend fun selectBookFromSearch(googleBook: GoogleBook): Long {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val book = bookRepository.findOrCreateBook(googleBook)
            book.id ?: -1L
        } catch (e: Exception) {
            _errorMessage.value = "Gagal memproses buku: ${e.message}"
            -1L
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Membuat buku baru di server dan me-refresh daftar buku.
     * Mengembalikan ID buku baru jika berhasil, atau -1L jika gagal.
     */
    suspend fun createBook(book: Book): Long {
        return try {
            _isLoading.value = true
            _errorMessage.value = null

            // Panggil fungsi `createBook` yang sudah kita buat di repository
            val newBook = bookRepository.createBook(book)

            // Setelah berhasil membuat, refresh daftar buku
            // fetchBooks()
            performSearch("") // Muat ulang daftar discover setelah menambah buku manual

            newBook.id
        } catch (e: Exception) {
            _errorMessage.value = "Failed to add book: ${e.message}"
            -1L // Indikator kegagalan
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
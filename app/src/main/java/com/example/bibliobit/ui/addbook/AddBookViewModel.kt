package com.example.bibliobit.ui.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var searchJob: Job? = null

    init {
        // Saat ViewModel dibuat, langsung ambil daftar buku awal dari server.
        fetchBooks()
    }

    /**
     * Mengambil daftar buku dari server berdasarkan query pencarian.
     * Jika query kosong, ambil semua buku.
     */
    private fun fetchBooks(query: String = "") {
        // Batalkan job pencarian sebelumnya agar tidak ada tumpukan request
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Panggil suspend fun dari repository secara langsung
                val result = if (query.isBlank()) {
                    bookRepository.getAllBooks()
                } else {
                    // Asumsi ada fungsi searchBooks di repository Anda
                    // Jika belum ada, tambahkan di BookRepository:
                    // suspend fun searchBooks(query: String): List<Book> = remoteDataSource.searchBooks(query)
                    bookRepository.searchBooks(query)
                }
                _books.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load books: ${e.message}"
                _books.value = emptyList() // Kosongkan daftar jika gagal
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fungsi yang dipanggil dari UI setiap kali teks pencarian berubah.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        fetchBooks(query)
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
            fetchBooks()

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
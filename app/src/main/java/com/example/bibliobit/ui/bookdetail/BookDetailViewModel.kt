package com.example.bibliobit.ui.bookdetail

import android.util.Log
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

data class UiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Memuat semua detail yang diperlukan untuk layar ini:
     * 1. Detail buku dari server.
     * 2. Status buku ini di perpustakaan pengguna (jika ada).
     */
    fun loadBookDetails(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true, error = null)
            try {
                // 1. Ambil detail buku dari API
                val bookDetails = bookRepository.getBookById(bookId)
                _book.value = bookDetails

                // 2. Ambil data library pengguna dari API, lalu cari entri yang cocok
                val userLibraryEntries = userLibraryRepository.getUserLibrary()
                _userLibrary.value = userLibraryEntries.firstOrNull { it.bookId == bookId }

                _uiState.value = UiState(isLoading = false)
            } catch (e: Exception) {
                Log.e("BookDetailViewModel", "Gagal memuat data detail buku", e)
                _uiState.value = UiState(isLoading = false, error = "Gagal memuat data: ${e.message}")
            }
        }
    }

    /**
     * Memperbarui status buku di perpustakaan pengguna.
     * Fungsi ini akan membuat entri baru jika belum ada, atau memperbarui yang sudah ada.
     */
    fun updateBookStatus(userId: String, bookId: Long, newStatus: BookStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val currentEntry = _userLibrary.value

                // Siapkan objek yang akan dikirim ke server
                val entryToUpsert = currentEntry?.copy(
                    status = newStatus
                ) ?: UserLibrary(
                    // Jika buku belum ada di library, buat objek baru
                    userId = userId,
                    bookId = bookId,
                    status = newStatus
                )

                // Kirim data ke server melalui repository
                val updatedEntryFromServer = userLibraryRepository.upsertUserLibrary(entryToUpsert)

                // Perbarui state UI dengan data valid dari server untuk memastikan konsistensi
                _userLibrary.value = updatedEntryFromServer
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                Log.e("BookDetailViewModel", "Gagal memperbarui status buku", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Gagal memperbarui status: ${e.message}")
            }
        }
    }
}
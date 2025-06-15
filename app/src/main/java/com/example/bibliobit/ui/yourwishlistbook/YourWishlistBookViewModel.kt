// File: ui/yourwishlistbook/YourWishlistBookViewModel.kt

package com.example.bibliobit.ui.yourwishlistbook

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// State untuk UI, menggabungkan semua data yang dibutuhkan layar
data class WishlistBookUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val userLibrary: UserLibrary? = null,
    val error: String? = null,
    val startReadingSuccess: Boolean = false, // Flag untuk menandakan sukses update
    val newLibraryId: Long? = null // Menyimpan ID library baru setelah diupdate
)

@HiltViewModel
class YourWishlistBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistBookUiState())
    val uiState: StateFlow<WishlistBookUiState> = _uiState.asStateFlow()

    /**
     * Memuat data awal: detail buku dan data library-nya berdasarkan bookId.
     */
    fun loadData(bookId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Ambil data buku dari server berdasarkan bookId
                val bookResult = bookRepository.getBookById(bookId)

                // 2. Ambil semua data library milik user dan cari yang cocok dengan bookId
                // Catatan: Ini bisa dioptimalkan di backend dengan endpoint
                // yang bisa mencari userLibrary berdasarkan book_id.
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                val userLibraryResult = allLibraryItems.firstOrNull { it.bookId == bookId }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        book = bookResult,
                        userLibrary = userLibraryResult
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Mengubah status buku dari PLAN_TO_READ menjadi 'READING'.
     */
    fun startReading() {
        // Ambil data userLibrary saat ini dari state
        val currentLibrary = _uiState.value.userLibrary ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Buat salinan objek dengan status yang diperbarui
                val updatedEntry = currentLibrary.copy(
                    status = BookStatus.READING,
                    updatedAt = Date() // Perbarui tanggal
                )

                // Kirim pembaruan ke server melalui repository
                val result = userLibraryRepository.upsertUserLibrary(updatedEntry)

                // Update state untuk menandakan sukses dan simpan ID baru/terupdate
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        startReadingSuccess = true,
                        newLibraryId = result.id // Simpan ID untuk navigasi
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
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
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// State untuk UI, menggabungkan semua data yang dibutuhkan layar
data class WishlistBookUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val userLibrary: UserLibrary? = null,
    val startReadingSuccess: Boolean = false // Flag untuk menandakan sukses
)

@HiltViewModel
class YourWishlistBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistBookUiState())
    val uiState: StateFlow<WishlistBookUiState> = _uiState.asStateFlow()

    /**
     * Memuat data awal: detail buku dan data library-nya.
     */
    fun loadData(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = WishlistBookUiState(isLoading = true)
            try {
                // Ambil data buku dari server
                val book = bookRepository.getBookById(bookId)

                // Ambil semua data library dan cari yang cocok
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                val userLibrary = allLibraryItems.firstOrNull { it.bookId == bookId }

                _uiState.value = WishlistBookUiState(
                    isLoading = false,
                    book = book,
                    userLibrary = userLibrary
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Mengubah status buku menjadi 'READING'.
     */
    fun startReading() {
        val currentLibrary = _uiState.value.userLibrary ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Buat salinan objek dengan status yang diperbarui
                val updatedEntry = currentLibrary.copy(
                    status = BookStatus.READING,
                    updatedAt = Date()
                )
                // Kirim pembaruan ke server
                userLibraryRepository.upsertUserLibrary(updatedEntry)

                // Update state untuk menandakan sukses
                _uiState.value = _uiState.value.copy(isLoading = false, startReadingSuccess = true)

            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
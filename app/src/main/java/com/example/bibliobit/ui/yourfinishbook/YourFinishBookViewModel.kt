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

    private val _uiState = MutableStateFlow(FinishBookUiState())
    val uiState: StateFlow<FinishBookUiState> = _uiState.asStateFlow()

    fun loadData(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = FinishBookUiState(isLoading = true)
            try {
                // Ambil data buku dan data library secara bersamaan
                val book = bookRepository.getBookById(bookId)
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                val userLibrary = allLibraryItems.firstOrNull { it.bookId == bookId }

                _uiState.value = FinishBookUiState(
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
     * Mengubah status buku kembali menjadi 'READING'
     * dan mengembalikan UserLibrary yang telah diperbarui.
     */
    suspend fun readAgain(): UserLibrary? {
        val currentLibrary = _uiState.value.userLibrary ?: return null

        return try {
            val updatedEntry = currentLibrary.copy(
                status = BookStatus.READING,
                lastPageRead = 0, // Reset progres
                rating = null, // Reset rating
                updatedAt = Date()
            )
            // Kirim pembaruan ke server
            userLibraryRepository.upsertUserLibrary(updatedEntry)
        } catch (e: Exception) {
            null
        }
    }
}
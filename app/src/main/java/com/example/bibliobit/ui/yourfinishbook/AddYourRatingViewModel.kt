package com.example.bibliobit.ui.yourfinishbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// State untuk UI, hanya menampung data yang relevan
data class AddRatingUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userLibrary: UserLibrary? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddYourRatingViewModel @Inject constructor(
    // Cukup butuh UserLibraryRepository untuk mengambil data dan menyimpan rating
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRatingUiState())
    val uiState: StateFlow<AddRatingUiState> = _uiState.asStateFlow()

    /**
     * Memuat data UserLibrary spesifik berdasarkan bookId.
     */
    fun loadUserLibrary(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = AddRatingUiState(isLoading = true)
            try {
                // Ambil seluruh daftar library dari server
                val allLibraryItems = userLibraryRepository.getUserLibrary()
                // Cari item yang cocok berdasarkan bookId
                val targetItem = allLibraryItems.firstOrNull { it.bookId == bookId }

                if (targetItem != null) {
                    _uiState.value = AddRatingUiState(isLoading = false, userLibrary = targetItem)
                } else {
                    _uiState.value = AddRatingUiState(isLoading = false, error = "Book not found in your library.")
                }
            } catch (e: Exception) {
                _uiState.value = AddRatingUiState(isLoading = false, error = "Failed to load data: ${e.message}")
            }
        }
    }

    /**
     * Menyimpan rating baru ke server.
     */
    fun saveRating(rating: Float) {
        val currentLibraryItem = _uiState.value.userLibrary ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Buat salinan objek dengan rating yang sudah diperbarui
                val updatedItem = currentLibraryItem.copy(rating = rating)

                // Kirim pembaruan ke server melalui repository
                userLibraryRepository.upsertUserLibrary(updatedItem)

                // Update state untuk menandakan sukses
                _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to save rating: ${e.message}")
            }
        }
    }
}
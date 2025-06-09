package com.example.bibliobit.ui.readingprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.ReadingProgress
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ReadingProgressUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val book: Book? = null,
    val userLibrary: UserLibrary? = null,
    val progressHistory: List<ReadingProgress> = emptyList()
)

@HiltViewModel
class ReadingProgressViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingProgressUiState())
    val uiState: StateFlow<ReadingProgressUiState> = _uiState.asStateFlow()

    fun loadData(userLibraryId: Long) {
        viewModelScope.launch {
            _uiState.value = ReadingProgressUiState(isLoading = true)
            try {
                val userLibrary = userLibraryRepository.getUserLibraryById(userLibraryId)
                val book = bookRepository.getBookById(userLibrary.bookId)
                val progressHistory = readingProgressRepository.getReadingProgressByUserLibraryId(userLibraryId)
                _uiState.value = ReadingProgressUiState(
                    isLoading = false,
                    userLibrary = userLibrary,
                    book = book,
                    progressHistory = progressHistory
                )
            } catch (e: Exception) {
                _uiState.value = ReadingProgressUiState(isLoading = false, error = "Gagal memuat data: ${e.message}")
            }
        }
    }

    /**
     * ## FUNGSI DIPERBARUI ##
     * Menambahkan entri progres baru DAN menangani jika buku sudah selesai.
     */
    fun addReadingProgress(
        userLibraryId: Long,
        pageRead: Int,
        recordedAt: Date,
        isFinished: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 1. Kirim progres baca baru ke server
                val newProgress = ReadingProgress(userLibraryId, pageRead, recordedAt)
                readingProgressRepository.insert(newProgress)

                // 2. Jika status buku berubah (misalnya menjadi FINISH), update juga UserLibrary
                val currentLibraryEntry = _uiState.value.userLibrary
                if (currentLibraryEntry != null) {
                    val newStatus = if (isFinished) BookStatus.FINISH else BookStatus.READING
                    if (currentLibraryEntry.status != newStatus || currentLibraryEntry.lastPageRead != pageRead) {
                        val updatedEntry = currentLibraryEntry.copy(
                            lastPageRead = pageRead,
                            status = newStatus,
                            updatedAt = recordedAt
                        )
                        userLibraryRepository.upsertUserLibrary(updatedEntry)
                    }
                }

                // 3. Muat ulang semua data agar UI konsisten
                loadData(userLibraryId)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Gagal menyimpan progres: ${e.message}")
            }
        }
    }
}
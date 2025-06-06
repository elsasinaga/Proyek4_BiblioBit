package com.example.bibliobit.ui.bookdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.UserDao
import com.example.bibliobit.data.repository.UserLibraryRepository
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Date
import javax.inject.Inject

data class UiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusUpdated: Boolean = false
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun getBookById(bookId: Long): Flow<Book?> {
        return bookRepository.getBookById(bookId)
    }

    fun loadUserLibrary(userId: String, bookId: Long) {
        viewModelScope.launch {
            try {
                Log.d("BookDetailViewModel", "Loading UserLibrary for userId=$userId, bookId=$bookId")
                val existingEntry = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                _userLibrary.value = existingEntry
                Log.d("BookDetailViewModel", "Loaded UserLibrary: $existingEntry")
            } catch (e: Exception) {
                Log.e("BookDetailViewModel", "Error loading UserLibrary: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Error loading UserLibrary: ${e.message}")
            }
        }
    }

    fun updateBookStatus(
        userId: String,
        bookId: Long,
        status: BookStatus,
        lastPageRead: Int? = null,
        rating: Float? = null,
        onSyncRequired: suspend (UserLibrary) -> Unit // Ubah ke suspend function
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                Log.d("BookDetailViewModel", "Updating book status: userId=$userId, bookId=$bookId, status=$status")
                val userExists = userDao.getUserById(userId) != null
                if (!userExists) {
                    Log.e("BookDetailViewModel", "User with userId=$userId doesn't exist")
                    _uiState.value = _uiState.value.copy(error = "User not found")
                    return@launch
                }
                val bookExists = bookRepository.getBookById(bookId).firstOrNull() != null
                if (!bookExists) {
                    Log.e("BookDetailViewModel", "Book with bookId=$bookId does not exist")
                    _uiState.value = _uiState.value.copy(error = "Book not found")
                    return@launch
                }
                val existingEntry = userLibraryRepository.getUserLibraryByBookId(userId, bookId)
                val updatedEntry = if (existingEntry != null) {
                    existingEntry.copy(
                        status = status,
                        lastPageRead = if (status == BookStatus.READING) lastPageRead else null,
                        rating = if (status == BookStatus.FINISH) rating else null,
                        updatedAt = Date()
                    )
                } else {
                    UserLibrary(
                        userId = userId,
                        bookId = bookId,
                        status = status,
                        lastPageRead = if (status == BookStatus.READING) lastPageRead else null,
                        updatedAt = Date(),
                        rating = if (status == BookStatus.FINISH) rating else null
                    )
                }
                if (existingEntry != null) {
                    userLibraryRepository.update(updatedEntry)
                    Log.d("BookDetailViewModel", "Updated existing UserLibrary: $updatedEntry")
                } else {
                    userLibraryRepository.insert(updatedEntry)
                    Log.d("BookDetailViewModel", "Inserted new UserLibrary: $updatedEntry")
                }
                _userLibrary.value = updatedEntry
                if (existingEntry == null || existingEntry.status != status) {
                    try {
                        onSyncRequired(updatedEntry) // Panggil suspend function di dalam coroutine
                        _uiState.value = _uiState.value.copy(statusUpdated = true, error = null)
                    } catch (e: JsonSyntaxException) {
                        Log.e("BookDetailViewModel", "JSON parsing error: ${e.message}", e)
                        _uiState.value = _uiState.value.copy(error = "Format respons server tidak valid")
                    } catch (e: HttpException) {
                        Log.e("BookDetailViewModel", "HTTP error: ${e.code()} - ${e.message}", e)
                        _uiState.value = _uiState.value.copy(error = "Error HTTP: ${e.code()} - ${e.message}")
                    } catch (e: Exception) {
                        Log.e("BookDetailViewModel", "Error syncing: ${e.message}", e)
                        _uiState.value = _uiState.value.copy(error = "Gagal sinkronisasi: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("BookDetailViewModel", "Error updating book status: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Error updating status: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
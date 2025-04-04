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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _userLibrary = MutableStateFlow<UserLibrary?>(null)
    val userLibrary: StateFlow<UserLibrary?> = _userLibrary.asStateFlow()

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
            }
        }
    }

    fun updateBookStatus(
        userId: String,
        bookId: Long,
        status: BookStatus,
        lastPageRead: Int? = null,
        rating: Float? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d("BookDetailViewModel", "Updating book status: userId=$userId, bookId=$bookId, status=$status")
                // Validasi userId
                val userExists = userDao.getUser(userId) != null
                if (!userExists) {
                    Log.e("BookDetailViewModel", "User with userId=$userId does not exist in users table")
                    return@launch
                }
                // Validasi bookId
                val bookExists = bookRepository.getBookById(bookId).firstOrNull() != null
                if (!bookExists) {
                    Log.e("BookDetailViewModel", "Book with bookId=$bookId does not exist in books table")
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
            } catch (e: Exception) {
                Log.e("BookDetailViewModel", "Error updating book status: ${e.message}", e)
                throw e // Untuk debugging
            }
        }
    }
}
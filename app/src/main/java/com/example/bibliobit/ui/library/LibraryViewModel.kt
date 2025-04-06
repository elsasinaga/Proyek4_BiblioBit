package com.example.bibliobit.ui.library

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    private val _filter = MutableStateFlow("all")
    private val _searchQuery = MutableStateFlow("")

    private val _libraryItems = MutableStateFlow<List<Pair<Book, UserLibrary>>>(emptyList())
    val libraryItems: StateFlow<List<Pair<Book, UserLibrary>>> = _libraryItems.asStateFlow()

    fun setUserId(userId: String) {
        _userId.value = userId
        loadLibraryItems()
    }

    fun setFilter(filter: String) {
        _filter.value = filter
        loadLibraryItems()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadLibraryItems()
    }

    fun deleteBookFromLibrary(bookId: Long) {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            try {
                userLibraryRepository.deleteUserLibrary(userId, bookId)
                Log.d("LibraryViewModel", "Book with ID $bookId deleted from library for user $userId")
                // Reload library items setelah penghapusan
                loadLibraryItems()
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error deleting book from library: ${e.message}", e)
            }
        }
    }

    private fun loadLibraryItems() {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            try {
                Log.d(
                    "LibraryViewModel",
                    "Loading library items for userId=$userId, filter=${_filter.value}, query=${_searchQuery.value}"
                )

                val libraryFlow = when (_filter.value) {
                    "all" -> {
                        if (_searchQuery.value.isEmpty()) {
                            userLibraryRepository.getUserLibrary(userId)
                        } else {
                            userLibraryRepository.searchUserLibrary(userId, _searchQuery.value)
                        }
                    }

                    "wishlist" -> {
                        if (_searchQuery.value.isEmpty()) {
                            userLibraryRepository.getUserLibraryByStatus(
                                userId,
                                BookStatus.PLAN_TO_READ
                            )
                        } else {
                            userLibraryRepository.searchUserLibrary(userId, _searchQuery.value)
                                .map { items -> items.filter { it.status == BookStatus.PLAN_TO_READ } }
                        }
                    }

                    "reading" -> {
                        if (_searchQuery.value.isEmpty()) {
                            userLibraryRepository.getUserLibraryByStatus(userId, BookStatus.READING)
                        } else {
                            userLibraryRepository.searchUserLibrary(userId, _searchQuery.value)
                                .map { items -> items.filter { it.status == BookStatus.READING } }
                        }
                    }

                    "finish" -> {
                        if (_searchQuery.value.isEmpty()) {
                            userLibraryRepository.getUserLibraryByStatus(userId, BookStatus.FINISH)
                        } else {
                            userLibraryRepository.searchUserLibrary(userId, _searchQuery.value)
                                .map { items -> items.filter { it.status == BookStatus.FINISH } }
                        }
                    }

                    else -> userLibraryRepository.getUserLibrary(userId)
                }

                libraryFlow
                    .map { userLibraryList ->
                        userLibraryList.mapNotNull { userLibrary ->
                            val book = bookRepository.getBookById(userLibrary.bookId).firstOrNull()
                            book?.let { Pair(it, userLibrary) }
                        }
                    }
                    .collect { items ->
                        if (isActive) { // Pastikan coroutine masih aktif
                            _libraryItems.value = items
                            Log.d("LibraryViewModel", "Loaded ${items.size} library items")
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d("LibraryViewModel", "Loading cancelled, ignoring")
                } else {
                    Log.e("LibraryViewModel", "Error loading library items: ${e.message}", e)
                }
            }
        }
    }
}
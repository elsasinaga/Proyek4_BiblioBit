package com.example.bibliobit.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.BookStatus
import com.example.bibliobit.data.model.UserLibrary
import com.example.bibliobit.data.repository.UserLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow<BookStatus?>(null)
    val filter: StateFlow<BookStatus?> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _libraryItems = MutableStateFlow<List<UserLibrary>>(emptyList())
    val libraryItems: StateFlow<List<UserLibrary>> = _libraryItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var fetchJob: Job? = null
    private var isInitialLoadDone = false

    fun setUserId(userId: String) {
        if (!isInitialLoadDone) {
            loadLibraryItems()
            isInitialLoadDone = true
        }
    }

    // Fungsi setFilter dikembalikan seperti semula agar aman
    fun setFilter(status: BookStatus?) {
        if (_filter.value != status) {
            _filter.value = status
            loadLibraryItems()
        }
    }

    // Fungsi setSearchQuery tidak diubah
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadLibraryItems()
    }

    // ===================================================================
    // PERBAIKAN: Fungsi baru ditambahkan di sini
    // ===================================================================
    /**
     * Fungsi publik yang aman untuk memuat ulang data dari UI.
     */
    fun refreshLibrary() {
        loadLibraryItems()
    }
    // ===================================================================

    fun deleteBookFromLibrary(libraryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userLibraryRepository.deleteUserLibrary(libraryId)
                loadLibraryItems() // Muat ulang daftar setelah menghapus
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error deleting book: ${e.message}", e)
                _errorMessage.value = "Failed to delete book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi loadLibraryItems dikembalikan menjadi private lagi
    private fun loadLibraryItems() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val statusFilter: String? = _filter.value?.name
                val query = _searchQuery.value.ifEmpty { null }
                val items = userLibraryRepository.getUserLibrary(status = statusFilter, query = query)
                _libraryItems.value = items
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading library items: ${e.message}", e)
                _errorMessage.value = "Failed to load library: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
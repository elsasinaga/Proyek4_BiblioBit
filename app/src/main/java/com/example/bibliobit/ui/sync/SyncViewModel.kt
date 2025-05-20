package com.example.bibliobit.ui.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.User
import com.example.bibliobit.data.repository.AuthRepository
import com.example.bibliobit.data.repository.BookRepository
import com.example.bibliobit.data.repository.NoteRepository
import com.example.bibliobit.data.repository.ReadingProgressRepository
import com.example.bibliobit.data.repository.UserLibraryRepository
import com.example.bibliobit.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(NetworkUtils.isOnline(context))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun syncAllData() {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(context)) {
                _syncStatus.value = "Offline, sync will start when online"
                return@launch
            }

            _isSyncing.value = true
            _syncStatus.value = null
            try {
                authRepository.syncLocalUser(User("", "", "", "")) // Placeholder User
                bookRepository.syncUnsyncedBooks()
                bookRepository.syncBooksFromServer()
                userLibraryRepository.syncUnsyncedUserLibrary()
                userLibraryRepository.syncUserLibraryFromServer()
                readingProgressRepository.syncUnsyncedReadingProgress()
                readingProgressRepository.syncReadingProgressFromServer()
                noteRepository.syncUnsyncedNotes()
                noteRepository.syncNotesFromServer()

                _syncStatus.value = "Sync completed successfully"
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun checkNetworkStatus() {
        _isOnline.value = NetworkUtils.isOnline(context)
    }
}
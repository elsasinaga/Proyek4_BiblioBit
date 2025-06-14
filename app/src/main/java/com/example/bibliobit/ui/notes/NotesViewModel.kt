package com.example.bibliobit.ui.notes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliobit.data.model.Note
import com.example.bibliobit.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private var currentUserLibraryId: Long = 0

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _imageUriForInput = MutableStateFlow<Uri?>(null)
    val imageUriForInput: StateFlow<Uri?> = _imageUriForInput.asStateFlow()

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initialize(userLibraryId: Long) {
        if (userLibraryId != currentUserLibraryId) {
            currentUserLibraryId = userLibraryId
            loadNotes()
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Panggilan ini sekarang akan menuju URL yang benar
                _notes.value = noteRepository.getNotesByUserLibraryId(currentUserLibraryId)
            } catch (e: Exception) {
                // Menampilkan pesan error 404 atau lainnya di UI
                _errorMessage.value = "Gagal memuat catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setImageUri(uri: Uri?) {
        _imageUriForInput.value = uri
    }

    fun saveOrUpdateNote(content: String) {
        val noteToEdit = _editingNote.value
        if (noteToEdit != null) {
            noteToEdit.id?.let { updateNote(it, content) }
        } else {
            addNote(content)
        }
    }

    private fun addNote(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                noteRepository.addNote(currentUserLibraryId, content, _imageUriForInput.value)
                resetInputAndRefresh()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menyimpan catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateNote(noteId: Long, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Untuk update, kita kirim URI hanya jika diubah (bukan URL http).
                // Jika tidak diubah, kirim null agar gambar lama tidak terhapus.
                val uriToSend = if (_imageUriForInput.value.toString().startsWith("http")) null else _imageUriForInput.value
                noteRepository.updateNote(noteId, content, uriToSend)
                resetInputAndRefresh()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                note.id?.let { noteRepository.deleteNote(it) }
                loadNotes() // Muat ulang setelah menghapus
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus catatan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startEditing(note: Note) {
        _editingNote.value = note
        // Jika catatan punya gambar, tampilkan dari URL lengkapnya
        _imageUriForInput.value = note.imageUrl?.let { Uri.parse(it) }
    }

    fun cancelEditing() {
        resetInputAndRefresh()
    }

    private fun resetInputAndRefresh() {
        _editingNote.value = null
        _imageUriForInput.value = null
        loadNotes()
    }
}

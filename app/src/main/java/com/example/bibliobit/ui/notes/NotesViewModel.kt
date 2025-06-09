//package com.example.bibliobit.ui.notes
//
//import android.content.Context
//import android.net.Uri
//import androidx.core.content.FileProvider
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.bibliobit.data.model.Note
//import com.example.bibliobit.data.repository.NoteRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.FileOutputStream
//import java.io.IOException
//import java.util.Date
//import javax.inject.Inject
//
//@HiltViewModel
//class NotesViewModel @Inject constructor(
//    private val noteRepository: NoteRepository,
//    @ApplicationContext private val context: Context
//) : ViewModel() {
//
//    private val _notes = MutableStateFlow<List<Note>>(emptyList())
//    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
//
//    private val _imageUri = MutableStateFlow<Uri?>(null)
//    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
//
//    private val _editingNote = MutableStateFlow<Note?>(null)
//    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()
//
//    var tempPhotoUri: Uri? = null
//        private set
//    private var tempPhotoFile: File? = null
//
//    fun initialize(userLibraryId: Long) {
//        viewModelScope.launch {
//            println("Initializing NotesViewModel with userLibraryId: $userLibraryId")
//            noteRepository.getNotesByUserLibraryId(userLibraryId)
//                .catch { e ->
//                    println("Error fetching notes: ${e.message}")
//                    emit(emptyList())
//                }
//                .collect { notes ->
//                    println("Received notes from repository: $notes")
//                    _notes.value = notes
//                }
//        }
//    }
//
//    fun launchCamera(): Uri? {
//        val timestamp = System.currentTimeMillis()
//        val tempFileName = "camera_photo_$timestamp.jpg"
//        val tempFile = File(context.filesDir, tempFileName)
//
//        // Pastikan direktori ada
//        val parentDir = tempFile.parentFile
//        if (parentDir != null && !parentDir.exists()) {
//            val created = parentDir.mkdirs()
//            println("Parent directory created: $created")
//        }
//
//        println("Launching camera")
//        println("Temporary photo URI: ${FileProvider.getUriForFile(context, "com.example.bibliobit.fileprovider", tempFile)}")
//        println("Temporary file path: ${tempFile.absolutePath}")
//        println("Temporary file exists before taking picture: ${tempFile.exists()}")
//
//        tempPhotoFile = tempFile
//        tempPhotoUri = FileProvider.getUriForFile(
//            context,
//            "com.example.bibliobit.fileprovider",
//            tempFile
//        )
//        return tempPhotoUri
//    }
//
//    fun setImageUri(uri: Uri?) {
//        if (uri == null) {
//            println("Image URI is null after taking picture")
//            _imageUri.value = null
//            return
//        }
//
//        viewModelScope.launch {
//            try {
//                // Periksa apakah file sementara ada (khusus untuk kamera)
//                if (uri == tempPhotoUri) {
//                    println("Checking if temp file exists after taking picture: ${tempPhotoFile?.exists()}")
//                    if (tempPhotoFile?.exists() == true) {
//                        println("Temp file size: ${tempPhotoFile?.length()} bytes")
//                    } else {
//                        println("Temp file does not exist after taking picture")
//                    }
//                }
//
//                val savedImagePath = saveImageToInternalStorage(uri)
//                savedImagePath?.let { path ->
//                    val file = File(path)
//                    if (file.exists()) {
//                        val newUri = FileProvider.getUriForFile(
//                            context,
//                            "com.example.bibliobit.fileprovider",
//                            file
//                        )
//                        _imageUri.value = newUri
//                        println("Image URI set to: $newUri")
//                    } else {
//                        println("Saved image file does not exist: $path")
//                        _imageUri.value = null
//                    }
//                } ?: run {
//                    println("Failed to save image to internal storage")
//                    _imageUri.value = null
//                }
//            } catch (e: Exception) {
//                println("Error setting image URI: ${e.message}")
//                e.printStackTrace()
//                _imageUri.value = null
//            }
//        }
//    }
//
//    fun saveNote(userLibraryId: Long, content: String) {
//        viewModelScope.launch {
//            val imagePath = _imageUri.value?.let { uri ->
//                // Gambar sudah disimpan di penyimpanan internal saat setImageUri
//                val path = uri.path?.substringAfterLast("note_images/")
//                "/data/user/0/com.example.bibliobit/files/$path"
//            }
//            println("Saving note with image path: $imagePath")
//            imagePath?.let { path ->
//                val file = File(path)
//                println("Saved image file exists: ${file.exists()}")
//                if (file.exists()) {
//                    val uri = FileProvider.getUriForFile(
//                        context,
//                        "com.example.bibliobit.fileprovider",
//                        file
//                    )
//                    println("Generated URI after saving: $uri")
//                }
//            }
//            val note = Note(
//                userLibraryId = userLibraryId,
//                content = content,
//                image = imagePath,
//                createdAt = Date()
//            )
//            try {
//                noteRepository.insert(note)
//                println("Note saved: $note")
//            } catch (e: Exception) {
//                println("Error saving note: ${e.message}")
//            }
//            _imageUri.value = null
//        }
//    }
//
//    fun updateNote(noteId: Long, content: String) {
//        viewModelScope.launch {
//            val imagePath = _imageUri.value?.let { uri ->
//                val path = uri.path?.substringAfterLast("note_images/")
//                "/data/user/0/com.example.bibliobit/files/$path"
//            }
//            println("Updating note with image path: $imagePath")
//            imagePath?.let { path ->
//                val file = File(path)
//                println("Saved image file exists: ${file.exists()}")
//                if (file.exists()) {
//                    val uri = FileProvider.getUriForFile(
//                        context,
//                        "com.example.bibliobit.fileprovider",
//                        file
//                    )
//                    println("Generated URI after updating: $uri")
//                }
//            }
//            val updatedNote = _editingNote.value?.copy(
//                content = content,
//                image = imagePath ?: _editingNote.value?.image,
//                updatedAt = Date()
//            )
//            updatedNote?.let {
//                try {
//                    noteRepository.update(it)
//                    println("Note updated: $it")
//                    _editingNote.value = null
//                    _imageUri.value = null
//                } catch (e: Exception) {
//                    println("Error updating note: ${e.message}")
//                }
//            }
//        }
//    }
//
//    fun startEditing(note: Note) {
//        _editingNote.value = note
//        note.image?.let { imagePath ->
//            val file = File(imagePath)
//            println("Loading image from path: $imagePath")
//            println("Image file exists: ${file.exists()}")
//            if (file.exists()) {
//                val uri = FileProvider.getUriForFile(
//                    context,
//                    "com.example.bibliobit.fileprovider",
//                    file
//                )
//                println("Generated URI for editing: $uri")
//                _imageUri.value = uri
//            } else {
//                println("Image file does not exist: $imagePath")
//                _imageUri.value = null
//            }
//        } ?: run {
//            _imageUri.value = null
//        }
//    }
//
//    fun cancelEditing() {
//        _editingNote.value = null
//        _imageUri.value = null
//    }
//
//    fun debugAllNotes() {
//        viewModelScope.launch {
//            val allNotes = noteRepository.getAllNotes()
//            println("All notes in database: $allNotes")
//        }
//    }
//
//    private fun saveImageToInternalStorage(uri: Uri): String? {
//        return try {
//            val file = File(context.filesDir, "note_image_${System.currentTimeMillis()}.jpg")
//            println("Saving image to: ${file.absolutePath}")
//
//            // Pastikan direktori ada
//            val parentDir = file.parentFile
//            if (parentDir != null && !parentDir.exists()) {
//                val created = parentDir.mkdirs()
//                println("Parent directory created: $created")
//            }
//
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                FileOutputStream(file).use { output ->
//                    input.copyTo(output)
//                }
//            } ?: throw FileNotFoundException("Input stream is null for URI: $uri")
//
//            println("Image successfully saved to: ${file.absolutePath}")
//            file.absolutePath
//        } catch (e: FileNotFoundException) {
//            println("Error saving image: ${e.message}")
//            e.printStackTrace()
//            null
//        } catch (e: IOException) {
//            println("Error saving image: ${e.message}")
//            e.printStackTrace()
//            null
//        } catch (e: Exception) {
//            println("Error saving image: ${e.message}")
//            e.printStackTrace()
//            null
//        }
//    }
//}
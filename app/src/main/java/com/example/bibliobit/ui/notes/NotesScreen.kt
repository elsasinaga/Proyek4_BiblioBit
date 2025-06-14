package com.example.bibliobit.ui.notes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bibliobit.data.model.Note
import com.example.bibliobit.ui.theme.hijau2
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("camera_photo_", ".jpg", context.cacheDir)
    val authority = "com.example.bibliobit.fileprovider"
    return FileProvider.getUriForFile(context, authority, tempFile)
}

// --- Composable Utama ---
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    userLibraryId: Long,
    bookTitle: String,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val imageUriForInput by viewModel.imageUriForInput.collectAsState()
    val editingNote by viewModel.editingNote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher tetap sama
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.setImageUri(it) }
    }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) { tempCameraUri?.let { viewModel.setImageUri(it) } }
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) { Toast.makeText(context, "Izin ditolak", Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(editingNote) {
        content = editingNote?.content ?: ""
    }

    LaunchedEffect(userLibraryId) {
        viewModel.initialize(userLibraryId)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F8E8))
                .padding(16.dp)
        ) {
            NoteInputSection(
                bookTitle = bookTitle,
                content = content,
                onContentChange = { content = it },
                imageUri = imageUriForInput,
                isEditing = editingNote != null,
                onSaveClick = {
                    if (content.isNotBlank()) {
                        viewModel.saveOrUpdateNote(content)
                        content = ""
                    } else {
                        Toast.makeText(context, "Konten tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancelEditClick = { viewModel.cancelEditing() },
                onGalleryClick = { pickImageLauncher.launch("image/*") },
                onCameraClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            val newUri = createTempImageUri(context)
                            tempCameraUri = newUri
                            takePictureLauncher.launch(newUri)
                        }
                        else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && notes.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id!! }) { note ->
                        NoteItem(
                            note = note,
                            onEditClick = { viewModel.startEditing(note) },
                            onDeleteClick = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        if(isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun NoteInputSection(
    bookTitle: String,
    content: String,
    onContentChange: (String) -> Unit,
    imageUri: Uri?,
    isEditing: Boolean,
    onSaveClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Column {
        Text(text = bookTitle, style = MaterialTheme.typography.titleLarge, color = hitam, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.bodySmall, color = hitam)
            Text(text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.bodySmall, color = hitam)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().border(1.dp, hijau4, RoundedCornerShape(8.dp)),
            color = hijau2,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditing) "Edit Catatan" else "Tulis Catatan ...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = hitam
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    textStyle = TextStyle(color = hitam, fontSize = 16.sp)
                )

                if (imageUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Image, "Gallery", tint = hijau5, modifier = Modifier.clickable(onClick = onGalleryClick))
                        Icon(Icons.Default.CameraAlt, "Camera", tint = hijau5, modifier = Modifier.clickable(onClick = onCameraClick))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isEditing) {
                            IconButton(onClick = onCancelEditClick) {
                                Icon(Icons.Default.Cancel, "Cancel Edit", tint = Color.Gray)
                            }
                        }
                        Button(onClick = onSaveClick, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = hijau4)) {
                            Icon(Icons.Default.Check, contentDescription = "Save Note", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, hijau4, RoundedCornerShape(8.dp)),
        color = hijau2, shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dibuat: ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(note.createdAt)}", style = MaterialTheme.typography.bodySmall, color = hitam)
                    note.updatedAt?.let { Text("Diubah: ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it)}", style = MaterialTheme.typography.bodySmall, color = hitam.copy(alpha=0.6f)) }
                }
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, "Edit", tint = hijau5) }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha=0.7f)) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(note.content, style = MaterialTheme.typography.bodyMedium, color = hitam)

            note.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Note Image",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
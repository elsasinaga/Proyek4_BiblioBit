//package com.example.bibliobit.ui.notes
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Image
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.ColorPainter
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import coil.compose.AsyncImage
//import coil.compose.AsyncImagePainter
//import coil.request.ImageRequest
//import com.example.bibliobit.data.model.Note
//import com.example.bibliobit.ui.theme.hijau2
//import com.example.bibliobit.ui.theme.hijau4
//import com.example.bibliobit.ui.theme.hijau5
//import com.example.bibliobit.ui.theme.hitam
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NotesScreen(
//    userLibraryId: Long,
//    bookTitle: String,
//    viewModel: NotesViewModel,
//    onNavigateBack: () -> Unit
//) {
//    val notes by viewModel.notes.collectAsState()
//    val imageUri by viewModel.imageUri.collectAsState()
//    val editingNote by viewModel.editingNote.collectAsState()
//    var content by remember { mutableStateOf("") }
//    val context = LocalContext.current
//
//    // Launcher untuk memilih gambar dari galeri
//    val pickImageLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri ->
//        uri?.let {
//            println("Image selected from gallery: $it")
//            viewModel.setImageUri(it)
//        } ?: println("No image selected from gallery")
//    }
//
//    // Launcher untuk mengambil foto dari kamera
//    val takePictureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture()
//    ) { isSuccess ->
//        if (isSuccess) {
//            viewModel.tempPhotoUri?.let { uri ->
//                println("Camera capture success, URI: $uri")
//                // Periksa apakah file benar-benar ada
//                try {
//                    val inputStream = context.contentResolver.openInputStream(uri)
//                    println("File exists and can be opened: ${inputStream != null}")
//                    inputStream?.close()
//                    viewModel.setImageUri(uri) // Perbarui URI
//                } catch (e: Exception) {
//                    println("Error accessing file: ${e.message}")
//                    Toast.makeText(context, "Failed to access captured image", Toast.LENGTH_SHORT).show()
//                    viewModel.setImageUri(null)
//                }
//            } ?: run {
//                println("Camera capture failed: tempPhotoUri is null")
//                Toast.makeText(context, "Failed to capture image: URI is null", Toast.LENGTH_SHORT).show()
//                viewModel.setImageUri(null)
//            }
//        } else {
//            println("Failed to take picture")
//            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
//            viewModel.setImageUri(null) // Reset jika gagal
//        }
//    }
//
//    // Launcher untuk meminta izin penyimpanan
//    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            println("Storage permission granted")
//            pickImageLauncher.launch("image/*")
//        } else {
//            println("Storage permission denied")
//            Toast.makeText(context, "Izin penyimpanan ditolak", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Launcher untuk meminta izin kamera
//    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            println("Camera permission granted")
//            val photoUri = viewModel.launchCamera()
//            if (photoUri != null) {
//                takePictureLauncher.launch(photoUri)
//            } else {
//                Toast.makeText(context, "Failed to launch camera", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            println("Camera permission denied")
//            Toast.makeText(context, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Periksa apakah perangkat memiliki kamera
//    val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
//
//    // Isi form dengan data catatan yang sedang diedit
//    LaunchedEffect(editingNote) {
//        val currentEditingNote = editingNote
//        if (currentEditingNote != null) {
//            content = currentEditingNote.content
//        } else {
//            content = ""
//        }
//    }
//
//    LaunchedEffect(userLibraryId) {
//        println("NotesScreen launched with userLibraryId: $userLibraryId")
//        viewModel.initialize(userLibraryId)
//    }
//
//    LaunchedEffect(notes) {
//        println("Notes updated in UI: $notes")
//    }
//
//    // Gunakan variabel lokal untuk menghindari masalah smart cast
//    val isEditing = editingNote != null
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF1F8E8))
//            .padding(horizontal = 16.dp)
//    ) {
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text(
//            text = bookTitle,
//            style = MaterialTheme.typography.titleLarge,
//            color = hitam,
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()),
//                style = MaterialTheme.typography.bodySmall,
//                color = hitam
//            )
//            Text(
//                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
//                style = MaterialTheme.typography.bodySmall,
//                color = hitam
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = if (isEditing) "Edit Catatan" else "Tulisan Catatan ...",
//            style = MaterialTheme.typography.bodyMedium,
//            color = hitam
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .border(1.dp, hijau4, RoundedCornerShape(8.dp)),
//            color = hijau2,
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                BasicTextField(
//                    value = content,
//                    onValueChange = { content = it },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp),
//                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = hitam)
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Image,
//                            contentDescription = "Add Image from Gallery",
//                            tint = hijau5,
//                            modifier = Modifier
//                                .size(24.dp)
//                                .clickable {
//                                    val permissionToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
//                                        Manifest.permission.READ_MEDIA_IMAGES
//                                    } else {
//                                        Manifest.permission.READ_EXTERNAL_STORAGE
//                                    }
//
//                                    if (ContextCompat.checkSelfPermission(
//                                            context,
//                                            permissionToRequest
//                                        ) == PackageManager.PERMISSION_GRANTED
//                                    ) {
//                                        println("Launching gallery picker")
//                                        pickImageLauncher.launch("image/*")
//                                    } else {
//                                        println("Requesting storage permission: $permissionToRequest")
//                                        requestStoragePermissionLauncher.launch(permissionToRequest)
//                                    }
//                                }
//                        )
//
//                        Icon(
//                            imageVector = Icons.Default.CameraAlt,
//                            contentDescription = "Take Photo",
//                            tint = hijau5,
//                            modifier = Modifier
//                                .size(24.dp)
//                                .clickable {
//                                    if (hasCamera) {
//                                        if (ContextCompat.checkSelfPermission(
//                                                context,
//                                                Manifest.permission.CAMERA
//                                            ) == PackageManager.PERMISSION_GRANTED
//                                        ) {
//                                            println("Launching camera")
//                                            val photoUri = viewModel.launchCamera()
//                                            if (photoUri != null) {
//                                                takePictureLauncher.launch(photoUri)
//                                            } else {
//                                                Toast.makeText(context, "Failed to launch camera", Toast.LENGTH_SHORT).show()
//                                            }
//                                        } else {
//                                            println("Requesting camera permission")
//                                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
//                                        }
//                                    } else {
//                                        println("Device does not have a camera")
//                                        Toast.makeText(context, "Kamera tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                        )
//                    }
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        if (isEditing) {
//                            IconButton(
//                                onClick = {
//                                    viewModel.cancelEditing()
//                                    content = ""
//                                },
//                                modifier = Modifier
//                                    .background(Color.Gray, RoundedCornerShape(50))
//                                    .size(40.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Check,
//                                    contentDescription = "Cancel Edit",
//                                    tint = Color.White
//                                )
//                            }
//                        }
//
//                        IconButton(
//                            onClick = {
//                                if (content.isNotBlank()) {
//                                    println("Saving note with content: $content, imageUri: $imageUri")
//                                    val currentEditingNote = editingNote
//                                    if (currentEditingNote != null) {
//                                        viewModel.updateNote(currentEditingNote.id, content)
//                                    } else {
//                                        viewModel.saveNote(userLibraryId, content)
//                                    }
//                                    content = ""
//                                } else {
//                                    println("Content is blank, not saving note")
//                                }
//                            },
//                            modifier = Modifier
//                                .background(hijau4, RoundedCornerShape(50))
//                                .size(40.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = if (isEditing) "Update Note" else "Save Note",
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                if (imageUri != null) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    println("Attempting to display image with URI: $imageUri")
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(imageUri)
//                            .setParameter("cache_buster", System.currentTimeMillis())
//                            .build(),
//                        contentDescription = "Selected Image",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = 400.dp)
//                            .wrapContentHeight()
//                            .clip(RoundedCornerShape(8.dp)),
//                        contentScale = ContentScale.Fit,
//                        placeholder = ColorPainter(Color.Gray),
//                        error = ColorPainter(Color.Red),
//                        onLoading = { println("Image is loading") },
//                        onSuccess = { println("Image loaded successfully") },
//                        onError = { state -> println("Error loading image: ${state.result.throwable.message}") }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Tampilkan daftar catatan menggunakan LazyColumn
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            items(notes) { note ->
//                NoteItem(
//                    note = note,
//                    onEditClick = { viewModel.startEditing(note) }
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun NoteItem(
//    note: Note,
//    onEditClick: () -> Unit
//) {
//    val context = LocalContext.current
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(1.dp, hijau4, RoundedCornerShape(8.dp)),
//        color = hijau2,
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Text(
//                        text = "Created: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(note.createdAt)}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = hitam
//                    )
//                    if (note.updatedAt != null) {
//                        Text(
//                            text = "Updated: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(note.updatedAt)}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = hitam
//                        )
//                    }
//                }
//                IconButton(
//                    onClick = onEditClick,
//                    modifier = Modifier.size(24.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Edit,
//                        contentDescription = "Edit Note",
//                        tint = hijau5
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = note.content,
//                style = MaterialTheme.typography.bodyMedium,
//                color = hitam
//            )
//            note.image?.let { imagePath ->
//                println("Loading image from path: $imagePath")
//                val file = File(imagePath)
//                if (file.exists()) {
//                    val uri = FileProvider.getUriForFile(
//                        context,
//                        "com.example.bibliobit.fileprovider",
//                        file
//                    )
//                    println("Generated URI for NoteItem: $uri")
//                    Spacer(modifier = Modifier.height(8.dp))
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(uri)
//                            .setParameter("cache_buster", System.currentTimeMillis())
//                            .build(),
//                        contentDescription = "Note Image",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = 400.dp)
//                            .wrapContentHeight()
//                            .clip(RoundedCornerShape(8.dp)),
//                        contentScale = ContentScale.Fit,
//                        placeholder = ColorPainter(Color.Gray),
//                        error = ColorPainter(Color.Red),
//                        onLoading = { println("NoteItem image is loading") },
//                        onSuccess = { println("NoteItem image loaded successfully") },
//                        onError = { state -> println("Error loading NoteItem image: ${state.result.throwable.message}") }
//                    )
//                } else {
//                    println("Image file does not exist: $imagePath")
//                }
//            }
//        }
//    }
//}
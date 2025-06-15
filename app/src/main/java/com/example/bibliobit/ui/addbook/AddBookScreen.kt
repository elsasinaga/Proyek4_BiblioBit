package com.example.bibliobit.ui.addbook

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.R
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.data.model.GoogleBook
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hitam
import com.example.bibliobit.ui.theme.putih
import kotlinx.coroutines.withContext

@Composable
fun AddBookScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AddBookViewModel
) {
    var showAddBookDialog by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    // --- DIPERBAIKI: Menggunakan searchResults dari Google API ---
    val searchResults by viewModel.searchResults.collectAsState()
//    val books by viewModel.books.collectAsState() // Dihilangkan initial value agar mengikuti state dari ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                placeholder = { Text("Search books...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = hijau4) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            IconButton(
                onClick = { /* Placeholder untuk fungsi scan */ },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.barcode_scanner),
                    contentDescription = "Scan Book",
                    tint = hijau4
                )
            }

            IconButton(
                onClick = { showAddBookDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Book Manually",
                    tint = hijau4
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            searchResults.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "Gunakan pencarian untuk menemukan buku baru." else "Buku tidak ditemukan untuk \"$searchQuery\"",
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(searchResults) { googleBook ->
                        GoogleBookItem(
                            googleBook = googleBook,
                            // --- AKSI KLIK DIPERBARUI ---
                            onClick = {
                                scope.launch {
                                    val bookId = viewModel.selectBookFromSearch(googleBook)
                                    if (bookId != -1L) {
                                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                                    } else {
                                        // Opsional: Tampilkan pesan error jika terjadi kegagalan
                                        Toast.makeText(context, "Gagal membuka detail buku", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAddBook = { title, author, genre, year, description, isbn, page, publisher, coverPhotoUri ->
                val coverPhotoPath = coverPhotoUri?.let { uri ->
                    FileUtils.savePhotoToInternalStorage(context, uri)
                }

                val book = Book(
                    title = title,
                    author = author,
                    genre = genre,
                    year = year,
                    description = description,
                    isbn = isbn,
                    pages = page,
                    publisher = publisher,
                    coverPhotoPath = coverPhotoPath
                )

                CoroutineScope(Dispatchers.IO).launch {
                    // ## DIPERBAIKI ##
                    // Menggunakan nama fungsi yang baru dari ViewModel
                    val bookId = viewModel.createBook(book)
                    withContext(Dispatchers.Main) {
                        if (bookId > 0) {
                            navController.navigate(Screen.BookDetail.createRoute(bookId))
                        }
                        showAddBookDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onAddBook: (String, String, String?, Int?, String?, String?, Int, String?, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var coverPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true

        if (storageGranted || cameraGranted) {
            showPhotoSourceDialog = true
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverPhotoUri = uri
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // URI sudah diset di coverPhotoUri saat membuat URI untuk kamera
        }
    }

    fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.CAMERA)
        permissionLauncher.launch(permissions.toTypedArray())
    }

    fun takePhoto(context: Context, onUriCreated: (Uri) -> Unit) {
        val photoFile = File(
            context.filesDir,
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )
        photoFile.parentFile?.mkdirs()
        val photoUri = FileProvider.getUriForFile(
            context,
            "com.example.bibliobit.fileprovider",
            photoFile
        )
        onUriCreated(photoUri)
        takePictureLauncher.launch(photoUri)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .width(850.dp)
                .heightIn(max = 600.dp)
                .padding(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Book Manually",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (coverPhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(coverPhotoUri),
                        contentDescription = "Book Cover Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable {
                                requestPermissions()
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Button(
                        onClick = {
                            requestPermissions()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Cover Photo")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = title.isBlank()
                )
                if (title.isBlank()) {
                    Text(
                        text = "Title is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = author.isBlank()
                )
                if (author.isBlank()) {
                    Text(
                        text = "Author is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = page,
                    onValueChange = { page = it },
                    label = { Text("Pages") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    isError = (page.toIntOrNull() == null || page.toIntOrNull()!! <= 0)
                )
                if (page.isBlank()) {
                    Text(
                        text = "Pages is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                } else if (page.isNotBlank() && (page.toIntOrNull() == null || page.toIntOrNull()!! <= 0)) {
                    Text(
                        text = "Please enter a valid number greater than 0",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Publisher (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && author.isNotBlank() && page.isNotBlank() && page.toIntOrNull() != null && page.toInt() > 0) {
                                onAddBook(
                                    title,
                                    author,
                                    genre.takeIf { it.isNotBlank() },
                                    year.toIntOrNull(),
                                    description.takeIf { it.isNotBlank() },
                                    isbn.takeIf { it.isNotBlank() },
                                    page.toInt(),
                                    publisher.takeIf { it.isNotBlank() },
                                    coverPhotoUri
                                )
                            }
                        },
                        enabled = title.isNotBlank() && author.isNotBlank() && page.isNotBlank() && page.toIntOrNull() != null && page.toIntOrNull()!! > 0
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Select Photo Source") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                            showPhotoSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery")
                    }
                    TextButton(
                        onClick = {
                            takePhoto(context) { uri ->
                                coverPhotoUri = uri
                            }
                            showPhotoSourceDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo with Camera")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Composable Baru untuk menampilkan hasil pencarian ---
/**
 * Composable baru untuk menampilkan setiap item buku dari hasil pencarian Google.
 */
@Composable
fun GoogleBookItem(
    googleBook: GoogleBook,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }, // Seluruh item bisa diklik
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            modifier = Modifier.width(140.dp).height(210.dp),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            if (!googleBook.coverPhotoPath.isNullOrBlank()) {
                val imageModel = if (googleBook.coverPhotoPath.startsWith("http")) {
                    googleBook.coverPhotoPath.replace("http://", "https://")
                } else {
                    File(googleBook.coverPhotoPath)
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = googleBook.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "No Cover",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = googleBook.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = hitam, // ganti dengan MaterialTheme.colorScheme.onBackground jika perlu
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = googleBook.author,
            style = MaterialTheme.typography.bodySmall,
            color = hijau4, // ganti dengan MaterialTheme.colorScheme.secondary jika perlu
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

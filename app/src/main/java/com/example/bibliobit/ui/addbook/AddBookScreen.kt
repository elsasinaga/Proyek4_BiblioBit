package com.example.bibliobit.ui.addbook

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.R
import com.example.bibliobit.data.model.Book
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

@Composable
fun AddBookScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AddBookViewModel
) {
    var showAddBookDialog by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val books by viewModel.books.collectAsState(initial = emptyList())
    val context = LocalContext.current

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
                onValueChange = { query ->
                    viewModel.updateSearchQuery(query) // Perbarui query di ViewModel
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .padding(horizontal = 2.dp)
                    .padding(top = 3.dp),
                placeholder = { Text("Search books...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = hijau4
                    )
                },
                shape = RoundedCornerShape(12.dp)
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

        if (books.isEmpty()) {
            Text(
                text = "No books added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(books) { book ->
                    BookItem(
                        book = book,
                        onClick = {
                            navController.navigate(Screen.BookDetail.createRoute(book.id))
                        }
                    )
                }
            }
        }
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAddBook = { title, author, genre, year, description, isbn, page, publisher, coverPhotoUri ->
                // Simpan foto ke penyimpanan internal dan dapatkan path-nya
                val coverPhotoPath = coverPhotoUri?.let { uri ->
                    FileUtils.savePhotoToInternalStorage(context, uri)
                }

                // Buat objek Book
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

                // Simpan ke database di background thread
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.insertBook(book)
                }

                showAddBookDialog = false
            }
        )
    }
}

@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.Start
    ) {
        if (book.coverPhotoPath != null) {
            Image(
                painter = rememberAsyncImagePainter(book.coverPhotoPath),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp)),
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

        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = hitam,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = hijau4,
            ),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onAddBook: (String, String, String?, Int?, String?, String?, Int?, String?, Uri?) -> Unit
) {
    // State untuk input form
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var coverPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // State untuk menentukan apakah dialog pemilihan sumber foto ditampilkan
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    // Launcher untuk meminta izin
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
        } else {
            // Tampilkan pesan bahwa izin diperlukan
        }
    }

    // Launcher untuk memilih foto dari galeri
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverPhotoUri = uri
    }

    // Launcher untuk mengambil foto dari kamera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // URI sudah diset di coverPhotoUri saat membuat URI untuk kamera
        }
    }

    // Fungsi untuk meminta izin
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

    // Fungsi untuk mengambil foto dari kamera (tanpa @Composable)
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

                // Pratinjau gambar dan tombol untuk memilih foto
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
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

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
                    label = { Text("Pages (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

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
                            if (title.isNotBlank() && author.isNotBlank()) {
                                onAddBook(
                                    title,
                                    author,
                                    genre.takeIf { it.isNotBlank() },
                                    year.toIntOrNull(),
                                    description.takeIf { it.isNotBlank() },
                                    isbn.takeIf { it.isNotBlank() },
                                    page.toIntOrNull(),
                                    publisher.takeIf { it.isNotBlank() },
                                    coverPhotoUri
                                )
                            }
                        },
                        enabled = title.isNotBlank() && author.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }

    // Dialog untuk memilih sumber foto (galeri atau kamera)
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
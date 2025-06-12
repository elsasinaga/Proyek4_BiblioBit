package com.example.bibliobit.ui.readingprogress

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddReadingProgressScreen(
    userLibraryId: Long,
    bookTitle: String,
    totalPages: Int,
    viewModel: ReadingProgressViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // State untuk input form
    var currentPage by remember { mutableStateOf("") }
    var recordedDate by remember { mutableStateOf(Date()) }
    var isFinished by remember { mutableStateOf(false) }

    // Panggil `loadData` hanya sekali saat layar dibuat
    LaunchedEffect(key1 = userLibraryId) {
        viewModel.loadData(userLibraryId)
    }

    val datePickerDialog = rememberDatePickerDialog(context) { newDate ->
        recordedDate = newDate
    }

    Dialog(onDismissRequest = { navController.popBackStack() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Your Progress", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                // Input Halaman Saat Ini
                OutlinedTextField(
                    value = currentPage,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.toIntOrNull() != null && newValue.toInt() <= totalPages)) {
                            currentPage = newValue
                        }
                    },
                    label = { Text("Page You've Read To") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("/ $totalPages", modifier = Modifier.padding(end = 12.dp)) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Input Tanggal Progres Dicatat
                OutlinedTextField(
                    value = remember(recordedDate) {
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(recordedDate)
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date Recorded") },
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox Selesai Baca
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isFinished = !isFinished },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isFinished, onCheckedChange = { isFinished = it })
                    Text("I've finished this book", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Aksi
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pageRead = if (isFinished && currentPage.isBlank()) {
                                totalPages
                            } else {
                                currentPage.toIntOrNull()
                            }

                            if (pageRead == null) {
                                Toast.makeText(context, "Please enter a valid page number.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Panggil fungsi ViewModel yang sudah disederhanakan
                            viewModel.addReadingProgress(userLibraryId, pageRead, recordedDate, isFinished)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Save")
                    }
                }

                // Tampilkan loading jika ada proses di ViewModel
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun rememberDatePickerDialog(
    context: Context,
    onDateSelected: (Date) -> Unit
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return remember {
        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(calendar.time)
        }, year, month, day)
    }
}
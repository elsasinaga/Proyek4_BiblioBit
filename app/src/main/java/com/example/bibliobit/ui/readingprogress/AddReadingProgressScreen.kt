package com.example.bibliobit.ui.readingprogress

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.bibliobit.ui.theme.hijau5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingProgressScreen(
    userLibraryId: Long,
    bookTitle: String,
    totalPages: Int,
    viewModel: ReadingProgressViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val firstReadingProgress by viewModel.firstReadingProgress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State lokal untuk form
    var currentPage by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var lastReadingDate by remember { mutableStateOf(Date()) }
    var isFinished by remember { mutableStateOf(false) }

    // State untuk mengontrol visibilitas DatePickerDialog
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showLastDatePicker by remember { mutableStateOf(false) }

    // Panggil `initialize` untuk memeriksa apakah sudah ada progres sebelumnya
    LaunchedEffect(key1 = userLibraryId) {
        viewModel.initialize(userLibraryId)
    }

    val showStartDateField = firstReadingProgress == null

    Dialog(onDismissRequest = { navController.popBackStack() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(0.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Your Progress", style = MaterialTheme.typography.titleLarge,textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(bookTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                    DateField(
                        label = "Start Date of Reading",
                        date = startDate,
                        onIconClick = { showStartDatePicker = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                // Last Reading Date field
                DateField(
                    label = "Last Reading Date",
                    date = lastReadingDate,
                    onIconClick = { showLastDatePicker = true }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Current Page field
                OutlinedTextField(
                    value = currentPage,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.toLongOrNull() ?: 0L) <= totalPages) {
                            currentPage = newValue
                        }
                    },
                    label = { Text("Current Page") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("/ $totalPages", modifier = Modifier.padding(end = 12.dp)) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Finish Book checkbox
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isFinished = !isFinished },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isFinished, onCheckedChange = { isFinished = it })
                    Text("I've finished this book", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pageRead = if (isFinished && currentPage.isBlank()) totalPages else currentPage.toIntOrNull()
                            if (pageRead == null) {
                                Toast.makeText(context, "Please enter a valid page number.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (showStartDateField) {
                                if (startDate == null) {
                                    Toast.makeText(context, "Start date is required for the first progress.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.insertStartReadingProgress(userLibraryId, startDate!!, lastReadingDate, pageRead, isFinished)
                            } else {
                                viewModel.addReadingProgress(userLibraryId, pageRead, lastReadingDate, isFinished)
                            }
                            navController.popBackStack()
                        },
                        enabled = !isLoading
                    ) {
                        Text("Save")
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Date Picker Dialogs
    if (showStartDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { date ->
                if (date.after(lastReadingDate)) {
                    Toast.makeText(context, "Start date cannot be after last reading date", Toast.LENGTH_SHORT).show()
                } else {
                    startDate = date
                }
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showLastDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { date ->
                if (startDate != null && date.before(startDate)) {
                    Toast.makeText(context, "Last reading date cannot be before start date", Toast.LENGTH_SHORT).show()
                } else {
                    lastReadingDate = date
                }
            },
            onDismiss = { showLastDatePicker = false }
        )
    }
}

@Composable
private fun DateField(label: String, date: Date?, onIconClick: () -> Unit) {
    OutlinedTextField(
        value = date?.let { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(it) } ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = onIconClick) {
                Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = "Select Date", tint = hijau5)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(Date(it)) }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
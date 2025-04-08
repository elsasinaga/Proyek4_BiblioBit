package com.example.bibliobit.ui.readingprogress

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.bibliobit.ui.components.Button1
import com.example.bibliobit.ui.navigation.Screen
import com.example.bibliobit.ui.theme.hijau4
import com.example.bibliobit.ui.theme.hijau5
import com.example.bibliobit.ui.theme.hitam
import com.example.bibliobit.ui.theme.abu2
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddReadingProgressScreen(
    userLibraryId: Long,
    bookTitle: String,
    totalPages: Int,
    userId: String,
    bookId: Long,
    viewModel: ReadingProgressViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val firstReadingProgress by viewModel.firstReadingProgress.collectAsState()
    var startDate by remember { mutableStateOf<Date?>(null) }
    var lastReadingDate by remember { mutableStateOf(Date()) }
    var currentPage by remember { mutableStateOf("") }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializeWithUserLibraryId(userLibraryId)
    }

    val showStartDateField = firstReadingProgress == null

    val startDatePickerDialog = remember {
        createDatePickerDialog(
            context = context,
            maxDate = Date(), // Batasi hingga tanggal hari ini
            onDateSelected = { date ->
                // Pastikan startDate tidak lebih baru dari lastReadingDate
                if (date.after(lastReadingDate)) {
                    Toast.makeText(
                        context,
                        "Start date cannot be after last reading date",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    startDate = date
                }
            }
        )
    }

    val lastReadingDatePickerDialog = remember {
        createDatePickerDialog(
            context = context,
            maxDate = Date(), // Batasi hingga tanggal hari ini
            onDateSelected = { date ->
                // Pastikan lastReadingDate tidak lebih lama dari startDate (jika startDate ada)
                if (startDate != null && date.before(startDate)) {
                    Toast.makeText(
                        context,
                        "Last reading date cannot be before start date",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    lastReadingDate = date
                }
            }
        )
    }

    Dialog(onDismissRequest = {
        navController.popBackStack()
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Your Progress Reading",
                    style = MaterialTheme.typography.h6.copy(
                        fontSize = 20.sp
                    ),
                    color = hitam
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.h5,
                    color = hitam
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (showStartDateField) {
                    TextField(
                        value = startDate?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "",
                        onValueChange = { /* Read-only, gunakan date picker */ },
                        label = { Text("Start Date of Reading", color = hijau5) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = hijau5,
                            unfocusedIndicatorColor = hijau5,
                            focusedLabelColor = hijau5,
                            unfocusedLabelColor = hijau5,
                            cursorColor = hijau5,
                            backgroundColor = MaterialTheme.colors.surface
                        ),
                        trailingIcon = {
                            IconButton(onClick = { startDatePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = hijau5
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TextField(
                    value = lastReadingDate.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    },
                    onValueChange = { /* Read-only, gunakan date picker */ },
                    label = { Text("Last Reading Date", color = hijau5) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = hijau5,
                        unfocusedIndicatorColor = hijau5,
                        focusedLabelColor = hijau5,
                        unfocusedLabelColor = hijau5,
                        cursorColor = hijau5,
                        backgroundColor = MaterialTheme.colors.surface
                    ),
                    trailingIcon = {
                        IconButton(onClick = { lastReadingDatePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Select Date",
                                tint = hijau5
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = currentPage,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() in 0..totalPages) {
                            currentPage = newValue
                        }
                    },
                    label = { Text("Current Page", color = hijau5) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = hijau5,
                        unfocusedIndicatorColor = hijau5,
                        focusedLabelColor = hijau5,
                        unfocusedLabelColor = hijau5,
                        cursorColor = hijau5,
                        backgroundColor = MaterialTheme.colors.surface
                    ),
                    trailingIcon = {
                        Text(
                            text = "/ $totalPages",
                            color = hijau5,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFinished,
                        onCheckedChange = { isFinished = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = hijau5,
                            uncheckedColor = hijau5
                        )
                    )
                    Text(
                        text = "Finish Book",
                        style = MaterialTheme.typography.body1,
                        color = hijau5,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Row untuk menampung tombol Cancel dan Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tombol Cancel
                    Button1(
                        onClick = {
                            navController.popBackStack() // Kembali tanpa menyimpan
                        },
                        modifier = Modifier
                            .weight(1f) // Membagi ruang secara merata
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        backgroundColor = abu2 // Warna abu-abu untuk tombol Cancel
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.button
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Jarak antara tombol

                    // Tombol Save
                    Button1(
                        onClick = {
                            // Jika "Finish Book" dicentang dan currentPage kosong, gunakan totalPages
                            val pageRead = if (isFinished && currentPage.isEmpty()) {
                                totalPages
                            } else {
                                currentPage.toIntOrNull() ?: 0
                            }
                            println("Saving progress: userLibraryId=$userLibraryId, pageRead=$pageRead, totalPages=$totalPages, currentPage='$currentPage', startDate=$startDate, lastReadingDate=$lastReadingDate")
                            if (pageRead in 0..totalPages) {
                                println("PageRead is valid, proceeding to save")
                                if (showStartDateField && startDate == null) {
                                    println("Start date is required for the first progress")
                                    Toast.makeText(context, "Start date is required for the first progress", Toast.LENGTH_SHORT).show()
                                    return@Button1
                                }
                                val recordedAt = if (showStartDateField && startDate != null) startDate!! else lastReadingDate
                                viewModel.updateReadingProgress(
                                    userLibraryId = userLibraryId,
                                    pageRead = pageRead,
                                    recordedAt = recordedAt,
                                    lastReadingDate = lastReadingDate,
                                    isFinished = isFinished || pageRead == totalPages,
                                    totalPages = totalPages
                                )
                                navController.popBackStack()
                            } else {
                                println("Invalid pageRead: $pageRead, must be between 0 and $totalPages")
                                Toast.makeText(context, "Invalid page: must be between 0 and $totalPages", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f) // Membagi ruang secara merata
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        backgroundColor = hijau4
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.button
                        )
                    }
                }
            }
        }
    }
}

private fun createDatePickerDialog(
    context: Context,
    maxDate: Date,
    onDateSelected: (Date) -> Unit
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            onDateSelected(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // Atur tanggal maksimum ke maxDate (hari ini)
    dialog.datePicker.maxDate = maxDate.time
    return dialog
}
package com.example.bibliobit.ui.readingprogress

import android.app.DatePickerDialog
import android.content.Context
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
        createDatePickerDialog(context) { date ->
            startDate = date
        }
    }

    val lastReadingDatePickerDialog = remember {
        createDatePickerDialog(context) { date ->
            lastReadingDate = date
        }
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

                Button1(
                    onClick = {
                        val pageRead = currentPage.toIntOrNull() ?: 0
                        println("Saving progress: userLibraryId=$userLibraryId, pageRead=$pageRead, totalPages=$totalPages, currentPage='$currentPage', startDate=$startDate, lastReadingDate=$lastReadingDate")
                        if (pageRead in 0..totalPages) {
                            println("PageRead is valid, proceeding to save")
                            if (showStartDateField && startDate == null) {
                                println("Start date is required for the first progress")
                                return@Button1
                            }
                            val recordedAt = if (showStartDateField && startDate != null) startDate!! else lastReadingDate
                            viewModel.updateReadingProgress(
                                userLibraryId = userLibraryId,
                                pageRead = pageRead,
                                recordedAt = recordedAt,
                                lastReadingDate = lastReadingDate, // Tambahkan lastReadingDate
                                isFinished = isFinished || pageRead == totalPages
                            )
                            navController.popBackStack()
                        } else {
                            println("Invalid pageRead: $pageRead, must be between 0 and $totalPages")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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

private fun createDatePickerDialog(context: Context, onDateSelected: (Date) -> Unit): DatePickerDialog {
    val calendar = Calendar.getInstance()
    return DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
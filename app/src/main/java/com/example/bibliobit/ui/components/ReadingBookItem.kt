package com.example.bibliobit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.bibliobit.data.model.Book
import com.example.bibliobit.ui.theme.BiblioBitTheme
import com.example.bibliobit.ui.theme.Typography
import java.io.File

@Composable
fun ReadingBookItem(
    book: Book,
    lastPageRead: Int,
    totalPages: Int = 300, // Asumsi total halaman
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover Buku
        if (book.coverPhotoPath != null) {
            Image(
                painter = rememberAsyncImagePainter(File(book.coverPhotoPath)),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "No Cover",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Judul Buku
        Text(
            text = book.title,
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        // Progress Bar
        val progress = (lastPageRead.toFloat() / totalPages).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(140.dp) // Selebar cover
                .height(10.dp) // Ketebalan lebih kecil
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "$lastPageRead / $totalPages",
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
fun ReadingBookItemPreview() {
    BiblioBitTheme {
        ReadingBookItem(
            book = Book(
                id = 1,
                title = "Book Title",
                author = "Author Name",
                genre = null,
                year = null,
                description = null,
                isbn = null,
                coverPhotoPath = null,
                publisher = null
            ),
            lastPageRead = 150
        )
    }
}
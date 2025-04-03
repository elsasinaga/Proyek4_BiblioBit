package com.example.bibliobit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun FinishBookItem(
    book: Book,
    rating: Float,
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

        // Rating
        RatingBar(
            rating = rating,
            modifier = Modifier.height(20.dp).padding(top = 4.dp)
        )
    }
}

@Preview
@Composable
fun FinishBookItemPreview() {
    BiblioBitTheme {
        FinishBookItem(
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
            rating = 4.5f
        )
    }
}
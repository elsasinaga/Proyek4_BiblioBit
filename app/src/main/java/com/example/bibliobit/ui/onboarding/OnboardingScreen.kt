package com.example.bibliobit.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bibliobit.R
import com.example.bibliobit.ui.theme.BiblioBitTheme

data class OnboardingScreen(
    val title: String,
    val description: String,
    val imageResId: Int
)

@Composable
fun OnboardingScreen(
    onBoardingComplete: () -> Unit // Pastikan tanpa @Composable
) {
    val pages = listOf(
        OnboardingScreen(
            title = "Welcome to BiblioBit",
            description = "Easily manage your reading list, track \n" +
                    "progress, and set goals to enhance \n" +
                    "your reading experience.",
            imageResId = R.drawable.onboard1
        ),
        OnboardingScreen(
            title = "Set Goals & Achieve More",
            description = "Challenge yourself with reading goals \n" +
                    "and see your progress in real-time. \n" +
                    "Every book counts!",
            imageResId = R.drawable.onboard2
        ),
        OnboardingScreen(
            title = "Save and Plan Your Reads",
            description = "Create a wishlist, organize finished \n" +
                    "books, and keep track of \n" +
                    "what to read next.",
            imageResId = R.drawable.onboard3
        )
    )

    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Tombol Skip di atas kiri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Skip",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .clickable {
                        onBoardingComplete() // Sekarang tidak ada error
                    }
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = pages[currentPage].imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 32.dp)
            )
            // Judul
            Text(
                text = pages[currentPage].title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // Deskripsi
            Text(
                text = pages[currentPage].description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indikator halaman
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentPage == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onBoardingComplete() // Sekarang tidak ada error
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    BiblioBitTheme {
        OnboardingScreen(
            onBoardingComplete = {}
        )
    }
}